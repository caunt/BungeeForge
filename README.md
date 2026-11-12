## BungeeForge
BungeeForge is a Forge mod that implements BungeeCord (legacy) player info forwarding: behind BungeeCord or
Velocity, your Forge server sees each player's real IP, UUID and skin.

**One jar for every version**, with no Mixin and no other mods needed. Tested on Forge 1.7.10, 1.8.9, 1.12.2,
1.16.5, 1.18.2, 1.19.2, 1.20.1, 1.20.2, 1.21.1 and 26.3, and on NeoForge 1.21.1 and 26.3.

### Usage
- Put the jar into the `mods/` directory of your server
- Velocity (`velocity.toml`): set `player-info-forwarding-mode` to `legacy`. BungeeCord (`config.yml`): set `ip_forward` to `true`
- Forge server (`server.properties`): set `online-mode` to `false`
- Connect through the proxy

Players who connect to the server directly, without the proxy, are disconnected. Server list pings still work.

> **Firewall the server so that only the proxy can reach it.** Legacy forwarding has no secret: anyone who can
> connect to the server's port can pretend to be a proxy and log in as any player.

### WARN: Minecraft 1.13 +
Forge for Minecraft 1.13 (and newer) is not compatible with proxies due to the lack of a "reset" packet to
gracefully reset client registries between server switches. To implement this functionality, there is an available
plugin called [Ambassador](https://github.com/adde0109/Ambassador) which was thankfully developed by adde0109.
**Install both [BungeeForge](https://github.com/caunt/BungeeForge/releases) and [Ambassador](https://github.com/adde0109/Ambassador/releases)
on 1.13+ setups**. For Minecraft 1.12.2 and lower versions, BungeeForge works by itself.

### How it works
BungeeForge does not patch Minecraft's code, so it does not depend on any version's names:
- It puts a small Netty handler in front of every new connection. The handler reads the handshake packet as raw
  bytes, a format that has not changed since 1.7, takes the forwarded data out of the hostname, and passes
  Minecraft the handshake it expects.
- It gives the connection the player's address and, at login, the player's UUID and skin. It finds the fields
  involved by their type, not their name, and refuses the login if it cannot find exactly one match, so an
  unknown future version fails safely instead of mixing up players.

### Building
`./gradlew build` with JDK 17. The jar ends up in `build/libs/`.

# Void Proxy
Take a look at [**Void Proxy**](https://github.com/caunt/Void) as alternative to BungeeCord and Velocity.
