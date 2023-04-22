package ua.caunt.bungeeforge.mixin.network;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(net.minecraft.network.NetHandlerPlayServer.class)
public class NetHandlerPlayServer {
    @ModifyConstant(method = "update()V", constant = @Constant(longValue = 15000L))
    private long bungee$modifyConstant(long value) {
        return FMLNetworkHandler.READ_TIMEOUT * 1000L;
    }
}
