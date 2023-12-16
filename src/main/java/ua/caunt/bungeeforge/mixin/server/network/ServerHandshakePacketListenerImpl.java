package ua.caunt.bungeeforge.mixin.server.network;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;
import ua.caunt.bungeeforge.bridge.network.protocol.handshake.ClientIntentionPacketBridge;

@Mixin(net.minecraft.server.network.ServerHandshakePacketListenerImpl.class)
public class ServerHandshakePacketListenerImpl {
    @Final
    @Shadow
    private Connection connection;

    @Inject(method = "handleIntention(Lnet/minecraft/network/protocol/handshake/ClientIntentionPacket;)V", at = @At("HEAD"))
    public void bungee$handleIntention(ClientIntentionPacket p_9975_, CallbackInfo ci) {
        ClientIntentionPacketBridge clientIntentionPacketBridge = (ClientIntentionPacketBridge) (Object) p_9975_;
        ConnectionBridge connectionBridge = (ConnectionBridge) connection;

        connectionBridge.bungee$setSpoofedAddress(clientIntentionPacketBridge.bungee$getSpoofedAddress());
        connectionBridge.bungee$setSpoofedId(clientIntentionPacketBridge.bungee$getSpoofedId());
        connectionBridge.bungee$setSpoofedProperties(clientIntentionPacketBridge.bungee$getSpoofedProperties());
    }
}
