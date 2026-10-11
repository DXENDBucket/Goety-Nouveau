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
        HitResult result=this.entityResult(world,host,range,radius)==null
                ? this.blockResult(world,host,range) : this.entityResult(world,host,range,radius);
        GoetyNativeGeometry.observe(this,caster,result);
        return result;
    }
    @Redirect(method="useParticle",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/ServerParticleUtil;addParticlesAroundMiddleSelf(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/world/entity/Entity;)V"))
    private void goetyNouveau$stormParticles(net.minecraft.server.level.ServerLevel world,net.minecraft.core.particles.ParticleOptions particle,net.minecraft.world.entity.Entity caster) {
        var spatial=caster instanceof LivingEntity living?GoetyNativeGeometry.spatial(this,living):caster;
        com.Polarice3.Goety.utils.ServerParticleUtil.addParticlesAroundMiddleSelf(world,particle,spatial);
    }
    @Inject(method="isShifting",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$sneak(LivingEntity caster,CallbackInfoReturnable<Boolean> cir) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && (caster==view.context().caster() || caster==view.host()))
            cir.setReturnValue(dev.goetynouveau.casting.GoetyResources.sneaking(view.context().caster()));
    }
    @Inject(method="getTarget(Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/LivingEntity;",
            at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$target(LivingEntity caster,int range,CallbackInfoReturnable<LivingEntity> cir) {
        var spatial=GoetyNativeGeometry.spatial(this,caster);
        if(spatial!=caster) cir.setReturnValue(goetyNouveau$observed(caster,((Spell)(Object)this).getTarget(spatial,range)));
    }
    @Inject(method="getTarget(Lnet/minecraft/world/entity/LivingEntity;I)Lnet/minecraft/world/entity/LivingEntity;",
            at=@At("RETURN"),cancellable=true)
    private void goetyNouveau$observeTarget(LivingEntity caster,int range,CallbackInfoReturnable<LivingEntity> cir) {
        cir.setReturnValue(goetyNouveau$observed(caster,cir.getReturnValue()));
    }
    @org.spongepowered.asm.mixin.Unique
    private LivingEntity goetyNouveau$observed(LivingEntity caster,LivingEntity target) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && caster==view.context().caster() && view.observer()!=null) {
            if(target!=null && (target==caster || target==view.host() || target==view.context().damageOwner()
                    || com.Polarice3.Goety.utils.MobUtil.areAllies(caster,target)
                    || !view.host().hasLineOfSight(target))) target=null;
            GoetyNativeGeometry.observe(this,caster,target==null?null:new net.minecraft.world.phys.EntityHitResult(target));
        }
        return target;
    }
}
