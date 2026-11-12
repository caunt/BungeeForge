package ua.caunt.bungeeforge;

import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * First handler of every connection. Reads the handshake as raw bytes (its format is the same in every version
 * since 1.7), takes the forwarded player info out of the hostname and hands Minecraft the handshake it expects.
 * Minecraft therefore never sees the long hostname, so its 255 character limit does not need patching.
 */
final class HandshakeHandler extends ChannelInboundHandlerAdapter {
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final int STATUS = 1;
    /** Packet id, protocol, a hostname of up to 32767 characters in UTF-8, port and intention. */
    private static final int MAX_HANDSHAKE = 1 + 5 + 3 + 3 * Short.MAX_VALUE + 2 + 5;

    private ByteBuf buffer;
    private boolean rejected;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }
        ByteBuf in = (ByteBuf) msg;
        if (rejected) {
            in.release();
            return;
        }
        if (buffer == null)
            buffer = ctx.alloc().buffer(in.readableBytes());
        buffer.writeBytes(in);
        in.release();
        process(ctx);
    }

    private void process(ChannelHandlerContext ctx) {
        ByteBuf buf = buffer;
        if (!buf.isReadable())
            return;
        if (isLegacyPing(buf)) {
            forward(ctx, buf);
            return;
        }

        int start = buf.readerIndex();
        int length;
        try {
            length = readVarInt(buf);
        } catch (IndexOutOfBoundsException incomplete) {
            buf.readerIndex(start);
            return;
        } catch (IllegalArgumentException malformed) {
            length = -1;
        }
        if (length >= 0 && length <= MAX_HANDSHAKE && length > buf.readableBytes()) {
            buf.readerIndex(start); // wait for the rest of the packet
            return;
        }

        int protocol, port, intention;
        String hostname;
        try {
            if (length < 0 || length > MAX_HANDSHAKE)
                throw new IllegalArgumentException("bad packet length " + length);
            ByteBuf packet = buf.readSlice(length);
            if (readVarInt(packet) != 0x00)
                throw new IllegalArgumentException("not a handshake");
            protocol = readVarInt(packet);
            hostname = readString(packet);
            port = packet.readUnsignedShort();
            intention = readVarInt(packet);
        } catch (RuntimeException malformed) {
            buf.readerIndex(start); // not ours to judge: Minecraft rejects it as usual
            forward(ctx, buf);
            return;
        }

        Forwarding forwarding = Forwarding.parse(hostname);
        if (intention == STATUS) { // server list ping: never forwarded, just keep the hostname short
            String host = forwarding != null ? forwarding.host : hostname;
            forward(ctx, handshake(ctx, protocol, host, port, intention, buf));
            return;
        }
        if (forwarding == null) {
            reject(ctx, "This server only accepts connections through its proxy.");
            Log.warn("Rejected " + ctx.channel().remoteAddress() + ": connected without BungeeCord forwarding data");
            return;
        }

        try {
            attach(ctx, forwarding);
        } catch (Exception e) {
            reject(ctx, "BungeeForge does not support this server version.");
            Log.error("Could not apply forwarding for " + ctx.channel().remoteAddress() + ", disconnecting", e);
            return;
        }
        forward(ctx, handshake(ctx, protocol, forwarding.host, port, intention, buf));
    }

    /**
     * Pre-1.7 server list pings are {@code FE}, {@code FE 01} or {@code FE 01 FA ...}. A handshake can start with
     * {@code FE} too (lengths 254, 382, ...), but then its packet id 0x00 follows: the same test Minecraft uses.
     */
    private static boolean isLegacyPing(ByteBuf buf) {
        int i = buf.readerIndex();
        if (buf.getUnsignedByte(i) != 0xFE)
            return false;
        return buf.readableBytes() < 3 || (buf.getUnsignedByte(i + 1) == 0x01 && buf.getUnsignedByte(i + 2) == 0xFA);
    }

    /** Points Minecraft's connection at the player's address and sets up the profile swap at login. */
    private static void attach(ChannelHandlerContext ctx, Forwarding forwarding) throws IllegalAccessException {
        Map.Entry<String, ChannelHandler> connection = connection(ctx.pipeline());
        Object handler = connection.getValue();
        Field address = Reflection.requireField(handler.getClass(), SocketAddress.class);
        SocketAddress proxy = ctx.channel().remoteAddress();
        int port = proxy instanceof InetSocketAddress ? ((InetSocketAddress) proxy).getPort() : 0;

        address.set(handler, new InetSocketAddress(forwarding.address, port));
        ctx.pipeline().addBefore(connection.getKey(), Injector.LOGIN, new LoginHandler(forwarding, handler));
    }

    /** Minecraft's connection object: the pipeline handler that owns a Channel and a SocketAddress. */
    private static Map.Entry<String, ChannelHandler> connection(ChannelPipeline pipeline) {
        Map.Entry<String, ChannelHandler> found = null;
        for (Map.Entry<String, ChannelHandler> entry : pipeline.toMap().entrySet()) {
            Class<?> type = entry.getValue().getClass();
            if (Reflection.isLibrary(type) || Reflection.field(type, Channel.class) == null || Reflection.field(type, SocketAddress.class) == null)
                continue;
            if (found != null)
                throw new IllegalStateException("more than one connection handler in the pipeline");
            found = entry;
        }
        if (found == null)
            throw new IllegalStateException("no connection handler in the pipeline " + pipeline.names());
        return found;
    }

    /** Sends the buffered bytes on to Minecraft and steps out of the pipeline. */
    private void forward(ChannelHandlerContext ctx, ByteBuf data) {
        if (data != buffer)
            buffer.release();
        buffer = null;
        ctx.pipeline().remove(this);
        ctx.fireChannelRead(data);
    }

    /** Disconnects with a message. Only used in the login phase, before compression or encryption exist. */
    private void reject(ChannelHandlerContext ctx, String message) {
        rejected = true;
        buffer.release();
        buffer = null;
        ByteBuf body = ctx.alloc().buffer();
        writeVarInt(body, 0x00); // login disconnect
        writeString(body, "{\"text\":" + new JsonPrimitive(message) + "}");
        ByteBuf frame = ctx.alloc().buffer();
        writeVarInt(frame, body.readableBytes());
        frame.writeBytes(body);
        body.release();
        ctx.writeAndFlush(frame).addListener(ChannelFutureListener.CLOSE);
    }

    /** A rebuilt handshake packet followed by whatever the client already sent after it. */
    private static ByteBuf handshake(ChannelHandlerContext ctx, int protocol, String host, int port, int intention, ByteBuf rest) {
        ByteBuf body = ctx.alloc().buffer();
        writeVarInt(body, 0x00);
        writeVarInt(body, protocol);
        writeString(body, host);
        body.writeShort(port);
        writeVarInt(body, intention);
        ByteBuf out = ctx.alloc().buffer(body.readableBytes() + 5 + rest.readableBytes());
        writeVarInt(out, body.readableBytes());
        out.writeBytes(body);
        out.writeBytes(rest);
        body.release();
        return out;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        if (buffer != null) {
            buffer.release();
            buffer = null;
        }
    }

    private static int readVarInt(ByteBuf buf) {
        int value = 0;
        for (int i = 0; i < 5; i++) {
            byte b = buf.readByte();
            value |= (b & 0x7F) << (7 * i);
            if ((b & 0x80) == 0)
                return value;
        }
        throw new IllegalArgumentException("VarInt too big");
    }

    private static void writeVarInt(ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    private static String readString(ByteBuf buf) {
        int length = readVarInt(buf);
        if (length < 0 || length > 3 * Short.MAX_VALUE)
            throw new IllegalArgumentException("bad string length " + length);
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, UTF_8);
    }

    private static void writeString(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
}
