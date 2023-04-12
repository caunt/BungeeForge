package ua.caunt.bungeeforge;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("bungeeforge")
public class BungeeForge
{
    public static HashMap<NetworkManager, GameProfile> MAP = new HashMap<NetworkManager, GameProfile>();
}
