package ua.caunt.bungeeforge;

import com.mojang.authlib.properties.Property;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

@Mod(modid = BungeeForge.MODID, name = ua.caunt.bungeeforge.BungeeForge.NAME, version = ua.caunt.bungeeforge.BungeeForge.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class BungeeForge
{
    public static final String MODID = "bungeeforge";
    public static final String NAME = "BungeeForge Mod";
    public static final String VERSION = "1.0";

    public static HashMap<NetworkManager, Tuple<UUID, Property[]>> MAP = new HashMap<>();
}
