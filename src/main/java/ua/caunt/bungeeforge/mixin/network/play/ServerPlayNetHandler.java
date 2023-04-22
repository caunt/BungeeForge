package ua.caunt.bungeeforge.mixin.network.play;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ua.caunt.bungeeforge.mixin.network.NetworkSystem;

@Mixin(net.minecraft.network.play.ServerPlayNetHandler.class)
public class ServerPlayNetHandler {
    @ModifyConstant(method = "tick()V", constant = @Constant(longValue = 15000L))
    private long bungee$modifyConstant(long value) {
        return NetworkSystem.getReadTimeout() * 1000L;
    }
}
