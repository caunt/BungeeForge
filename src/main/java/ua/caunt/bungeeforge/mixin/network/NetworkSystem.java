package ua.caunt.bungeeforge.mixin.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.network.NetworkSystem.class)
public interface NetworkSystem {
    @Accessor(value = "READ_TIMEOUT", remap = false)
    static int getReadTimeout() {
        return 30;
    }
}

