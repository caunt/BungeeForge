package ua.caunt.bungeeforge;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Waits for the server to open its port, then hooks into the listening channel so every new connection gets a
 * {@link HandshakeHandler} in front of Minecraft's own handlers (the approach ViaVersion uses on Bukkit).
 */
final class Injector implements Runnable {
    static final String ACCEPTOR = "bungeeforge:acceptor";
    static final String HANDSHAKE = "bungeeforge:handshake";
    static final String LOGIN = "bungeeforge:login";

    private static boolean started;

    static synchronized void start() {
        if (started)
            return;
        started = true;
        if (exists("net.minecraft.client.Minecraft")) {
            Log.info("Client detected, staying inactive: BungeeForge only does something on dedicated servers");
            return;
        }
        Thread thread = new Thread(new Injector(), "BungeeForge");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        try {
            Callable<Object> server = serverLookup();
            long since = System.nanoTime();
            boolean warned = false;
            while (true) {
                Object instance = server.call();
                if (instance != null && inject(instance))
                    return;
                if (!warned && System.nanoTime() - since > TimeUnit.MINUTES.toNanos(2)) {
                    Log.warn("Still waiting for the server to open its port");
                    warned = true;
                }
                Thread.sleep(10);
            }
        } catch (Throwable t) {
            Log.error("Could not hook into the server, forwarding is NOT active", t);
        }
    }

    /** Forge and NeoForge both expose the running server through a class whose name never gets obfuscated. */
    private static Callable<Object> serverLookup() throws Exception {
        for (String name : new String[]{"cpw.mods.fml.common.FMLCommonHandler", "net.minecraftforge.fml.common.FMLCommonHandler"}) {
            Class<?> type = load(name);
            if (type != null) {
                final Object handler = type.getMethod("instance").invoke(null);
                final Method getter = type.getMethod("getMinecraftServerInstance");
                return () -> getter.invoke(handler);
            }
        }
        for (String name : new String[]{"net.neoforged.neoforge.server.ServerLifecycleHooks", "net.minecraftforge.server.ServerLifecycleHooks", "net.minecraftforge.fml.server.ServerLifecycleHooks"}) {
            Class<?> type = load(name);
            if (type != null) {
                final Method getter = type.getMethod("getCurrentServer");
                return () -> getter.invoke(null);
            }
        }
        throw new IllegalStateException("no known Forge or NeoForge server accessor");
    }

    /**
     * Finds the server's network listener: the object it references that keeps a list of bound ChannelFutures.
     * Returns false until that list has something in it, i.e. until the port is open.
     */
    private static boolean inject(Object server) throws IllegalAccessException {
        for (Object candidate : Reflection.values(server)) {
            if (Reflection.isLibrary(candidate.getClass()))
                continue;
            List<Channel> channels = new ArrayList<Channel>();
            List<Channel> existing = new ArrayList<Channel>();
            for (Object value : Reflection.values(candidate)) {
                if (!(value instanceof List))
                    continue;
                List<?> list = (List<?>) value;
                try {
                    synchronized (list) { // Minecraft keeps these as synchronized lists
                        for (Object element : list) {
                            if (element instanceof ChannelFuture)
                                channels.add(((ChannelFuture) element).channel());
                            else if (element instanceof ChannelHandler)
                                existing.add(channelOf(element));
                        }
                    }
                } catch (RuntimeException concurrentlyModified) { // some unrelated list; not the one we want
                    continue;
                }
            }
            if (channels.isEmpty())
                continue;

            for (Channel channel : channels) {
                if (channel.pipeline().get(ACCEPTOR) == null)
                    channel.pipeline().addFirst(ACCEPTOR, new Acceptor());
            }
            // Anything accepted before we were in place never went through the handshake check.
            int closed = 0;
            for (Channel channel : existing) {
                if (channel != null && channel.isOpen()) {
                    channel.close();
                    closed++;
                }
            }
            Log.info("Accepting BungeeCord forwarding on " + addresses(channels)
                    + (closed > 0 ? " (closed " + closed + " connection(s) made before startup finished)" : ""));
            return true;
        }
        return false;
    }

    private static Channel channelOf(Object connection) {
        try {
            Field field = Reflection.field(connection.getClass(), Channel.class);
            return field == null ? null : (Channel) field.get(connection);
        } catch (Exception e) {
            return null;
        }
    }

    private static String addresses(List<Channel> channels) {
        StringBuilder builder = new StringBuilder();
        for (Channel channel : channels)
            builder.append(builder.length() == 0 ? "" : ", ").append(channel.localAddress());
        return builder.toString();
    }

    private static Class<?> load(String name) {
        try {
            return Class.forName(name, true, Injector.class.getClassLoader());
        } catch (ClassNotFoundException | LinkageError e) {
            return null;
        }
    }

    private static boolean exists(String name) {
        try {
            Class.forName(name, false, Injector.class.getClassLoader());
            return true;
        } catch (Throwable e) { // not found, or (Forge 1.7 - 1.12) refused because it is client-only
            return false;
        }
    }

    /** On the listening channel: every accepted connection is a message here, before Minecraft initialises it. */
    private static final class Acceptor extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof Channel)
                ((Channel) msg).pipeline().addFirst(HANDSHAKE, new HandshakeHandler());
            ctx.fireChannelRead(msg);
        }
    }
}
