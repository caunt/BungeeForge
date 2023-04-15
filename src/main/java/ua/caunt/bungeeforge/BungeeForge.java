package ua.caunt.bungeeforge;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("bungeeforge")
public class BungeeForge
{
    public static HashMap<NetworkManager, Tuple<UUID, Property[]>> MAP = new HashMap<>();
}
