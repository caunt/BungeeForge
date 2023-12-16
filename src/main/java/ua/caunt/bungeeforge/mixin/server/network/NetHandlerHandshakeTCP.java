package ua.caunt.bungeeforge.mixin.server.network;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.NetworkManagerBridge;
import ua.caunt.bungeeforge.bridge.network.handshake.client.C00HandshakeBridge;

@Mixin(net.minecraft.server.network.NetHandlerHandshakeTCP.class)
public class NetHandlerHandshakeTCP {
    @Final
    @Shadow
    private NetworkManager field_147386_b;

    @Inject(method = "processHandshake(Lnet/minecraft/network/handshake/client/C00Handshake;)V", at = @At("HEAD"))
    public void bungee$processHandshake(C00Handshake packetIn, CallbackInfo ci) {
        C00HandshakeBridge handshakeBridge = (C00HandshakeBridge) packetIn;
        NetworkManagerBridge networkManagerBridge = (NetworkManagerBridge) field_147386_b;

        networkManagerBridge.bungee$setSpoofedAddress(handshakeBridge.bungee$getSpoofedAddress());
        networkManagerBridge.bungee$setSpoofedId(handshakeBridge.bungee$getSpoofedId());
        networkManagerBridge.bungee$setSpoofedProperties(handshakeBridge.bungee$getSpoofedProperties());
    }
}
