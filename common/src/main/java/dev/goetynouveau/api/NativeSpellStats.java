package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.entity.LivingEntity;

/** Immutable Goety parameters; each execution receives its own mutable native stats. */
public record NativeSpellStats(int potency, int duration, int range, double radius,
                               int burning, float velocity) {
    public static NativeSpellStats defaults(ISpell spell) { return snapshot(spell.defaultStats()); }
    /** Native attribute baseline, before Ars augments and the spell's own focus-enchantment rules.
     * WandUtil.getStats does not apply focus enchantments. Keep those in each native adapter:
     * some spells add them, some halve them, and some replace a parameter entirely. */
    public static NativeSpellStats forCaster(LivingEntity caster, ISpell spell) {
        return snapshot(WandUtil.getStats(caster, spell));
    }
    public static NativeSpellStats snapshot(SpellStat stats) {
        return new NativeSpellStats(stats.getPotency(), stats.getDuration(), stats.getRange(),
                stats.getRadius(), stats.getBurning(), stats.getVelocity());
    }
    public SpellStat nativeCopy() {
        return new SpellStat(potency, duration, range, radius, burning, velocity);
    }
}
