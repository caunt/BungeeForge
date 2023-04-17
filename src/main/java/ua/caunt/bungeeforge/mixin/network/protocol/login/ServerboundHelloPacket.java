package ua.caunt.bungeeforge.mixin.network.protocol.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.protocol.login.ServerboundHelloPacket.class)
public class ServerboundHelloPacket {
    @Mutable
    @Final
    @Shadow
    private GameProfile gameProfile;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "handle(Lnet/minecraft/network/protocol/login/ServerLoginPacketListener;)V", at = @At("HEAD"))
    public void bungee$handle(ServerLoginPacketListener p_134848_, CallbackInfo ci) {
        ConnectionBridge connectionBridge = (ConnectionBridge)p_134848_.getConnection();

        if (!connectionBridge.bungee$hasSpoofedProfile())
            return;

        gameProfile = new GameProfile(connectionBridge.bungee$getSpoofedId().get(), gameProfile.getName());
        PropertyMap properties = gameProfile.getProperties();

        Arrays.stream(connectionBridge.bungee$getSpoofedProperties().get()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
