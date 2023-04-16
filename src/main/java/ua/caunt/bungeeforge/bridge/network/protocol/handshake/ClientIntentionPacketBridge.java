package ua.caunt.bungeeforge.bridge.network.protocol.handshake;

import com.mojang.authlib.properties.Property;

import java.util.UUID;

public interface ClientIntentionPacketBridge {
    UUID bungee$getSpoofedId();
    Property[] bungee$getSpoofedProperties();
}
