package ua.caunt.bungeeforge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Player info a BungeeCord-compatible proxy (BungeeCord, Velocity "legacy" mode, ...) puts into the handshake
 * hostname: {@code host \0 player-ip \0 uuid [\0 properties-json]}.
 */
final class Forwarding {
    private static final Pattern IPV4 = Pattern.compile("(25[0-5]|2[0-4]\\d|1?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|1?\\d?\\d)){3}");
    // Leading hex digit or ':' plus a ':' somewhere makes the JDK parse it as an IPv6 literal (or fail), never resolve it.
    private static final Pattern IPV6 = Pattern.compile("(?=.*:)[0-9a-fA-F:][0-9a-fA-F:.]*");
    private static final Pattern PROPERTY_NAME = Pattern.compile("\\w{1,16}");

    /** Hostname to hand to Minecraft: the original one, plus Forge's marker if the proxy passed it along. */
    final String host;
    final InetAddress address;
    final UUID id;
    final List<Property> properties;

    private Forwarding(String host, InetAddress address, UUID id, List<Property> properties) {
        this.host = host;
        this.address = address;
        this.id = id;
        this.properties = properties;
    }

    /** Parses forwarded data out of a handshake hostname, or returns null if there is none or it is malformed. */
    static Forwarding parse(String hostname) {
        String[] parts = hostname.split("\0");
        if (parts.length < 3)
            return null;

        InetAddress address = parseAddress(parts[1]);
        UUID id = parseUuid(parts[2]);
        if (address == null || id == null)
            return null;

        String host = parts[0];
        List<Property> properties = Collections.emptyList();
        if (parts.length > 3) {
            String extraData = null;
            boolean forgeClient = false;
            properties = new ArrayList<Property>();
            try {
                for (JsonElement element : new Gson().fromJson(parts[3], JsonArray.class)) {
                    JsonObject object = element.getAsJsonObject();
                    String name = object.get("name").getAsString();
                    String value = object.get("value").getAsString();
                    JsonElement signature = object.get("signature");

                    if (name.equals("extraData"))
                        extraData = value;
                    else if (name.equals("forgeClient"))
                        forgeClient = value.equals("true");
                    else if (PROPERTY_NAME.matcher(name).matches() && !name.equals("modernForgeClient"))
                        properties.add(new Property(name, value, signature == null || signature.isJsonNull() ? null : signature.getAsString()));
                }
            } catch (RuntimeException malformed) {
                return null;
            }

            // Forge's handshake marker (e.g. "\0FML2\0" or "\0FORGE") arrives with \0 swapped for \1.
            if (extraData != null && extraData.startsWith("\1"))
                host += extraData.replace('\1', '\0');
            else if (forgeClient) // Forge 1.7 - 1.12 client
                host += "\0FML\0";
        }

        return new Forwarding(host, address, id, properties);
    }

    /** Only IP literals: InetAddress would otherwise resolve anything that looks like a hostname via DNS. */
    private static InetAddress parseAddress(String text) {
        if (!IPV4.matcher(text).matches() && !IPV6.matcher(text).matches())
            return null;
        try {
            return InetAddress.getByName(text);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /** Accepts both the undashed form BungeeCord sends and the dashed form. */
    private static UUID parseUuid(String text) {
        String hex = text.replace("-", "");
        if (hex.length() != 32)
            return null;
        try {
            return new UUID(Long.parseUnsignedLong(hex.substring(0, 16), 16), Long.parseUnsignedLong(hex.substring(16), 16));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static final class Property {
        final String name;
        final String value;
        final String signature;

        Property(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }
    }
}
