package dev.goetynouveau.platform;

import dev.goetynouveau.api.ResourceSource;
import net.minecraftforge.common.ForgeConfigSpec;

public final class GoetyConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<ResourceSource> RESOURCE_SOURCE;
    public static final ForgeConfigSpec.IntValue BURNING_COST, EXTENSION_COST, LIFE_ABSORB_COST, SOUL_EATER_COST;
    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("resources");
        RESOURCE_SOURCE = builder.comment("Resource used by Goety glyph triggers. Native Goety and other Ars glyphs retain their own rules.")
                .defineEnum("source", ResourceSource.SOUL_ENERGY);
        BURNING_COST = builder.comment("Additional trigger cost per Burning augment, before native soul discounts. Uses the selected resource.")
                .defineInRange("burningCostPerUnit", 5, 0, 1000000);
        EXTENSION_COST = builder.comment("Additional trigger cost per Extension augment, before native soul discounts.")
                .defineInRange("extensionCostPerUnit", 5, 0, 1000000);
        LIFE_ABSORB_COST = builder.comment("Additional trigger cost for Life Absorb, before native soul discounts.")
                .defineInRange("lifeAbsorbCost", 5, 0, 1000000);
        SOUL_EATER_COST = builder.comment("Additional trigger cost per Soul Eater augment, before native soul discounts.")
                .defineInRange("soulEaterCostPerUnit", 5, 0, 1000000);
        builder.pop();
        SPEC = builder.build();
    }
    private GoetyConfig() {}
}
