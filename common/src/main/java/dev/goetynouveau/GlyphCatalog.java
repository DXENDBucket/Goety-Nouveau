package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.glyph.GoetyBoltGlyph;
import dev.goetynouveau.api.BoltFocus;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDurationDown;
import java.util.List;

/** Spell semantics belong here; carrier integrations are supplied by Conflux. */
public final class GlyphCatalog {
    public static final GoetyBoltGlyph SOUL_BOLT = new GoetyBoltGlyph(BoltFocus.SOUL_BOLT);
    public static final GoetyBoltGlyph MAGIC_BOLT = new GoetyBoltGlyph(BoltFocus.MAGIC_BOLT);
    public static final GoetyBoltGlyph FIREBALL = new GoetyBoltGlyph(BoltFocus.FIREBALL);
    public static final GoetyBoltGlyph POISON_DART = new GoetyBoltGlyph(BoltFocus.POISON_DART);
    public static final GoetyBoltGlyph WITHER_SKULL = new GoetyBoltGlyph(BoltFocus.WITHER_SKULL);
    public static final GoetyEffectGlyph ICE_CHUNK = new GoetyEffectGlyph(EffectFocus.ICE_CHUNK);
    public static final GoetyEffectGlyph FROST_NOVA = new GoetyEffectGlyph(EffectFocus.FROST_NOVA);
    public static final GoetyEffectGlyph ENTANGLING = new GoetyEffectGlyph(EffectFocus.ENTANGLING);
    public static final GoetyEffectGlyph SOUL_HEAL = new GoetyEffectGlyph(EffectFocus.SOUL_HEAL);
    public static final GoetyEffectGlyph HAUNTED_SKULL = new GoetyEffectGlyph(EffectFocus.HAUNTED_SKULL);
    public static final GoetyEffectGlyph IRON_HIDE = new GoetyEffectGlyph(EffectFocus.IRON_HIDE);
    public static final GoetyEffectGlyph CHILL_HIDE = new GoetyEffectGlyph(EffectFocus.CHILL_HIDE);
    public static final GoetyEffectGlyph WEAKENING = new GoetyEffectGlyph(EffectFocus.WEAKENING);
    public static final GoetyEffectGlyph LIGHTNING = new GoetyEffectGlyph(EffectFocus.LIGHTNING);
    public static final GoetyEffectGlyph UPDRAFT = new GoetyEffectGlyph(EffectFocus.UPDRAFT);
    public static final GoetyEffectGlyph SHOCKWAVE = new GoetyEffectGlyph(EffectFocus.SHOCKWAVE);
    public static final GoetyEffectGlyph CUSHION = new GoetyEffectGlyph(EffectFocus.CUSHION);
    public static final GoetyEffectGlyph FANGS = new GoetyEffectGlyph(EffectFocus.FANGS);
    public static final GoetyEffectGlyph IGNITE = new GoetyEffectGlyph(EffectFocus.IGNITE);
    public static final GoetyEffectGlyph FEAST = new GoetyEffectGlyph(EffectFocus.FEAST);
    public static final GoetyEffectGlyph BULWARK = new GoetyEffectGlyph(EffectFocus.BULWARK);
    public static final GoetyEffectGlyph DISCHARGE = new GoetyEffectGlyph(EffectFocus.DISCHARGE);
    public static final GoetyEffectGlyph EARTH_FIST = new GoetyEffectGlyph(EffectFocus.EARTH_FIST);
    public static final GoetyEffectGlyph GHOST_FIRE = new GoetyEffectGlyph(EffectFocus.GHOST_FIRE);
    public static final GoetyEffectGlyph LEECHING = new GoetyEffectGlyph(EffectFocus.LEECHING);
    public static final GoetyEffectGlyph LAUNCH = new GoetyEffectGlyph(EffectFocus.LAUNCH);
    public static final GoetyEffectGlyph ERUPTION = new GoetyEffectGlyph(EffectFocus.ERUPTION);
    public static final GoetyEffectGlyph BIO_MINE = new GoetyEffectGlyph(EffectFocus.BIO_MINE);
    public static final GoetyEffectGlyph TEETH = new GoetyEffectGlyph(EffectFocus.TEETH);
    public static final GoetyEffectGlyph THUNDERBOLT = new GoetyEffectGlyph(EffectFocus.THUNDERBOLT);
    public static final GoetyEffectGlyph HAIL = new GoetyEffectGlyph(EffectFocus.HAIL);
    public static final GoetyEffectGlyph MONSOON = new GoetyEffectGlyph(EffectFocus.MONSOON);
    public static final GoetyEffectGlyph FIRE_BLAST = new GoetyEffectGlyph(EffectFocus.FIRE_BLAST);
    public static final GoetyEffectGlyph SONIC_BOOM = new GoetyEffectGlyph(EffectFocus.SONIC_BOOM);
    public static final GoetyEffectGlyph FLAME_STRIKE = new GoetyEffectGlyph(EffectFocus.FLAME_STRIKE);
    public static final GoetyEffectGlyph VOID_RIFT = new GoetyEffectGlyph(EffectFocus.VOID_RIFT);
    public static final GoetyBoltGlyph ICE_SPIKE = new GoetyBoltGlyph(BoltFocus.ICE_SPIKE);
    public static final GoetyBoltGlyph LAVABALL = new GoetyBoltGlyph(BoltFocus.LAVABALL);
    public static final GoetyBoltGlyph STEAM = new GoetyBoltGlyph(BoltFocus.STEAM);
    public static final GoetyBoltGlyph BOUNCY_BUBBLE = new GoetyBoltGlyph(BoltFocus.BOUNCY_BUBBLE);
    public static final GoetyBoltGlyph ELECTRO_ORB = new GoetyBoltGlyph(BoltFocus.ELECTRO_ORB);
    public static final GoetyBoltGlyph SURGING = new GoetyBoltGlyph(BoltFocus.SURGING);
    public static final GoetyBoltGlyph SMACK_STONE = new GoetyBoltGlyph(BoltFocus.SMACK_STONE);
    public static final GoetyBoltGlyph BLOSSOM = new GoetyBoltGlyph(BoltFocus.BLOSSOM);
    public static final GoetyBoltGlyph MAGMA = new GoetyBoltGlyph(BoltFocus.MAGMA);
    public static final GoetyBoltGlyph VOID_BOMB = new GoetyBoltGlyph(BoltFocus.VOID_BOMB);
    public static final GoetyBoltGlyph SWORD = new GoetyBoltGlyph(BoltFocus.SWORD);
    public static final GoetyBoltGlyph RAZOR_WIND = new GoetyBoltGlyph(BoltFocus.RAZOR_WIND);
    public static final GoetyEffectGlyph WIND_BLAST = new GoetyEffectGlyph(EffectFocus.WIND_BLAST);
    public static final GoetyEffectGlyph OVERGROWTH = new GoetyEffectGlyph(EffectFocus.OVERGROWTH);
    public static final GoetyEffectGlyph ZOMBIE = new GoetyEffectGlyph(EffectFocus.ZOMBIE);
    public static final GoetyEffectGlyph SKELETON = new GoetyEffectGlyph(EffectFocus.SKELETON);
    public static final GoetyEffectGlyph VEX = new GoetyEffectGlyph(EffectFocus.VEX);
    public static final GoetyBoltGlyph SOUL_LIGHT = new GoetyBoltGlyph(BoltFocus.SOUL_LIGHT);
    public static final GoetyBoltGlyph GLOW_LIGHT = new GoetyBoltGlyph(BoltFocus.GLOW_LIGHT);
    public static final GoetyEffectGlyph SENSING = new GoetyEffectGlyph(EffectFocus.SENSING);
    public static final GoetyEffectGlyph ROTATION = new GoetyEffectGlyph(EffectFocus.ROTATION);
    public static final GoetyEffectGlyph TREMBLING = new GoetyEffectGlyph(EffectFocus.TREMBLING);
    public static final GoetyEffectGlyph CHARGE = new GoetyEffectGlyph(EffectFocus.CHARGE);
    public static final GoetyEffectGlyph SPIKES = new GoetyEffectGlyph(EffectFocus.SPIKES);
    public static final GoetyEffectGlyph LEAPING = new GoetyEffectGlyph(EffectFocus.LEAPING);
    public static final GoetyEffectGlyph BARRICADE = new GoetyEffectGlyph(EffectFocus.BARRICADE);
    public static final GoetyEffectGlyph HUNTING = new GoetyEffectGlyph(EffectFocus.HUNTING);
    public static final GoetyEffectGlyph MAULING = new GoetyEffectGlyph(EffectFocus.MAULING);
    public static final GoetyEffectGlyph REAPER = new GoetyEffectGlyph(EffectFocus.REAPER);
    public static final GoetyEffectGlyph WRAITH = new GoetyEffectGlyph(EffectFocus.WRAITH);
    public static final GoetyEffectGlyph PHANTOM = new GoetyEffectGlyph(EffectFocus.PHANTOM);
    public static final GoetyEffectGlyph SLIMY = new GoetyEffectGlyph(EffectFocus.SLIMY);
    public static final GoetyEffectGlyph CARRION = new GoetyEffectGlyph(EffectFocus.CARRION);
    public static final GoetyEffectGlyph WHISPER = new GoetyEffectGlyph(EffectFocus.WHISPER);
    public static final GoetyEffectGlyph FROSTBORN = new GoetyEffectGlyph(EffectFocus.FROSTBORN);
    public static final GoetyEffectGlyph SPRITE = new GoetyEffectGlyph(EffectFocus.SPRITE);
    public static final GoetyEffectGlyph VANGUARD = new GoetyEffectGlyph(EffectFocus.VANGUARD);
    public static final GoetyEffectGlyph BLACKGUARD = new GoetyEffectGlyph(EffectFocus.BLACKGUARD);
    public static final GoetyEffectGlyph KILLING = new GoetyEffectGlyph(EffectFocus.KILLING);
    public static final GoetyEffectGlyph BOLTING = new GoetyEffectGlyph(EffectFocus.BOLTING);
    public static final GoetyEffectGlyph ILLUSION = new GoetyEffectGlyph(EffectFocus.ILLUSION);
    public static final GoetyEffectGlyph BLAZING = new GoetyEffectGlyph(EffectFocus.BLAZING);
    public static final GoetyEffectGlyph GHASTLY = new GoetyEffectGlyph(EffectFocus.GHASTLY);
    public static final GoetyEffectGlyph GUARDIAN = new GoetyEffectGlyph(EffectFocus.GUARDIAN);
    public static final GoetyEffectGlyph WATCHING = new GoetyEffectGlyph(EffectFocus.WATCHING);
    public static final GoetyEffectGlyph SNARING = new GoetyEffectGlyph(EffectFocus.SNARING);
    public static final GoetyEffectGlyph BLASTING = new GoetyEffectGlyph(EffectFocus.BLASTING);
    public static final GoetyEffectGlyph FIRE_BREATH = new GoetyEffectGlyph(EffectFocus.FIRE_BREATH);
    public static final GoetyEffectGlyph FROST_BREATH = new GoetyEffectGlyph(EffectFocus.FROST_BREATH);
    public static final GoetyEffectGlyph SWARM = new GoetyEffectGlyph(EffectFocus.SWARM);
    public static final GoetyEffectGlyph MINISTROUS = new GoetyEffectGlyph(EffectFocus.MINISTROUS);
    public static final GoetyEffectGlyph HOGGING = new GoetyEffectGlyph(EffectFocus.HOGGING);
    public static final GoetyEffectGlyph ARROW_RAIN = new GoetyEffectGlyph(EffectFocus.ARROW_RAIN);
    public static final GoetyEffectGlyph BLIZZARD = new GoetyEffectGlyph(EffectFocus.BLIZZARD);
    public static final GoetyEffectGlyph METEOR_SHOWER = new GoetyEffectGlyph(EffectFocus.METEOR_SHOWER);
    public static final GoetyBoltGlyph ICE_STORM = new GoetyBoltGlyph(BoltFocus.ICE_STORM);
    public static final GoetyEffectGlyph BOMBARDMENT = new GoetyEffectGlyph(EffectFocus.BOMBARDMENT);
    public static final GoetyEffectGlyph THUNDERSTORM = new GoetyEffectGlyph(EffectFocus.THUNDERSTORM);
    public static final GoetyEffectGlyph TRIDENT_STORM = new GoetyEffectGlyph(EffectFocus.TRIDENT_STORM);
    public static final GoetyEffectGlyph CORRUPTED_BEAM = new GoetyEffectGlyph(EffectFocus.CORRUPTED_BEAM);
    public static final GoetyEffectGlyph WATER_JET = new GoetyEffectGlyph(EffectFocus.WATER_JET);
    public static final GoetyEffectGlyph PRISMA_BEAM = new GoetyEffectGlyph(EffectFocus.PRISMA_BEAM);
    public static final GoetyEffectGlyph VOID_SHOCK = new GoetyEffectGlyph(EffectFocus.VOID_SHOCK);
    public static final GoetyEffectGlyph QUAKING = new GoetyEffectGlyph(EffectFocus.QUAKING);
    public static final GoetyEffectGlyph WARDING = new GoetyEffectGlyph(EffectFocus.WARDING);
    public static final GoetyEffectGlyph FLYING = new GoetyEffectGlyph(EffectFocus.FLYING);
    public static final GoetyEffectGlyph WHIRLWIND = new GoetyEffectGlyph(EffectFocus.WHIRLWIND);
    public static final GoetyEffectGlyph SHOCKING = new GoetyEffectGlyph(EffectFocus.SHOCKING);
    public static final GoetyEffectGlyph TELEKINESIS = new GoetyEffectGlyph(EffectFocus.TELEKINESIS);
    public static final GoetyEffectGlyph BURROWING = new GoetyEffectGlyph(EffectFocus.BURROWING);
    public static final GoetyEffectGlyph CYCLONE = new GoetyEffectGlyph(EffectFocus.CYCLONE);
    public static final GoetyEffectGlyph BANISH = new GoetyEffectGlyph(EffectFocus.BANISH);
    public static final GoetyEffectGlyph END_WALK = new GoetyEffectGlyph(EffectFocus.END_WALK);
    public static final GoetyEffectGlyph GRAPPLE = new GoetyEffectGlyph(EffectFocus.GRAPPLE);
    public static final GoetyEffectGlyph TUNNEL = new GoetyEffectGlyph(EffectFocus.TUNNEL);
    public static final GoetyEffectGlyph PULVERIZE = new GoetyEffectGlyph(EffectFocus.PULVERIZE);
    public static final GoetyEffectGlyph SCATTER = new GoetyEffectGlyph(EffectFocus.SCATTER);
    public static final GoetyEffectGlyph ILLUMINATE = new GoetyEffectGlyph(EffectFocus.ILLUMINATE);
    public static final GoetyEffectGlyph WATER_WHIP = new GoetyEffectGlyph(EffectFocus.WATER_WHIP);
    public static final GoetyEffectGlyph TIDAL = new GoetyEffectGlyph(EffectFocus.TIDAL);
    public static final GoetyEffectGlyph BREEZE = dev.goetynouveau.platform.NativeSummonHooks.breezeSupported()
            ? new GoetyEffectGlyph(EffectFocus.BREEZE) : null;
    public static final List<AbstractSpellPart> ALL = withNativeExtras(List.of(SOUL_BOLT, MAGIC_BOLT, FIREBALL, POISON_DART, WITHER_SKULL, ICE_CHUNK, FROST_NOVA,
            ENTANGLING, SOUL_HEAL, HAUNTED_SKULL, IRON_HIDE, CHILL_HIDE, WEAKENING, LIGHTNING, UPDRAFT,
            SHOCKWAVE, CUSHION, FANGS, IGNITE, FEAST, BULWARK, DISCHARGE, EARTH_FIST, GHOST_FIRE, LEECHING, LAUNCH, ERUPTION, BIO_MINE, TEETH, THUNDERBOLT, HAIL, MONSOON, FIRE_BLAST, SONIC_BOOM, FLAME_STRIKE, VOID_RIFT, WIND_BLAST, OVERGROWTH, ZOMBIE, SKELETON, VEX, SENSING, ROTATION, TREMBLING, CHARGE, SPIKES, LEAPING, BARRICADE, HUNTING, MAULING, REAPER, WRAITH, PHANTOM, SLIMY, CARRION, WHISPER, FROSTBORN, SPRITE, VANGUARD, BLACKGUARD, KILLING, BOLTING, ILLUSION, BLAZING, GHASTLY, GUARDIAN, WATCHING, SNARING, BLASTING, FIRE_BREATH, FROST_BREATH, SWARM, MINISTROUS, HOGGING, ARROW_RAIN, BLIZZARD, METEOR_SHOWER, BOMBARDMENT, THUNDERSTORM, TRIDENT_STORM, CORRUPTED_BEAM, WATER_JET, PRISMA_BEAM, VOID_SHOCK, QUAKING, WARDING, FLYING, WHIRLWIND, SHOCKING, TELEKINESIS, BURROWING, CYCLONE, TIDAL, WATER_WHIP, ILLUMINATE, PULVERIZE, SCATTER, TUNNEL, GRAPPLE, END_WALK, BANISH, ICE_STORM, GLOW_LIGHT, SOUL_LIGHT, ICE_SPIKE, LAVABALL, STEAM, BOUNCY_BUBBLE, ELECTRO_ORB, SURGING, SMACK_STONE, BLOSSOM, MAGMA, VOID_BOMB, SWORD, RAZOR_WIND, dev.goetynouveau.glyph.AugmentBurning.INSTANCE,
            dev.goetynouveau.glyph.AugmentGoety.EXTENSION, dev.goetynouveau.glyph.AugmentGoety.LIFE_ABSORB,
            dev.goetynouveau.glyph.AugmentGoety.SOUL_EATER));
    private static List<AbstractSpellPart> withNativeExtras(List<AbstractSpellPart> shared) {
        // Do not construct an unavailable glyph: even construction registers its spell school.
        if (BREEZE == null) return shared;
        var parts = new java.util.ArrayList<>(shared);
        parts.add(parts.indexOf(HOGGING) + 1, BREEZE);
        return List.copyOf(parts);
    }
    private GlyphCatalog() {}
    public static void register() {
        // Secondary ordering runs within the provider group, never across providers.
        dev.arsconflux.api.glyph.GlyphPresentation.register("goety_nouveau",
                part -> part instanceof dev.arsconflux.api.projectile.ProjectileForm ? "1" : "0");
        ALL.forEach(GlyphRegistry::registerSpell);
        dev.arsconflux.api.glyph.SpellValidators.register(CarrierRegistry.id("goety_nouveau:augments"),
                dev.goetynouveau.glyph.GoetyAugmentValidation::validate);
    }
    public static GoetyBoltGlyph projectile(BoltFocus focus) {
        return switch (focus) {
            case SOUL_BOLT -> SOUL_BOLT;
            case MAGIC_BOLT -> MAGIC_BOLT;
            case FIREBALL -> FIREBALL;
            case POISON_DART -> POISON_DART;
            case WITHER_SKULL -> WITHER_SKULL;
            case ICE_SPIKE -> ICE_SPIKE;
            case ICE_STORM -> ICE_STORM;
            case LAVABALL -> LAVABALL;
            case STEAM -> STEAM;
            case BOUNCY_BUBBLE -> BOUNCY_BUBBLE;
            case ELECTRO_ORB -> ELECTRO_ORB;
            case SURGING -> SURGING;
            case SMACK_STONE -> SMACK_STONE;
            case BLOSSOM -> BLOSSOM;
            case MAGMA -> MAGMA;
            case VOID_BOMB -> VOID_BOMB;
            case SWORD -> SWORD;
            case RAZOR_WIND -> RAZOR_WIND;
            case GLOW_LIGHT -> GLOW_LIGHT;
            case SOUL_LIGHT -> SOUL_LIGHT;
        };
    }
    public static void configureCarriers() {
        for (var profile : CarrierRegistry.profiles()) {
            var glyph = profile.glyph();
            if (glyph == null) continue;
            profile.rememberOriginalAugments(glyph);
            for (var focus : BoltFocus.values()) glyph.compatibleAugments.add(projectile(focus));
            glyph.compatibleAugments.add(dev.goetynouveau.glyph.AugmentBurning.INSTANCE);
            glyph.compatibleAugments.add(AugmentExtendTime.INSTANCE);
            glyph.compatibleAugments.add(AugmentDurationDown.INSTANCE);
            glyph.compatibleAugments.addAll(profile.augments());
        }
    }
}
