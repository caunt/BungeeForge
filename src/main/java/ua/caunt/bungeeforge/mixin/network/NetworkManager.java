package ua.caunt.bungeeforge.mixin.network;


import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.caunt.bungeeforge.bridge.network.NetworkManagerBridge;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.network.NetworkManager.class)
public class NetworkManager implements NetworkManagerBridge {
    private UUID spoofedId;
    private Property[] spoofedProperties;

    @Override
    public void bungee$setSpoofedId(UUID spoofedId) {
        this.spoofedId = spoofedId;
    }

    @Override
    public void bungee$setSpoofedProperties(Property[] spoofedProperties) {
        this.spoofedProperties = spoofedProperties;
    }

    @Override
    public Optional<UUID> bungee$getSpoofedId() {
        return Optional.ofNullable(spoofedId);
    }

    @Override
    public Optional<Property[]> bungee$getSpoofedProperties() {
        return Optional.ofNullable(spoofedProperties);
    }

    @Override
    public boolean bungee$hasSpoofedProfile() {
        return bungee$getSpoofedId().isPresent() && bungee$getSpoofedProperties().isPresent();
    }
}
