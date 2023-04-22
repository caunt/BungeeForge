package ua.caunt.bungeeforge.mixin.network.protocol.handshake;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.protocol.handshake.ClientIntentionPacketBridge;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Mixin(net.minecraft.network.protocol.handshake.ClientIntentionPacket.class)
public class ClientIntentionPacket implements ClientIntentionPacketBridge {
    private UUID spoofedId;
    private Property[] spoofedProperties;

    private static final Gson gson = new Gson();

    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"))
    private String bungee$readUtf(FriendlyByteBuf buf, int length) {
        var data = buf.readUtf(Short.MAX_VALUE);
        var chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        var properties = gson.fromJson(chunks[3], Property[].class);

        spoofedId = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProperties = Arrays.stream(properties)
                .filter(packet -> !isFmlMarker(packet))
                .toArray(Property[]::new);

        return Arrays.stream(properties)
                .filter(packet -> isFmlMarker(packet))
                .findFirst()
                .map(property -> chunks[1] + "\0" + property.getValue().split("\u0001")[1] + "\0")
                .orElseGet(() -> chunks[1]);
    }

    private static boolean isFmlMarker(Property property)
    {
        return Objects.equals(property.getName(), "extraData") && property.getValue().startsWith("\u0001FML");
    }

    @Override
    public UUID bungee$getSpoofedId() {
        return spoofedId;
    }

    @Override
    public Property[] bungee$getSpoofedProperties() {
        return spoofedProperties;
    }
}