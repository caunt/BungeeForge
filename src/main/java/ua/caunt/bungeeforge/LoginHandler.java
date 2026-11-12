package ua.caunt.bungeeforge;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.lang.reflect.Field;
import java.util.UUID;

/**
 * Sits right before Minecraft's connection handler and sees decoded packets. Gives the player the forwarded
 * UUID and skin at login, then checks the login success packet really carries that UUID.
 */
final class LoginHandler extends ChannelDuplexHandler {
    private final Forwarding forwarding;
    private final Object connection;
    private int received;

    LoginHandler(Forwarding forwarding, Object connection) {
        this.forwarding = forwarding;
        this.connection = connection;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 1st packet: the handshake. 2nd: login start, the only one we act on.
        if (++received != 2) {
            ctx.fireChannelRead(msg);
            return;
        }

        Field packetProfile;
        try {
            packetProfile = Reflection.field(msg.getClass(), Reflection.GAME_PROFILE);
            if (packetProfile != null) {
                // Up to 1.18 the packet carries the profile Minecraft goes on to use: replace it before it is read.
                packetProfile.set(msg, Reflection.newProfile(packetProfile.get(msg), forwarding));
            }
        } catch (Exception e) {
            fail(ctx, e);
            ReferenceCountUtil.release(msg);
            return;
        }

        if (packetProfile != null) {
            // Nothing can race a swap made before Minecraft reads the packet: no need to watch the login reply.
            ctx.pipeline().remove(this);
            ctx.fireChannelRead(msg);
            return;
        }

        // Since 1.19 the packet only has the name: Minecraft builds the offline profile while handling it, which
        // happens right here on this thread, and the server thread picks it up on its next tick. Do all the slow
        // work first, so that afterwards only one already-used field write separates the two.
        Field field;
        Object listener, forwardedProfile = null;
        try {
            listener = loginListener(); // created when the handshake was handled
            field = Reflection.requireField(listener.getClass(), Reflection.GAME_PROFILE);
            field.set(listener, field.get(listener)); // first read and write through a Field are slow: do both now (it is still null)
            Field name = Reflection.field(msg.getClass(), String.class);
            if (name != null)
                forwardedProfile = Reflection.newProfile(field.getType(), (String) name.get(msg), forwarding);
        } catch (Exception e) {
            fail(ctx, e);
            ReferenceCountUtil.release(msg);
            return;
        }

        ctx.fireChannelRead(msg);

        try {
            Object created = field.get(listener);
            if (forwardedProfile != null)
                field.set(listener, forwardedProfile);
            if (created == null)
                throw new IllegalStateException("login start was not handled synchronously by " + listener.getClass().getName());
            if (forwardedProfile == null)
                field.set(listener, Reflection.newProfile(created, forwarding));
        } catch (Exception e) {
            fail(ctx, e);
        }
    }

    /** The connection's current packet listener: the one object it references that holds a GameProfile. */
    private Object loginListener() throws IllegalAccessException {
        Object found = null;
        for (Object value : Reflection.values(connection)) {
            if (value == found || Reflection.isLibrary(value.getClass()) || Reflection.field(value.getClass(), Reflection.GAME_PROFILE) == null)
                continue;
            if (found != null)
                throw new IllegalStateException("more than one login listener candidate on " + connection.getClass().getName());
            found = value;
        }
        if (found == null)
            throw new IllegalStateException("no login listener on " + connection.getClass().getName());
        return found;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        UUID sent;
        try {
            Field field = Reflection.field(msg.getClass(), Reflection.GAME_PROFILE);
            if (field == null) {
                ctx.write(msg, promise);
                return;
            }
            sent = Reflection.profileId(field.get(msg));
        } catch (Exception e) {
            sent = null;
        }

        // Login success. If the server thread ever read the offline profile before the swap, refuse the login
        // instead of letting the player in under the wrong identity.
        ctx.pipeline().remove(this);
        if (forwarding.id.equals(sent)) {
            ctx.write(msg, promise);
            return;
        }
        Log.error("Login of " + forwarding.id + " came out as " + sent + "; disconnecting, the player can simply reconnect");
        ReferenceCountUtil.release(msg);
        promise.setFailure(new IllegalStateException("forwarded profile was not applied"));
        ctx.close();
    }

    private void fail(ChannelHandlerContext ctx, Exception e) {
        Log.error("Could not apply forwarded profile " + forwarding.id + ", disconnecting", e);
        ctx.close();
    }
}
