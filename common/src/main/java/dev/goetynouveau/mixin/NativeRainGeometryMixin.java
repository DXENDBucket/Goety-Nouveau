package dev.goetynouveau.mixin;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.magic.Spell;
import dev.goetynouveau.casting.GoetyRainGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native mob targets and default player ray tracing, at the real executor's body. */
@Mixin(value=Spell.class,remap=false)
public abstract class NativeRainGeometryMixin implements ISpell {
    @Override public HitResult rayTrace(Level world, LivingEntity caster, int range, double radius) {
        var host=GoetyRainGeometry.spatial(this,caster);
        // Preserve virtual entityResult/blockResult overrides outside and inside the scope.
        if(this.entityResult(world,host,range,radius)==null) return this.blockResult(world,host,range);
        return this.entityResult(world,host,range,radius);
    }
    @Inject(method="isShifting",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$sneak(LivingEntity caster,CallbackInfoReturnable<Boolean> cir) {
        var view=GoetyRainGeometry.current();
        if(view!=null && view.spell()==(Object)this && caster==view.context().caster())
            cir.setReturnValue(dev.goetynouveau.casting.GoetyResources.sneaking(caster));
    }
    @Inject(method="getTarget(Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/LivingEntity;",
            at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$target(LivingEntity caster,int range,CallbackInfoReturnable<LivingEntity> cir) {
        var spatial=GoetyRainGeometry.spatial(this,caster);
        if(spatial!=caster) cir.setReturnValue(((Spell)(Object)this).getTarget(spatial,range));
    }
}
