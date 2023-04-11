package ua.caunt.bungeeforge.mixin.network;

import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(net.minecraft.server.network.NetHandlerHandshakeTCP.class)
public class NetHandlerHandshakeTCP {
    @Shadow
    public NetworkManager networkManager;
}
