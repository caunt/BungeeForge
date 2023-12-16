package ua.caunt.bungeeforge.mixin.network.protocol.handshake;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.protocol.handshake.ClientIntentionPacketBridge;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Mixin(net.minecraft.network.protocol.handshake.ClientIntentionPacket.class)
public class ClientIntentionPacket implements ClientIntentionPacketBridge {
    @Unique
    private static String bungee$spoofedAddress;
    @Unique
    private static UUID bungee$spoofedId;
    @Unique
    private static Property[] bungee$spoofedProperties;

    @Unique
    private static final Gson bungee$gson = new Gson();

    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"))
    private static String bungee$readUtf(FriendlyByteBuf buf, int length) {
        var data = buf.readUtf(Short.MAX_VALUE);
        var chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        var properties = bungee$gson.fromJson(chunks[3], Property[].class);

        bungee$spoofedAddress = chunks[1];
        bungee$spoofedId = UUID.fromString(ensureDashesInUuid(chunks[2]));
        bungee$spoofedProperties = Arrays.stream(properties)
                .filter(packet -> !isFmlMarker(packet))
                .toArray(Property[]::new);

        return Arrays.stream(properties)
                .filter(ClientIntentionPacket::isFmlMarker)
                .findFirst()
                .map(property -> chunks[1] + "\0" + property.value().split("\u0001")[1] + "\0")
                .orElseGet(() -> chunks[1]);
    }

    private static boolean isFmlMarker(Property property)
    {
        return Objects.equals(property.name(), "extraData") && property.value().startsWith("\u0001FORGE");
    }

    private static String ensureDashesInUuid(String source) {
        if (source.length() > 32)
            return source;

        var builder = new StringBuilder(source);
        builder.insert(8, "-");
        builder.insert(13, "-");
        builder.insert(18, "-");
        builder.insert(23, "-");

        return builder.toString();
    }

    @Override
    public String bungee$getSpoofedAddress() {
        return bungee$spoofedAddress;
    }

    @Override
    public UUID bungee$getSpoofedId() {
        return bungee$spoofedId;
    }

    @Override
    public Property[] bungee$getSpoofedProperties() {
        return bungee$spoofedProperties;
    }
}