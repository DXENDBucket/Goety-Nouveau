package dev.goetynouveau.casting;

import dev.arsconflux.api.context.CastContext;
import net.minecraft.world.entity.LivingEntity;
import java.util.function.Supplier;

/** A synchronous native callback scope; equipment and ownership continue to use the caster. */
public final class GoetyNativeGeometry {
    public record View(CastContext context, LivingEntity host, com.Polarice3.Goety.common.magic.Spell spell,
                       java.util.function.Consumer<net.minecraft.world.phys.HitResult> observer) {
        public View(CastContext context,LivingEntity host,com.Polarice3.Goety.common.magic.Spell spell) {
            this(context,host,spell,null);
        }
    }
    public static void observe(Object spell,LivingEntity caster,net.minecraft.world.phys.HitResult hit) {
        var view=current();
        if(view!=null && view.spell()==spell && view.context().caster()==caster && view.observer()!=null)
            view.observer().accept(hit);
    }
    private static final ThreadLocal<View> CURRENT = new ThreadLocal<>();
    private GoetyNativeGeometry() {}
    public static View current() { return CURRENT.get(); }
    public static LivingEntity spatial(Object spell, LivingEntity caster) {
        var view=current();
        return view!=null && spell==view.spell() && caster==view.context().caster() ? view.host() : caster;
    }
    public static <T> T scoped(View view, Supplier<T> action) {
        var previous=CURRENT.get(); CURRENT.set(view);
        try { return action.get(); }
        finally { if(previous==null) CURRENT.remove(); else CURRENT.set(previous); }
    }
}
