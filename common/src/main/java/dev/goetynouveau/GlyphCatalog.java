package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.glyph.SoulBoltGlyph;

/** Spell semantics belong here; carrier integrations are supplied by Conflux. */
public final class GlyphCatalog {
    public static final SoulBoltGlyph SOUL_BOLT = new SoulBoltGlyph();
    private GlyphCatalog() {}
    public static void register() { GlyphRegistry.registerSpell(SOUL_BOLT); }
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
