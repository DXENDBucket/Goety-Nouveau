package dev.goetynouveau.compat;

import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.glyph.GlyphIcon;
import dev.arsconflux.api.projectile.CarrierRegistry;
import java.util.List;

/** Called only after the loader confirms that the optional Goety Charms dependency is present. */
public final class GoetyCharmsCompat {
    public static void addGlyph(List<AbstractSpellPart> glyphs) {
        glyphs.add(new dev.goetynouveau.compat.charms.ActivateCharmGlyph());
    }

    public static GlyphIcon icon(AbstractSpellPart part) {
        return part.getRegistryName().equals(CarrierRegistry.id("goety_nouveau:glyph_activate_charm"))
                ? GlyphIcon.texture(CarrierRegistry.id("goety_charms:textures/item/summoning_charm.png")) : null;
    }

    private GoetyCharmsCompat() { }
}
