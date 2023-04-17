package ua.caunt.bungeeforge.mixin.network.protocol.login;

import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;
import ua.caunt.bungeeforge.bridge.server.network.ServerLoginPacketListenerImplBridge;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.protocol.login.ServerboundHelloPacket.class)
public class ServerboundHelloPacket {
    @Mutable
    @Final
    @Shadow
    private Optional<UUID> profileId;
    @Mutable
    @Final
    @Shadow
    private String name;

    @Inject(method = "handle(Lnet/minecraft/network/protocol/login/ServerLoginPacketListener;)V", at = @At("HEAD"))
    public void bungee$head(ServerLoginPacketListener p_134848_, CallbackInfo ci) {
        var serverLoginPacketListenerImplBridge = (ServerLoginPacketListenerImplBridge)p_134848_;
        var connectionBridge = (ConnectionBridge)serverLoginPacketListenerImplBridge.bungee$getConnection();

        if (!connectionBridge.bungee$hasSpoofedProfile())
            return;

        profileId = connectionBridge.bungee$getSpoofedId();
    }
}
