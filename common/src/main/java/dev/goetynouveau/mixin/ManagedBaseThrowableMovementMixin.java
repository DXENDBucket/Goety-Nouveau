package dev.goetynouveau.mixin;

import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only Ars-driven Goety replacements skip base movement; native subclass trails/lifetimes still tick. */
@Mixin(ThrowableProjectile.class)
public abstract class ManagedBaseThrowableMovementMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$movement(CallbackInfo ci) {
        if (!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed()) return;
        var projectile = (Projectile)(Object)this;
        if (!state.goetyNouveau$arsDriven()) return;
        projectile.baseTick();
        if (projectile.level().isClientSide) {
            projectile.setPos(projectile.position().add(projectile.getDeltaMovement()));
            ProjectileUtil.rotateTowardsMovement(projectile, 0.2F);
        }
        ci.cancel();
    }
}
