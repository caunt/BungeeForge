package ua.caunt.bungeeforge.mixin.network.handshake.client;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.INetHandlerHandshakeServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.BungeeForge;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Pattern;

@Mixin(net.minecraft.network.handshake.client.C00Handshake.class)
public class C00Handshake {
    private UUID spoofedUUID;
    private Property[] spoofedProfile;

    private static final Gson gson = new Gson();
    private static final Pattern HOST_PATTERN = Pattern.compile("[0-9a-f\\.:]{0,45}");
    private static final Pattern PROP_PATTERN = Pattern.compile("\\w{0,16}");

    @Redirect(method = "readPacketData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketBuffer;readString(I)Ljava/lang/String;"))
    private String readString(PacketBuffer buf, int length) {
        String data = buf.readString(Short.MAX_VALUE);
        String[] chunks = data.split("\0");

        for (String chunk : chunks)
            System.out.println("chunk: " + chunk);

        if (chunks.length == 2)
            return data;

        spoofedUUID = UUIDTypeAdapter.fromString(chunks[2]);
        spoofedProfile = gson.fromJson(chunks[3], Property[].class);

        /*
            chunk: 127.0.0.1
            chunk: 127.0.0.1
            chunk: 14d8e92c3384471abd1bb91d18799758
            chunk: [{"name":"textures","value":"ewogICJ0aW1lc3RhbXAiIDogMTY4MTE0NzgxOTkzMSwKICAicHJvZmlsZUlkIiA6ICIxNGQ4ZTkyYzMzODQ0NzFhYmQxYmI5MWQxODc5OTc1OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJjYXVudCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mNTg1YWM1N2VmYzg0YTFhYTJiYmJjZjRhZWQxZTg4YWZkM2FiYWFiNWVkZGM0ZDgzZjUzNDU3NmM2OTY4MWMiCiAgICB9CiAgfQp9","signature":"LZif9MDTf6lfO7jRKVfFL1w/b3y+TlXnErvzS4MFvQOAmiKoF57VH/CVClfduD5GQ66LT+yromaeJLKmQS+S8XEqXVy6B86U1QvV7DEiplwlUe4tLfY6c8Gfstcbq1oFv/lS1zYK1s3bVuKn1pQgaSu6Ig/Uw+uyph2Upim0ffWdq9xG3mx2YaV1I4NcMNBrjqNRVR9I1JLMZhnmi7HM/1kfEY5V8qArVAWltcaOiODnpbdb/Hq8P9U80bRtYsYPzKL/MOBEOwBZC7C7kRxfkVO7nqOA0P/ZQXcbuM8gn4AOUPGclY0HcvqiOBLA7P/CEyHXyuu1QwM9TmixAlzJTgKJ/vEvefwhDqFYCUJVnoV5LQ/pzqfVLU4X4BWwPKx/s1eMfqvBReyqQ+YaoAbf7WAw79Obj4iLK6CtvNhzHNpcJXpCsQpubjVPEYCV1IMqYnUKCbMzgIBlIBaZCvT1m1WaTdtFXSBRuBiEgxUIIj3uQwjfHefyxo8+7gvyZapVVqjYoSiuybTOwhNoKj9CQsvJ8thZ5jSOriCmjAl78avCEE2GB9KfoQbGqdEADOsVop5404VWYZ8X/2iCvby5IEgMS5mmVoPOzwpK4QQ/rNLKbKnTb31OoPVY5j/vKQUwTEun800t7nLrIVJmn2nYxZ1SFNzKmx1EFlt3XqcIF2s\u003d"},{"name":"forgeClient","value":"true","signature":""}]
         */
        return chunks[1] + "\0FML\0";
    }

    @Inject(method = "processPacket", at = @At("HEAD"))
    private void processPacket(INetHandlerHandshakeServer handler, CallbackInfo callbackInfo) {
        try {
            if (spoofedUUID == null || spoofedProfile == null)
                return;

            GameProfile gameProfile = new GameProfile(spoofedUUID, "");

            for (Property property : spoofedProfile) {
                if (!PROP_PATTERN.matcher(property.getName()).matches())
                    continue;

                gameProfile.getProperties().put(property.getName(), property);
            }

            Field field = ObfuscationReflectionHelper.findField(NetHandlerHandshakeTCP.class, "field_147386_b");
            field.setAccessible(true);

            BungeeForge.MAP.put((NetworkManager) field.get(handler), gameProfile);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}