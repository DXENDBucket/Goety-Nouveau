package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.ModDamageSource;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=ModDamageSource.class,remap=false)
public abstract class NativeWaterDamageMixin {
    @Inject(method="directDrench",at=@At("RETURN"),cancellable=true)
    private static void goetyNouveau$owner(LivingEntity caster,CallbackInfoReturnable<DamageSource> cir) {
        var view=GoetyNativeGeometry.current();
        if(view==null || view.context().caster()!=caster || view.observer()==null)return;
        var owner=view.context().damageOwner()!=null?view.context().damageOwner():caster;
        cir.setReturnValue(new DamageSource(cir.getReturnValue().typeHolder(),owner,owner));
    }
}
