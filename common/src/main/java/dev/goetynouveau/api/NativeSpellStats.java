package dev.goetynouveau.api;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.magic.SpellStat;

/** Immutable Goety parameters; each execution receives its own mutable native stats. */
public record NativeSpellStats(int potency, int duration, int range, double radius,
                               int burning, float velocity) {
    public static NativeSpellStats defaults(ISpell spell) { return snapshot(spell.defaultStats()); }
    public static NativeSpellStats snapshot(SpellStat stats) {
        return new NativeSpellStats(stats.getPotency(), stats.getDuration(), stats.getRange(),
                stats.getRadius(), stats.getBurning(), stats.getVelocity());
    }
    public SpellStat nativeCopy() {
        return new SpellStat(potency, duration, range, radius, burning, velocity);
    }
}
