package ua.caunt.bungeeforge.mixin.network.handshake.client;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.IHandshakeNetHandler;
import net.minecraft.network.handshake.ServerHandshakeNetHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.handshake.client.CHandshakePacket.class)
public class CHandshakePacket {
    private UUID spoofedUUID;
    private Property[] spoofedProfile;

    private static final Gson gson = new Gson();
    private static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f\\.:]{0,45}");
    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readUtf(I)Ljava/lang/String;"))
    private String readUtf(PacketBuffer buf, int length) {
        String data = buf.readUtf(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        for (String chunk : chunks)
            System.out.println(chunk);

        if (chunks.length <= 2)
            return data;

        spoofedUUID = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProfile = gson.fromJson(chunks[3], Property[].class);

        return chunks[1]; // + "\0" + FMLNetworkConstants.NETVERSION + "\0";
    }

    @Inject(method = "handle", at = @At("HEAD"))
    private void processPacket(IHandshakeNetHandler handler, CallbackInfo callbackInfo) {
        try {
            if (spoofedUUID == null || spoofedProfile == null)
                return;

            GameProfile gameProfile = new GameProfile(spoofedUUID, "");

            Arrays.stream(spoofedProfile).filter(property -> PROP_PATTERN.matcher(property.getName()).matches()).forEach(property -> {
                gameProfile.getProperties().put(property.getName(), property);
            });

            Field field = ObfuscationReflectionHelper.findField(ServerHandshakeNetHandler.class, "field_147386_b");
            field.setAccessible(true);

            BungeeForge.MAP.put((NetworkManager) field.get(handler), gameProfile);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}