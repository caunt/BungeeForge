package ua.caunt.bungeeforge.mixin.network.login.client;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.login.INetHandlerLoginServer;
import net.minecraft.server.network.NetHandlerLoginServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.NetworkManagerBridge;

import java.util.Arrays;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.login.client.C00PacketLoginStart.class)
public class C00PacketLoginStart {
    @Shadow
    private GameProfile field_149305_a;

    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Inject(method = "processPacket(Lnet/minecraft/network/login/INetHandlerLoginServer;)V", at = @At("HEAD"))
    private void bungee$processPacket(INetHandlerLoginServer handler, CallbackInfo ci) {
        NetHandlerLoginServer netHandlerLoginServer = (NetHandlerLoginServer) handler;
        NetworkManagerBridge networkManagerBridge = (NetworkManagerBridge)netHandlerLoginServer.field_147333_a;

        if (!networkManagerBridge.bungee$hasSpoofedProfile())
            return;

        field_149305_a = new GameProfile(networkManagerBridge.bungee$getSpoofedId().get(), field_149305_a.getName());
        PropertyMap properties = field_149305_a.getProperties();

        Arrays.stream(networkManagerBridge.bungee$getSpoofedProperties().get()).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
            properties.put(property.getName(), property);
        });
    }
}
