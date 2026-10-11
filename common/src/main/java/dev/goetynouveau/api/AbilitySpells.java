package dev.goetynouveau.api;

import com.Polarice3.Goety.common.effects.brew.BrewEffects;
import com.Polarice3.Goety.common.effects.brew.BrewEffect;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.api.magic.SpellType;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.platform.GoetyConfig;
import java.util.*;

/** Goety abilities without native focuses. Only timing/cost metadata is new;
 * execution uses the original projectile/brew behavior at an Ars-selected hit. */
public final class AbilitySpells {
    public enum Ability {
        SNARE_SHOT("snareling_shot", "entangling_focus", null, 20, 60),
        SNARE_TRAP("snareling_trap", "entangling_focus", null, 20, 100),
        SNAP("snap_fungus", "blossoming_focus", null, 20, 100),
        BLAST("blast_fungus", "blossoming_focus", null, 20, 100),
        BERSERK("berserk_fungus", "iron_hide_focus", null, 20, 100),
        BERSERK_CLOUD("berserk_cloud", "iron_hide_focus", null, 30, 160),
        BONE_PULL("bone_pull", "shockwave_focus", null, 30, 160),
        LEAF_SHELL("leaf_shell", null, "leaf_shell", 60, 600),
        THORN_TRAP("thorn_trap", null, "thorn_trap", 40, 400),
        TRANSPOSE("transpose", null, "transpose", 60, 400),
        CORROSION("corrosion", null, "corrosion", 80, 600);
        public final String glyph, reference, brew;
        public final int preparation, cooldown;
        Ability(String glyph,String reference,String brew,int preparation,int cooldown) {
            this.glyph=glyph;this.reference=reference;this.brew=brew;
            this.preparation=preparation;this.cooldown=cooldown;
        }
        public BrewEffect brewEffect() {
            if(brew==null) throw new IllegalStateException("Not a brew: "+glyph);
            return Objects.requireNonNull(BrewEffects.INSTANCE.getBrewEffect("effect.goety."+brew),"Missing Goety brew: "+brew);
        }
    }
    private AbilitySpells() {}
    public static Optional<Ability> ability(String glyph) {
        return Arrays.stream(Ability.values()).filter(a->a.glyph.equals(glyph)).findFirst();
    }
    private static final Map<Ability,Spell> SPELLS=new EnumMap<>(Ability.class);
    static { for(var ability:Ability.values()) SPELLS.put(ability,new Adapter(ability)); }
    public static Optional<Spell> read(String glyph) {return ability(glyph).map(SPELLS::get);}
    private static final class Adapter extends Spell {
        private final Ability ability;
        private Adapter(Ability ability) {this.ability=ability;}
        @Override public int defaultSoulCost() {
            return ability.brew!=null?ability.brewEffect().getSoulCost():
                    FocusSpells.read(CarrierRegistry.id("goety:"+ability.reference)).orElseThrow().defaultSoulCost();
        }
        @Override public int defaultCastDuration() {return GoetyConfig.abilityPreparation(ability.glyph,ability.preparation);}
        @Override public int defaultSpellCooldown() {return GoetyConfig.abilityCooldown(ability.glyph,ability.cooldown);}
        @Override public SpellType getSpellType() {return SpellType.NONE;}
        @Override public SpellStat defaultStats() {return new SpellStat(0,0,16,0,0,.75F);}
    }
}
