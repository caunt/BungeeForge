package ua.caunt.bungeeforge.mixin.server.network;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;
import ua.caunt.bungeeforge.bridge.server.network.ServerLoginPacketListenerImplBridge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.server.network.ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImpl implements ServerLoginPacketListenerImplBridge {
    @Final
    @Shadow
    Connection connection;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Redirect(method = "handleHello(Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerLoginPacketListenerImpl;gameProfile:Lcom/mojang/authlib/GameProfile;", opcode = Opcodes.PUTFIELD))
    public void bungee$handleHello(net.minecraft.server.network.ServerLoginPacketListenerImpl instance, GameProfile value) {
        var connectionBridge = (ConnectionBridge)bungee$getConnection();

        if (!connectionBridge.bungee$hasSpoofedProfile()) {
            instance.gameProfile = value;
            return;
        }

        var gameProfile = new GameProfile(connectionBridge.bungee$getSpoofedId().get(), value.getName());
        var properties = gameProfile.getProperties();

        Arrays.stream(connectionBridge.bungee$getSpoofedProperties().get()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });

        instance.gameProfile = gameProfile;
    }

    @Override
    public Connection bungee$getConnection() {
        return connection;
    }
}
