package dev.goetynouveau.platform;

import dev.goetynouveau.api.ResourceSource;
import net.minecraftforge.common.ForgeConfigSpec;

public final class GoetyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<ResourceSource> RESOURCE_SOURCE;
    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("resources");
        RESOURCE_SOURCE = builder.comment("Resource used by Goety glyph triggers. Native Goety and other Ars glyphs retain their own rules.")
                .defineEnum("source", ResourceSource.SOUL_ENERGY);
        builder.pop();
        SPEC = builder.build();
    }
    private GoetyConfig() {}
}
