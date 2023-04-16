package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraft.network.login.ServerLoginNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.NetworkManagerBridge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.login.client.CLoginStartPacket.class)
public class CLoginStartPacket {
    @Shadow
    private GameProfile gameProfile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "handle(Lnet/minecraft/network/login/IServerLoginNetHandler;)V", at = @At("HEAD"))
    private void bungee$handle(IServerLoginNetHandler handler, CallbackInfo callbackInfo) {
        ServerLoginNetHandler serverLoginNetHandler = (ServerLoginNetHandler) handler;
        NetworkManagerBridge networkManagerBridge = (NetworkManagerBridge)serverLoginNetHandler.connection;

        if (!networkManagerBridge.bungee$hasSpoofedProfile())
            return;

        gameProfile = new GameProfile(networkManagerBridge.bungee$getSpoofedId().get(), gameProfile.getName());
        PropertyMap properties = gameProfile.getProperties();

        Arrays.stream(networkManagerBridge.bungee$getSpoofedProperties().get()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
