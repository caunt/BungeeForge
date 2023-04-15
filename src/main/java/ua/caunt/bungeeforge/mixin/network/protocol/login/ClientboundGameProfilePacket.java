package ua.caunt.bungeeforge.mixin.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.protocol.login.ClientboundGameProfilePacket.class)
public class ClientboundGameProfilePacket {
    @Shadow
    private GameProfile gameProfile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "handle", at = @At("HEAD"))
    private void handle(ClientLoginPacketListener handler, CallbackInfo callbackInfo) {
        Connection connection = handler.getConnection();

        if (!BungeeForge.MAP.containsKey(connection))
            return;

        var tuple = BungeeForge.MAP.get(connection);

        gameProfile = new GameProfile(tuple.getA(), gameProfile.getName());
        var properties = gameProfile.getProperties();

        Arrays.stream(tuple.getB()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
