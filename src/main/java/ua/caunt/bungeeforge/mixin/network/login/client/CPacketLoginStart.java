package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.server.network.NetHandlerLoginServer;
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

@Mixin(net.minecraft.network.login.client.CPacketLoginStart.class)
public class CPacketLoginStart {
    @Shadow
    private GameProfile profile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "processPacket", at = @At("HEAD"))
    private void processPacket(INetHandlerLoginServer handler, CallbackInfo callbackInfo) {
        NetworkManager networkManager = ((NetHandlerLoginServer) handler).networkManager;

        if (!BungeeForge.MAP.containsKey(networkManager))
            return;

        Tuple<UUID, Property[]> tuple = BungeeForge.MAP.get(networkManager);

        profile = new GameProfile(tuple.getFirst(), profile.getName());
        PropertyMap properties = profile.getProperties();

        Arrays.stream(tuple.getSecond()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
