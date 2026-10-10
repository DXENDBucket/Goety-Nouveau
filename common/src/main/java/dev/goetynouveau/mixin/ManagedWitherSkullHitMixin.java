package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.ModWitherSkull;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.casting.GoetyNativeProjectileCasting;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The native override owns damage, Wither, kill healing, loot explosions and removal.
 * Intercept that complete call, not the vanilla WitherSkull superclass implementation. */
@Mixin(value = ModWitherSkull.class, remap = false)
public abstract class ManagedWitherSkullHitMixin {
    @Inject(method = "onHit", remap = true, at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$impact(HitResult hit, CallbackInfo ci) {
        var projectile = (ModWitherSkull)(Object)this;
        var state = (ManagedBolt)projectile;
        if (!state.goetyNouveau$managed() || state.goetyNouveau$handlingHit() || projectile.level().isClientSide) return;
        GoetyNativeProjectileCasting.nativeImpact(projectile, hit);
        ci.cancel();
    }
}
