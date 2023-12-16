package ua.caunt.bungeeforge.bridge.network.protocol.handshake;

import com.mojang.authlib.properties.Property;

import java.util.UUID;

public interface ClientIntentionPacketBridge {
    String bungee$getSpoofedAddress();
    UUID bungee$getSpoofedId();
    Property[] bungee$getSpoofedProperties();
}
