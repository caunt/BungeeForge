package ua.caunt.bungeeforge.mixin.network.handshake.client;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.handshake.client.C00HandshakeBridge;

import java.io.IOException;
import java.util.UUID;

@Mixin(net.minecraft.network.handshake.client.C00Handshake.class)
public class C00Handshake implements C00HandshakeBridge {
    private UUID spoofedId;
    private Property[] spoofedProperties;

    private static final Gson gson = new Gson();

    @Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readStringFromBuffer(I)Ljava/lang/String;"))
    private String bungee$readPacketData(PacketBuffer buf, int length) throws IOException {
        String data = buf.readStringFromBuffer(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        spoofedId = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProperties = gson.fromJson(chunks[3], Property[].class);

        return chunks[1];
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