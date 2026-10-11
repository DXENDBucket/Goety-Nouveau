package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.ModDamageSource;
import dev.goetynouveau.casting.GoetyBreathGeometry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Preserve native damage types while separating the breath host from its damage owner. */
@Mixin(value = ModDamageSource.class, remap = false)
public abstract class NativeBreathingDamageMixin {
    @Inject(method = {"fireBreath", "magicFireBreath", "frostBreath", "swarm"}, at = @At("RETURN"), cancellable = true)
    private static void goetyNouveau$owner(Entity direct, Entity caster, CallbackInfoReturnable<DamageSource> cir) {
        var view = GoetyBreathGeometry.current();
        if (view == null || caster != view.context().caster()) return;
        var owner = view.context().damageOwner();
        if (owner == null || owner == caster) return;
        cir.setReturnValue(new DamageSource(cir.getReturnValue().typeHolder(), direct == caster ? owner : direct, owner));
    }
}
