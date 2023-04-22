package ua.caunt.bungeeforge.mixin.server.network;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(net.minecraft.server.network.ServerConnectionListener.class)
public interface ServerConnectionListener {
    @Accessor(value = "READ_TIMEOUT", remap = false)
    static int getReadTimeout() {
        return 30;
    }
}
