package dev.goetynouveau.platform;

import dev.goetynouveau.api.ResourceSource;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GoetyConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<ResourceSource> RESOURCE_SOURCE;
    public static final ModConfigSpec.IntValue BURNING_COST, EXTENSION_COST, LIFE_ABSORB_COST, SOUL_EATER_COST, UNBOUNDED_CHANNEL_TICKS;
    public static final ModConfigSpec.BooleanValue CHANTING_ENABLED, MOVEMENT_ENABLED, COOLDOWNS_ENABLED, CREATIVE_COOLDOWN;
    static {
        var builder = new ModConfigSpec.Builder();
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
        builder.push("chanting");
        CHANTING_ENABLED = builder.comment("Native Goety preparation on active Ars recipes containing Goety glyphs. Mixed casts prepare once via Conflux.",
                "Includes native casting-speed/gear adjustments. Charging focuses contribute castUp, not item-use lifetime.",
                "Does not reproduce hold-to-charge, magazine cadence or charge-dependent strength.",
                "False restores immediate casting. Hits, Orbit/Echo and passive triggers never re-chant.")
                .define("enabled", true);
        MOVEMENT_ENABLED = builder.comment("Apply native-style player movement limits during Conflux preparations containing Goety glyphs.",
                "Uses Goety fullStopCast: stop movement/jumping when enabled, otherwise 20% walking speed.",
                "Does not affect pure Ars recipes, passengers or native Goety casting.")
                .define("movementRestrictions", true);
        builder.pop();
        builder.push("cooldowns");
        COOLDOWNS_ENABLED = builder.comment("Native focus cooldowns shared with Goety wands and other Ars recipes. Includes native cooldown reduction.",
                "Check before preparation and again on release. An acknowledged release starts each distinct focus cooldown once.",
                "Creative bypasses existing cooldowns and adds none unless creativeCooldown is enabled; resources remain free.",
                "Existing projectiles and repeated triggers continue; custom charge/stop-based cooldown lifecycles are not reproduced.",
                "Automatic devices, passive effects and third-party direct resolvers are unaffected unless explicitly integrated.")
                .define("enabled", true);
        CREATIVE_COOLDOWN = builder.comment("Apply bridge focus cooldowns in creative mode. False bypasses both checks and writes.",
                "True restores the original Goety creative cooldown behavior.")
                .define("creativeCooldown", false);
        builder.pop();
        builder.push("channels");
        UNBOUNDED_CHANNEL_TICKS = builder.comment("Finite base lifetime in ticks for supported held-channel spells whose native duration is infinite (zero).",
                "Used by Corrupted Beam and Water Jet when their native duration is zero. Positive native durations retain their own value. Ars duration augments scale this base.",
                "This does not change native Goety wand casting.")
                .defineInRange("unboundedDurationTicks", 200, 1, 72000);
        builder.pop();
        SPEC = builder.build();
    }
    private GoetyConfig() {}
    private static <T> T read(ModConfigSpec.ConfigValue<T> value) { return SPEC.isLoaded() ? value.get() : value.getDefault(); }
    public static int unboundedChannelTicks() { return read(UNBOUNDED_CHANNEL_TICKS); }
    public static boolean chantingEnabled() { return read(CHANTING_ENABLED); }
    public static boolean movementEnabled() { return read(MOVEMENT_ENABLED); }
    public static boolean creativeCooldown() { return read(CREATIVE_COOLDOWN); }
    public static boolean cooldownsEnabled() { return read(COOLDOWNS_ENABLED); }
}
