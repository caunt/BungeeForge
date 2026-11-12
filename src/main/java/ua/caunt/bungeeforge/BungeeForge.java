package ua.caunt.bungeeforge;

/**
 * Entry point for every loader generation. Each loader only looks for its own {@code @Mod} annotation,
 * so one class carries all three: Forge 1.7, Forge 1.8+ (1.8 - 1.12 read {@code modid}, 1.13+ read
 * {@code value}) and NeoForge. Version and description come from mcmod.info / mods.toml.
 */
@cpw.mods.fml.common.Mod(modid = BungeeForge.ID, name = BungeeForge.NAME, useMetadata = true, acceptableRemoteVersions = "*")
@net.minecraftforge.fml.common.Mod(value = BungeeForge.ID, modid = BungeeForge.ID, name = BungeeForge.NAME, useMetadata = true, acceptableRemoteVersions = "*", serverSideOnly = true)
@net.neoforged.fml.common.Mod(BungeeForge.ID)
public final class BungeeForge {
    static final String ID = "bungeeforge";
    static final String NAME = "BungeeForge";

    public BungeeForge() {
        Injector.start();
    }
}
