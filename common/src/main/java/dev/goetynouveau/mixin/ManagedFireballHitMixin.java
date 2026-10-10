package dev.goetynouveau.mixin;

import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.casting.GoetyNativeProjectileCasting;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enter before SmallFireball's virtual entity/block hit and discard, preserving the full call. */
@Mixin(SmallFireball.class)
public abstract class ManagedFireballHitMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$impact(HitResult hit, CallbackInfo ci) {
        if (!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed()
                || state.goetyNouveau$handlingHit()) return;
        var projectile = (Projectile)(Object)this;
        if (projectile.level().isClientSide) return;
        GoetyNativeProjectileCasting.nativeImpact(projectile, hit);
        ci.cancel();
    }
}
