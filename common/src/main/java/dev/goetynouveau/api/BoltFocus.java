package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** Only native SpellHurtingProjectile spells belong to this flight adapter. */
public enum BoltFocus {
    SOUL_BOLT("soul_bolt", "Soul Bolt"),
    MAGIC_BOLT("magic_bolt", "Magic Bolt");

    public final String glyph, name;
    BoltFocus(String glyph, String name) { this.glyph = glyph; this.name = name; }
    public ISpell spell() { return FocusSpells.read(CarrierRegistry.id("goety:" + glyph + "_focus")).orElseThrow(); }
}
