package ua.caunt.bungeeforge.mixin.network;


import com.mojang.authlib.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ua.caunt.bungeeforge.bridge.network.ConnectionBridge;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;
import java.util.UUID;

@Mixin(value = net.minecraft.network.Connection.class)
public class Connection implements ConnectionBridge {
    private String spoofedAddress;
    private UUID spoofedId;
    private Property[] spoofedProperties;

    @Override
    public void bungee$setSpoofedAddress(String spoofedAddress) { this.spoofedAddress = spoofedAddress; }

    @Override
    public void bungee$setSpoofedId(UUID spoofedId) {
        this.spoofedId = spoofedId;
    }

    @Override
    public void bungee$setSpoofedProperties(Property[] spoofedProperties) { this.spoofedProperties = spoofedProperties; }

    @Override
    public Optional<String> bungee$getSpoofedAddress() { return Optional.ofNullable(spoofedAddress); }

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
        return bungee$getSpoofedAddress().isPresent() && bungee$getSpoofedId().isPresent() && bungee$getSpoofedProperties().isPresent();
    }

    @Inject(method = "getRemoteAddress", at = @At("HEAD"), cancellable = true)
    public void bungee$handleIntention(CallbackInfoReturnable<SocketAddress> cir) {
        bungee$getSpoofedAddress().ifPresent(s -> cir.setReturnValue(new InetSocketAddress(s, 0)));
    }
}
