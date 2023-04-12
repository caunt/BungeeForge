package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraft.network.login.ServerLoginNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

@Mixin(net.minecraft.network.login.client.CLoginStartPacket.class)
public class CLoginStartPacket {
    @Shadow
    private GameProfile gameProfile;

    @Inject(method = "handle", at = @At("HEAD"))
    private void processPacket(IServerLoginNetHandler handler, CallbackInfo callbackInfo) {
        NetworkManager networkManager = ((ServerLoginNetHandler) handler).connection;

        if (!BungeeForge.MAP.containsKey(networkManager))
            return;

        GameProfile spoofedProfile = BungeeForge.MAP.get(networkManager);
        gameProfile = new GameProfile(spoofedProfile.getId(), gameProfile.getName());
    }
}
