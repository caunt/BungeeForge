# BungeeForge
BungeeForge is a Forge mod that implements BungeeCord (legacy) protocol.
At first it was developed to support Velocity legacy forwarding but it can also work for other Bungee proxies. 

## Usage:
- Download the mod into mods/ directory on your server
- Setup Velocity (velocity.toml) option `player-info-forwarding-mode` to `legacy`
- Configure your Forge server (server.properties) option `online-mode` to `false`
- Connect through Velocity to your Forge server
- You should see skins and IP forwarding works

### WARN: Minecraft 1.13 +
This modification requires both Proxy and Forge to be compatible. Unfortunately, currently, Forge for Minecraft 1.13 (and newer) is not compatible with proxies due to the lack of a "reset" packet to gracefully reset client registries between server switches. To implement this functionality, there is an available plugin called [Ambassador](https://github.com/adde0109/Ambassador) which was thankfully developed by adde0109. **Install both [BungeeForge](https://github.com/caunt/BungeeForge/releases) and [Ambassador](https://github.com/adde0109/Ambassador/releases) on 1.13+ setups**. For Minecraft 1.12.2 and lower versions, BungeeForge works by itself For the Most part Some Versions like 1.7.10 Require another mod to add mixin support.

### NOTE: Minecraft 1.7.10 & 1.12.2
BungeeForge Makes use of mixins, Which are not aviable in forge by itself in 1.7.10 & 1.12.2 as such [Unixmins](https://www.curseforge.com/minecraft/mc-mods/unimixins) is needed on the server,otherwise server will crash seconds after boot.

## Other versions
Other Forge versions can be supported as well. Please feel free to create a Issue and ask.
