package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.glyph.SoulBoltGlyph;
import java.util.List;

/** Spell semantics belong here; carrier integrations are supplied by Conflux. */
public final class GlyphCatalog {
    public static final SoulBoltGlyph SOUL_BOLT = new SoulBoltGlyph();
    public static final GoetyEffectGlyph ICE_CHUNK = new GoetyEffectGlyph(EffectFocus.ICE_CHUNK);
    public static final GoetyEffectGlyph FROST_NOVA = new GoetyEffectGlyph(EffectFocus.FROST_NOVA);
    public static final GoetyEffectGlyph ENTANGLING = new GoetyEffectGlyph(EffectFocus.ENTANGLING);
    public static final GoetyEffectGlyph SOUL_HEAL = new GoetyEffectGlyph(EffectFocus.SOUL_HEAL);
    public static final GoetyEffectGlyph HAUNTED_SKULL = new GoetyEffectGlyph(EffectFocus.HAUNTED_SKULL);
    public static final List<AbstractSpellPart> ALL = List.of(SOUL_BOLT, ICE_CHUNK, FROST_NOVA, ENTANGLING, SOUL_HEAL, HAUNTED_SKULL);
    private GlyphCatalog() {}
    public static void register() {
        dev.arsconflux.api.glyph.GlyphPresentation.register("goety_nouveau");
        ALL.forEach(GlyphRegistry::registerSpell);
    }
    public static void configureCarriers() {
        for (var profile : CarrierRegistry.profiles()) {
            var glyph = profile.glyph();
            if (glyph == null) continue;
            profile.rememberOriginalAugments(glyph);
            glyph.compatibleAugments.add(SOUL_BOLT);
            glyph.compatibleAugments.addAll(profile.augments());
        }
    }
}
