package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** Projectile payload identity; each native entity family has its own flight adapter. */
public enum BoltFocus {
    SOUL_BOLT("soul_bolt", "Soul Bolt"),
    MAGIC_BOLT("magic_bolt", "Magic Bolt"),
    FIREBALL("fireball", "Fireball"),
    POISON_DART("poison_dart", "Poison Dart"),
    WITHER_SKULL("wither_skull", "Wither Skull"),
    ICE_SPIKE("ice_spike", "ice_spike_focus", "Ice Spike"),
    LAVABALL("lavaball", "lavaball_focus", "Lava Bomb"),
    STEAM("steam", "steaming_focus", "Steam Missile"),
    BOUNCY_BUBBLE("bouncy_bubble", "bouncy_bubble_focus", "Bouncy Bubble"),
    ELECTRO_ORB("electro_orb", "electrocute_focus", "Electro Orb"),
    SURGING("surging", "surging_focus", "Surging Orb"),
    SMACK_STONE("smack_stone", "smack_stone_focus", "Smack Stone"),
    BLOSSOM("blossom", "blossoming_focus", "Blossom Ball"),
    MAGMA("magma", "magma_bomb_focus", "Magma Bomb"),
    VOID_BOMB("void_bomb", "void_flash_focus", "Void Bomb"),
    SWORD("sword", "sword_focus", "Magic Sword"),
    RAZOR_WIND("razor_wind", "razor_wind_focus", "Razor Wind"),
    GLOW_LIGHT("glow_light", "glow_light_focus", "Glow Light"),
    SOUL_LIGHT("soul_light", "soul_light_focus", "Soul Light"),
    ICE_STORM("ice_storm", "ice_storm_focus", "Ice Storm"),
    SNARELING_SHOT("snareling_shot", "glyph_snareling_shot", "Snareling Shot"),
    SNAP_FUNGUS("snap_fungus", "glyph_snap_fungus", "Snap Fungus"),
    BLAST_FUNGUS("blast_fungus", "glyph_blast_fungus", "Blast Fungus"),
    BERSERK_FUNGUS("berserk_fungus", "glyph_berserk_fungus", "Berserk Fungus");

    public final String glyph, focus, name;
    BoltFocus(String glyph, String name) { this(glyph, glyph + "_focus", name); }
    BoltFocus(String glyph, String focus, String name) { this.glyph = glyph; this.focus = focus; this.name = name; }
    public boolean lighting() { return this==GLOW_LIGHT || this==SOUL_LIGHT; }
    /** These native callbacks create light/status/continuous fields rather than immediate impact damage. */
    public dev.arsconflux.api.projectile.ImpactTriggers.Policy impactPolicy() {
        return switch(this) {
            case GLOW_LIGHT, SOUL_LIGHT, ICE_STORM, SNARELING_SHOT, SNAP_FUNGUS, BERSERK_FUNGUS ->
                    dev.arsconflux.api.projectile.ImpactTriggers.Policy.ACTIVATION;
            default -> dev.arsconflux.api.projectile.ImpactTriggers.Policy.ENTITY_DAMAGE;
        };
    }
    public boolean additional() { return switch (this) {
        case SOUL_BOLT, MAGIC_BOLT, FIREBALL, POISON_DART, WITHER_SKULL -> false;
        default -> true;
    }; }
    public net.minecraft.resources.ResourceLocation focusId() {
        return CarrierRegistry.id((AbilitySpells.ability(glyph).isPresent()?"goety_nouveau:":"goety:")+focus);
    }
    public ISpell spell() { return FocusSpells.read(focusId()).orElseThrow(); }
}
