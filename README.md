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
This modification requires both Proxy and Forge to be compatible. Unfortunately, currently, Forge for Minecraft 1.13 (and newer) is not compatible with proxies due to the lack of a "reset" packet to gracefully reset client registries between server switches. To implement this functionality, there is an available plugin called [Ambassador](https://github.com/adde0109/Ambassador) which was thankfully developed by adde0109. **Install both [BungeeForge](https://github.com/caunt/BungeeForge/releases) and [Ambassador](https://github.com/adde0109/Ambassador/releases) on 1.13+ setups**. For Minecraft 1.12.2 and lower versions, BungeeForge works by itself.

## Other versions
Other Forge versions can be supported as well. Please feel free to create a Issue and ask.

# Support This Project

If you find this project helpful and would like to support its development, consider making a donation. Your contribution helps maintain and improve the project.

- **[Donate with PayPal](https://www.paypal.com/donate/?hosted_button_id=BH7PCQXDM8EKN)**: You can use PayPal to make a one-time or recurring donation.

- **Bitcoin**: If you prefer cryptocurrency, you can send Bitcoin to the following address: `bc1qt0j847jw44sfhd9qvhwyxp0hd9nfu7wcfnm9hs`

- **Ethereum**: For Ethereum and ERC20 donations, use the following address: `0x9D9c94bD50DA22a486a6ba6845f3c268DCc8CbE4`

Your support is greatly appreciated!
