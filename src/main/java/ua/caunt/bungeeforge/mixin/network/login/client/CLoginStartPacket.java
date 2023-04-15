package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.IServerLoginNetHandler;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.login.client.CLoginStartPacket.class)
public class CLoginStartPacket {
    @Shadow
    private GameProfile gameProfile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "handle", at = @At("HEAD"))
    private void processPacket(IServerLoginNetHandler handler, CallbackInfo callbackInfo) {
        NetworkManager networkManager = ((ServerLoginNetHandler) handler).connection;

        if (!BungeeForge.MAP.containsKey(networkManager))
            return;

        Tuple<UUID, Property[]> tuple = BungeeForge.MAP.get(networkManager);

        gameProfile = new GameProfile(tuple.getA(), gameProfile.getName());
        PropertyMap properties = gameProfile.getProperties();

        Arrays.stream(tuple.getB()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
