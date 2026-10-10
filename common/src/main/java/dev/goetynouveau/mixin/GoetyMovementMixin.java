package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.WaterHurtingProjectile;
import dev.goetynouveau.casting.ManagedBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only bypass native movement/collision; the subclass still animates and emits its own trail. */
@Mixin(value = WaterHurtingProjectile.class, remap = false)
public abstract class GoetyMovementMixin {
    @Inject(method = "tick", remap = true, at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$arsMovement(CallbackInfo ci) {
        if (!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed()) return;
        var entity = (WaterHurtingProjectile)(Object)this;
        if (entity.level().isClientSide) entity.setPos(entity.position().add(entity.getDeltaMovement()));
        ci.cancel();
    }
}
