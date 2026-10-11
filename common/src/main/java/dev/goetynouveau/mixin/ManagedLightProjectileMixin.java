package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.LightProjectile;
import dev.goetynouveau.casting.*;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** LightProjectile overrides the arrow tick completely. Special Ars carriers own its motion;
 * retain both native client particle layers instead of running a second collision integrator. */
@Mixin(value=LightProjectile.class,remap=false)
public abstract class ManagedLightProjectileMixin extends net.minecraft.world.entity.projectile.Arrow {
    protected ManagedLightProjectileMixin(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.projectile.Arrow> type, net.minecraft.world.level.Level world) { super(type,world); }
    @Override protected void onHit(HitResult hit) {
        var light=(LightProjectile)(Object)this;
        if((Object)this instanceof ManagedBolt state && state.goetyNouveau$managed() && !state.goetyNouveau$handlingHit() && !light.level().isClientSide) {
            GoetyNativeProjectileCasting.nativeImpact(light,hit); return;
        }
        super.onHit(hit);
    }
    @Inject(method="tick",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$motion(CallbackInfo ci) {
        if(!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed() || !state.goetyNouveau$arsDriven()) return;
        var light=(LightProjectile)(Object)this;
        light.baseTick();
        if(light.level().isClientSide) {
            light.setPos(light.position().add(light.getDeltaMovement()));
            var next=light.position().add(light.getDeltaMovement());
            light.level().addParticle(light.sourceParticle(),light.getX(),light.getY(),light.getZ(),0,0,0);
            light.level().addParticle(light.trailParticle(),next.x+light.level().random.nextDouble()/2,next.y+.5,next.z+light.level().random.nextDouble()/2,0,0,0);
        }
        ci.cancel();
    }
}
