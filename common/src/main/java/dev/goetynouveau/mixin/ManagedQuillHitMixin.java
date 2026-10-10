package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.PoisonQuill;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.casting.GoetyNativeProjectileCasting;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Quill inherits Projectile.onHit: wrap its full native virtual hit without replacing arrow physics. */
@Mixin(Projectile.class)
public abstract class ManagedQuillHitMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$impact(HitResult hit, CallbackInfo ci) {
        if (!((Object)this instanceof PoisonQuill) || !((Object)this instanceof ManagedBolt state)
                || !state.goetyNouveau$managed() || state.goetyNouveau$handlingHit()) return;
        var projectile = (Projectile)(Object)this;
        if (projectile.level().isClientSide) return;
        GoetyNativeProjectileCasting.nativeImpact(projectile, hit);
        ci.cancel();
    }
}
