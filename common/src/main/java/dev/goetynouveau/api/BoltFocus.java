package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** Projectile payload identity; each native entity family has its own flight adapter. */
public enum BoltFocus {
    SOUL_BOLT("soul_bolt", "Soul Bolt"),
    MAGIC_BOLT("magic_bolt", "Magic Bolt"),
    FIREBALL("fireball", "Fireball"),
    POISON_DART("poison_dart", "Poison Dart"),
    WITHER_SKULL("wither_skull", "Wither Skull");

    public final String glyph, name;
    BoltFocus(String glyph, String name) { this.glyph = glyph; this.name = name; }
    public ISpell spell() { return FocusSpells.read(CarrierRegistry.id("goety:" + glyph + "_focus")).orElseThrow(); }
}
