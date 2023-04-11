package ua.caunt.bungeeforge;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

@Mod(modid = BungeeForge.MODID, name = ua.caunt.bungeeforge.BungeeForge.NAME, version = ua.caunt.bungeeforge.BungeeForge.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class BungeeForge
{
    public static final String MODID = "bungeeforge";
    public static final String NAME = "BungeeForge Mod";
    public static final String VERSION = "1.0";

    public static HashMap<NetworkManager, GameProfile> MAP = new HashMap<NetworkManager, GameProfile>();

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) { logger = event.getModLog(); }

    @EventHandler
    public void init(FMLInitializationEvent event) { }
}
