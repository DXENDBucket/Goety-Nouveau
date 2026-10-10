package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** Native focus identity and targeting semantics belong to this provider, not Conflux. */
public enum EffectFocus {
    ICE_CHUNK("ice_chunk", "iceology_focus", "Ice Chunk", true, false, false),
    FROST_NOVA("frost_nova", "frost_nova_focus", "Frost Nova", true, true, true),
    ENTANGLING("entangling", "entangling_focus", "Entangling", false, true, true),
    SOUL_HEAL("soul_heal", "soul_heal_focus", "Soul Heal", true, false, true),
    HAUNTED_SKULL("haunted_skull", "skull_focus", "Haunted Skull", true, true, true),
    IRON_HIDE("iron_hide", "iron_hide_focus", "Iron Hide", true, true, false),
    CHILL_HIDE("chill_hide", "chilling_focus", "Chill Hide", true, true, false),
    WEAKENING("weakening", "weakening_focus", "Weakening", true, true, true),
    LIGHTNING("lightning", "lightning_focus", "Lightning", true, false, false),
    UPDRAFT("updraft", "updraft_focus", "Updraft", true, false, true),
    SHOCKWAVE("shockwave", "shockwave_focus", "Shockwave", true, false, true),
    CUSHION("cushion", "cushion_focus", "Cushion", false, true, true),
    FANGS("fangs", "biting_focus", "Fangs", true, false, false),
    IGNITE("ignite", "ignite_focus", "Ignite", false, false, false),
    FEAST("feast", "feast_focus", "Feast", true, false, true);

    public final String glyph, focus, name;
    public final boolean potency, duration, radius;
    EffectFocus(String glyph, String focus, String name, boolean potency, boolean duration, boolean radius) {
        this.glyph = glyph; this.focus = focus; this.name = name;
        this.potency = potency; this.duration = duration; this.radius = radius;
    }
    public ISpell spell() { return FocusSpells.read(CarrierRegistry.id("goety:" + focus)).orElseThrow(); }
}
