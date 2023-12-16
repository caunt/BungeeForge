package ua.caunt.bungeeforge.bridge.network;

import com.mojang.authlib.properties.Property;

import java.util.Optional;
import java.util.UUID;

public interface ConnectionBridge {
    void bungee$setSpoofedAddress(String spoofedAddress);
    void bungee$setSpoofedId(UUID spoofedId);
    void bungee$setSpoofedProperties(Property[] spoofedProperties);
    Optional<String> bungee$getSpoofedAddress();
    Optional<UUID> bungee$getSpoofedId();
    Optional<Property[]> bungee$getSpoofedProperties();
    boolean bungee$hasSpoofedProfile();
}