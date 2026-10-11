package dev.goetynouveau.casting;

import dev.arsconflux.api.context.CastContext;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Supplier;

/** Only the executing breath reads this scope. The caster's body and native gear never move. */
public final class GoetyBreathGeometry {
    public record View(CastContext context, LivingEntity host) {}
    // A plain Java default-method delegate avoids older Forge Mixin rewriting interface-super
    // calls into invalid class Methodrefs. This is stateless geometry, never a fake caster.
    private static final com.Polarice3.Goety.api.magic.IBreathingSpell DEFAULTS = new com.Polarice3.Goety.api.magic.IBreathingSpell() {
        public int Cooldown() { return 0; }
        public int defaultSoulCost() { return 0; }
        public com.Polarice3.Goety.api.magic.SpellType getSpellType() { return com.Polarice3.Goety.api.magic.SpellType.NONE; }
        @SuppressWarnings({"rawtypes", "unchecked"}) public java.util.List acceptedEnchantments() { return java.util.List.of(); }
    };
    public static com.Polarice3.Goety.api.magic.IBreathingSpell nativeDefaults() { return DEFAULTS; }
    private static final ThreadLocal<View> CURRENT = new ThreadLocal<>();
    private GoetyBreathGeometry() {}
    public static View current() { return CURRENT.get(); }
    public static LivingEntity spatial(LivingEntity caster) {
        var view = current();
        return view != null && caster == view.context().caster() ? view.host() : caster;
    }
    public static boolean allied(LivingEntity caster, LivingEntity host) {
        return caster == host || com.Polarice3.Goety.utils.MobUtil.areAllies(caster, host);
    }
    public static <T> T scoped(View view, Supplier<T> action) {
        var previous = CURRENT.get(); CURRENT.set(view);
        try { return action.get(); }
        finally { if (previous == null) CURRENT.remove(); else CURRENT.set(previous); }
    }
}
