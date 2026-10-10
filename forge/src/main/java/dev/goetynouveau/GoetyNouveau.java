package dev.goetynouveau;

import dev.goetynouveau.platform.GoetyConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GoetyNouveau.MOD_ID)
public final class GoetyNouveau {
    public static final String MOD_ID = "goety_nouveau";
    public GoetyNouveau() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GoetyConfig.SPEC, "goety_nouveau-server.toml");
    }
}
