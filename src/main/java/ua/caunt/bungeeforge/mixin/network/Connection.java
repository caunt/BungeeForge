package ua.caunt.bungeeforge.mixin.network;


import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.network.Connection.class)
public class Connection implements ConnectionBridge {
    @Unique
    private String bungee$spoofedAddress;
    @Unique
    private UUID bungee$spoofedId;
    @Unique
    private Property[] bungee$spoofedProperties;
    @Shadow
    private SocketAddress address;

    @Override
    public void bungee$setSpoofedAddress(String spoofedAddress) {
        this.bungee$spoofedAddress = spoofedAddress;
        this.address = new InetSocketAddress(spoofedAddress, 0);
    }

    @Override
    public void bungee$setSpoofedId(UUID spoofedId) {
        this.bungee$spoofedId = spoofedId;
    }

    @Override
    public void bungee$setSpoofedProperties(Property[] spoofedProperties) {
        this.bungee$spoofedProperties = spoofedProperties;
    }

    @Override
    public Optional<String> bungee$getSpoofedAddress() {
        return Optional.ofNullable(bungee$spoofedAddress);
    }

    @Override
    public Optional<UUID> bungee$getSpoofedId() {
        return Optional.ofNullable(bungee$spoofedId);
    }

    @Override
    public Optional<Property[]> bungee$getSpoofedProperties() {
        return Optional.ofNullable(bungee$spoofedProperties);
    }

    @Override
    public boolean bungee$hasSpoofedProfile() {
        return bungee$getSpoofedAddress().isPresent() && bungee$getSpoofedId().isPresent() && bungee$getSpoofedProperties().isPresent();
    }
}
