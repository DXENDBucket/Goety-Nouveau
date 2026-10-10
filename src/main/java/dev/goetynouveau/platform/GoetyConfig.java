package dev.goetynouveau.platform;

import dev.goetynouveau.api.ResourceSource;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GoetyConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<ResourceSource> RESOURCE_SOURCE;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("resources");
        RESOURCE_SOURCE = builder.comment("Resource used by Goety glyph triggers. Native Goety and other Ars glyphs retain their own rules.")
                .defineEnum("source", ResourceSource.SOUL_ENERGY);
        builder.pop();
        SPEC = builder.build();
    }
    private GoetyConfig() {}
}
