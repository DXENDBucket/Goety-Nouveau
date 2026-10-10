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
    public static final List<AbstractSpellPart> ALL = List.of(SOUL_BOLT, MAGIC_BOLT, FIREBALL, POISON_DART, WITHER_SKULL, ICE_CHUNK, FROST_NOVA,
            ENTANGLING, SOUL_HEAL, HAUNTED_SKULL, IRON_HIDE, CHILL_HIDE, WEAKENING, LIGHTNING, UPDRAFT,
            SHOCKWAVE, CUSHION, FANGS, IGNITE, FEAST, BULWARK, DISCHARGE, EARTH_FIST, GHOST_FIRE, LEECHING, LAUNCH, ERUPTION, BIO_MINE, TEETH, ICE_SPIKE, LAVABALL, STEAM, BOUNCY_BUBBLE, ELECTRO_ORB, SURGING, SMACK_STONE, BLOSSOM, MAGMA, VOID_BOMB, SWORD, RAZOR_WIND, dev.goetynouveau.glyph.AugmentBurning.INSTANCE,
            dev.goetynouveau.glyph.AugmentGoety.EXTENSION, dev.goetynouveau.glyph.AugmentGoety.LIFE_ABSORB,
            dev.goetynouveau.glyph.AugmentGoety.SOUL_EATER);
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
