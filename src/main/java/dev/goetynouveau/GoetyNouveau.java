package dev.goetynouveau;

import dev.goetynouveau.platform.GoetyConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(GoetyNouveau.MOD_ID)
public final class GoetyNouveau {
    public static final String MOD_ID = "goety_nouveau";
    public GoetyNouveau(ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, GoetyConfig.SPEC, "goety_nouveau-server.toml");
    }
}
