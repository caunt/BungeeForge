package ua.caunt.bungeeforge.mixin.network.handshake.client;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.handshake.client.C00HandshakeBridge;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Mixin(net.minecraft.network.handshake.client.C00Handshake.class)
public class C00Handshake implements C00HandshakeBridge {
    private UUID spoofedId;
    private Property[] spoofedProperties;

    private static final Gson gson = new Gson();

    @Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readString(I)Ljava/lang/String;"))
    private String bungee$readPacketData(PacketBuffer buf, int length) {
        String data = buf.readString(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        Property[] properties = gson.fromJson(chunks[3], Property[].class);

        spoofedId = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProperties = Arrays.stream(properties)
                .filter(packet -> !isFmlMarker(packet))
                .toArray(Property[]::new);

        return Arrays.stream(properties)
                .filter(packet -> isFmlMarker(packet))
                .findFirst()
                .map(property -> chunks[1] + "\0FML\0")
                .orElseGet(() -> chunks[1]);
    }

    private static boolean isFmlMarker(Property property)
    {
        return Objects.equals(property.getName(), "forgeClient") && property.getValue().equals("true");
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