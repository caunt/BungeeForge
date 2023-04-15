package ua.caunt.bungeeforge.mixin.network.handshake.client;

import com.google.gson.Gson;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.lang.reflect.Field;
import java.util.UUID;

@Mixin(net.minecraft.network.handshake.client.C00Handshake.class)
public class C00Handshake {
    private UUID spoofedUUID;
    private Property[] spoofedProfile;

    private static final Gson gson = new Gson();

    @Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readString(I)Ljava/lang/String;"))
    private String readString(PacketBuffer buf, int length) {
        String data = buf.readString(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        spoofedUUID = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProfile = gson.fromJson(chunks[3], Property[].class);

        return chunks[1] + "\0FML\0";
    }

    @Inject(method = "processPacket", at = @At("HEAD"))
    private void processPacket(INetHandlerHandshakeServer handler, CallbackInfo callbackInfo) {
        try {
            if (spoofedUUID == null || spoofedProfile == null)
                return;

            Field field = ObfuscationReflectionHelper.findField(NetHandlerHandshakeTCP.class, "field_147386_b");
            field.setAccessible(true);

            BungeeForge.MAP.put((NetworkManager) field.get(handler), new Tuple<>(spoofedUUID, spoofedProfile));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}