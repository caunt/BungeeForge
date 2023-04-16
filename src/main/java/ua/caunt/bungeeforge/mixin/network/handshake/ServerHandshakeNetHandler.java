package ua.caunt.bungeeforge.mixin.network.handshake;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.CHandshakePacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.handshake.client.CHandshakePacketBridge;
import ua.caunt.bungeeforge.bridge.network.NetworkManagerBridge;

@Mixin(net.minecraft.network.handshake.ServerHandshakeNetHandler.class)
public class ServerHandshakeNetHandler {
    @Final
    @Shadow
    private NetworkManager connection;

    @Inject(method = "handleIntention(Lnet/minecraft/network/handshake/client/CHandshakePacket;)V", at = @At("HEAD"))
    public void bungee$handleIntention(CHandshakePacket p_147383_1_, CallbackInfo ci) {
        CHandshakePacketBridge handshakePacketBridge = (CHandshakePacketBridge) p_147383_1_;
        NetworkManagerBridge networkManagerBridge = (NetworkManagerBridge) connection;

        networkManagerBridge.bungee$setSpoofedId(handshakePacketBridge.bungee$getSpoofedId());
        networkManagerBridge.bungee$setSpoofedProperties(handshakePacketBridge.bungee$getSpoofedProperties());
    }
}
