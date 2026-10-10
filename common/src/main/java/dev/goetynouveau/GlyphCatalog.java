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
    public static final List<AbstractSpellPart> ALL = List.of(SOUL_BOLT, MAGIC_BOLT, FIREBALL, POISON_DART, ICE_CHUNK, FROST_NOVA,
            ENTANGLING, SOUL_HEAL, HAUNTED_SKULL, IRON_HIDE, CHILL_HIDE, WEAKENING, LIGHTNING, UPDRAFT,
            SHOCKWAVE, CUSHION, FANGS, IGNITE, dev.goetynouveau.glyph.AugmentBurning.INSTANCE,
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
    public static void configureCarriers() {
        for (var profile : CarrierRegistry.profiles()) {
            var glyph = profile.glyph();
            if (glyph == null) continue;
            profile.rememberOriginalAugments(glyph);
            glyph.compatibleAugments.add(SOUL_BOLT);
            glyph.compatibleAugments.add(MAGIC_BOLT);
            glyph.compatibleAugments.add(FIREBALL);
            glyph.compatibleAugments.add(POISON_DART);
            glyph.compatibleAugments.add(dev.goetynouveau.glyph.AugmentBurning.INSTANCE);
            glyph.compatibleAugments.add(AugmentExtendTime.INSTANCE);
            glyph.compatibleAugments.add(AugmentDurationDown.INSTANCE);
            glyph.compatibleAugments.addAll(profile.augments());
        }
    }
}
