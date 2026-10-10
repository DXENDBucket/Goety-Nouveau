package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** Native focus identity and targeting semantics belong to this provider, not Conflux. */
public enum EffectFocus {
    ICE_CHUNK("ice_chunk", "iceology_focus", "Ice Chunk", true, false, false),
    FROST_NOVA("frost_nova", "frost_nova_focus", "Frost Nova", true, true, true),
    ENTANGLING("entangling", "entangling_focus", "Entangling", false, true, true),
    SOUL_HEAL("soul_heal", "soul_heal_focus", "Soul Heal", true, false, true),
    HAUNTED_SKULL("haunted_skull", "skull_focus", "Haunted Skull", true, true, true);

    public final String glyph, focus, name;
    public final boolean potency, duration, radius;
    EffectFocus(String glyph, String focus, String name, boolean potency, boolean duration, boolean radius) {
        this.glyph = glyph; this.focus = focus; this.name = name;
        this.potency = potency; this.duration = duration; this.radius = radius;
    }
    public ISpell spell() { return FocusSpells.read(CarrierRegistry.id("goety:" + focus)).orElseThrow(); }
}
