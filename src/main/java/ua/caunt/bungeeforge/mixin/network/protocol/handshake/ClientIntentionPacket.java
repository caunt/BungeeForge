package ua.caunt.bungeeforge.mixin.network.protocol.handshake;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.handshake.ServerHandshakePacketListener;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Tuple;
import net.minecraftforge.network.NetworkConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.protocol.handshake.ClientIntentionPacket.class)
public class ClientIntentionPacket {
    private UUID spoofedUUID;
    private Property[] spoofedProfile;

    private static final Gson gson = new Gson();

    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readUtf(I)Ljava/lang/String;"))
    private String readUtf(FriendlyByteBuf buf, int length) {
        String data = buf.readUtf(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        if (chunks.length <= 2)
            return data;

        spoofedUUID = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProfile = gson.fromJson(chunks[3], Property[].class);

        return chunks[1] + "\0" + NetworkConstants.NETVERSION + "\0";
    }

    @Inject(method = "handle", at = @At("HEAD"))
    private void handle(ServerHandshakePacketListener handler, CallbackInfo callbackInfo) {
        if (spoofedUUID == null || spoofedProfile == null)
            return;

        BungeeForge.MAP.put(handler.getConnection(), new Tuple<>(spoofedUUID, spoofedProfile));
    }
}