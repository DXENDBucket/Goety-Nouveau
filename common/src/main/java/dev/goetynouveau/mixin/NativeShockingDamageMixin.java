package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.common.magic.spells.storm.ShockingSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=ModDamageSource.class,remap=false)
public abstract class NativeShockingDamageMixin {
    @Inject(method="directShock",at=@At("RETURN"),cancellable=true)
    private static void goetyNouveau$owner(LivingEntity caster,CallbackInfoReturnable<DamageSource> cir) {
        var view=GoetyNativeGeometry.current();
        if(view==null || !(view.spell() instanceof ShockingSpell) || view.context().caster()!=caster)return;
        var owner=view.context().damageOwner()!=null?view.context().damageOwner():caster;
        cir.setReturnValue(new DamageSource(cir.getReturnValue().typeHolder(),owner,owner));
    }
}
