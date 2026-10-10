package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.SlashProjectile;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Slash uses a growing overlap volume, not Projectile.onHitEntity. Retain the actual virtual damage. */
@Mixin(value=SlashProjectile.class,remap=false)
public abstract class ManagedSlashMixin {
    @WrapOperation(method="tick",remap=true,at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/entities/projectiles/SlashProjectile;damageEntity(Lnet/minecraft/world/entity/Entity;)V",remap=false))
    private void goetyNouveau$overlap(SlashProjectile slash,Entity entity,Operation<Void> original) {
        if(slash instanceof ManagedBolt state && state.goetyNouveau$managed() && !state.goetyNouveau$handlingHit())
            GoetyNativeProjectileCasting.nativeImpact(slash,new EntityHitResult(entity));
        else original.call(slash,entity);
    }
    @Inject(method="onHitBlock",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$block(BlockHitResult hit,CallbackInfo ci) {
        if(!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed() || state.goetyNouveau$handlingHit()) return;
        var slash=(SlashProjectile)(Object)this;
        if(slash.level().isClientSide) return;
        GoetyNativeProjectileCasting.nativeImpact(slash,hit); ci.cancel();
    }
    @Inject(method="tick",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$trajectory(CallbackInfo ci) {
        if(!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed() || !state.goetyNouveau$arsDriven()) return;
        var slash=(SlashProjectile)(Object)this; slash.baseTick();
        if(slash.tickCount>slash.getMaxLifeSpan()) { slash.discard(); ci.cancel(); return; }
        slash.oldBB=slash.getBoundingBox(); slash.setRadius(slash.getRadius()+.12F); slash.spawnParticles();
        if(slash.level().isClientSide) {
            slash.setPos(slash.position().add(slash.getDeltaMovement())); ProjectileUtil.rotateTowardsMovement(slash,.2F);
        }
        ci.cancel();
    }
}
