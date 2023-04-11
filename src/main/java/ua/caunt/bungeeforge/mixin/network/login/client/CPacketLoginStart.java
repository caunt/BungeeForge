package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

@Mixin(net.minecraft.network.login.client.CPacketLoginStart.class)
public class CPacketLoginStart {
    @Shadow
    private GameProfile profile;

    /*@Redirect(method = "readPacketData", at = @At(value = "NEW", target = "com/mojang/authlib/GameProfile"))
    public GameProfile readPacketData(UUID uuid, String name) {
        return new GameProfile(UUID.fromString("14d8e92c-3384-471a-bd1b-b91d18799758"), "caunt");
    }*/


    @Inject(method = "processPacket", at = @At("HEAD"))
    private void processPacket(INetHandlerLoginServer handler, CallbackInfo callbackInfo) {
        NetworkManager networkManager = ((NetHandlerLoginServer) handler).networkManager;

        if (!BungeeForge.MAP.containsKey(networkManager))
            return;

        GameProfile spoofedProfile = BungeeForge.MAP.get(networkManager);
        profile = new GameProfile(spoofedProfile.getId(), profile.getName());
    }
}
