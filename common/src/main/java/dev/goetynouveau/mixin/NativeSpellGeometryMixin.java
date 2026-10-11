package dev.goetynouveau.mixin;

import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.magic.Spell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Native mob targets and default player ray tracing, at the real executor's body. */
@Mixin(value=Spell.class,remap=false)
public abstract class NativeSpellGeometryMixin implements ISpell {
    @Override public HitResult rayTrace(Level world, LivingEntity caster, int range, double radius) {
        var host=GoetyNativeGeometry.spatial(this,caster);
        // Preserve virtual entityResult/blockResult overrides outside and inside the scope.
        if(this.entityResult(world,host,range,radius)==null) return this.blockResult(world,host,range);
        return this.entityResult(world,host,range,radius);
    }
    @Redirect(method="useParticle",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/ServerParticleUtil;addParticlesAroundMiddleSelf(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/world/entity/Entity;)V"))
    private void goetyNouveau$stormParticles(net.minecraft.server.level.ServerLevel world,net.minecraft.core.particles.ParticleOptions particle,net.minecraft.world.entity.Entity caster) {
        var spatial=caster instanceof LivingEntity living?GoetyNativeGeometry.spatial(this,living):caster;
        com.Polarice3.Goety.utils.ServerParticleUtil.addParticlesAroundMiddleSelf(world,particle,spatial);
    }
    @Inject(method="isShifting",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$sneak(LivingEntity caster,CallbackInfoReturnable<Boolean> cir) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && caster==view.context().caster())
            cir.setReturnValue(dev.goetynouveau.casting.GoetyResources.sneaking(caster));
    }
    @Inject(method="getTarget(Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/LivingEntity;",
            at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$target(LivingEntity caster,int range,CallbackInfoReturnable<LivingEntity> cir) {
        var spatial=GoetyNativeGeometry.spatial(this,caster);
        if(spatial!=caster) cir.setReturnValue(((Spell)(Object)this).getTarget(spatial,range));
    }
}
