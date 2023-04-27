package ua.caunt.bungeeforge.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(net.minecraft.server.network.ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImpl {
    @ModifyConstant(method = "tick()V", constant = @Constant(longValue = 15000L))
    private long bungee$modifyConstant(long value) {
        return ServerConnectionListener.getReadTimeout() * 1000L;
    }
}
