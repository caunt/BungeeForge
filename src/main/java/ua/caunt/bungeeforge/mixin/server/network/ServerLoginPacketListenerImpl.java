package ua.caunt.bungeeforge.mixin.server.network;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;
import ua.caunt.bungeeforge.bridge.server.network.ServerLoginPacketListenerImplBridge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.server.network.ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImpl implements ServerLoginPacketListenerImplBridge {
    @Final
    @Shadow
    Connection connection;
    @Shadow
    public GameProfile gameProfile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "handleHello(Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;)V", at = @At("HEAD"))
    public void bungee$handleHello(ServerboundHelloPacket p_10047_, CallbackInfo ci) {
        var connectionBridge = (ConnectionBridge)bungee$getConnection();

        if (!connectionBridge.bungee$hasSpoofedProfile())
            return;

        gameProfile = new GameProfile(connectionBridge.bungee$getSpoofedId().get(), gameProfile.getName());
        var properties = gameProfile.getProperties();

        Arrays.stream(connectionBridge.bungee$getSpoofedProperties().get()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });

        ci.cancel();
    }

    @Override
    public Connection bungee$getConnection() {
        return connection;
    }
}
