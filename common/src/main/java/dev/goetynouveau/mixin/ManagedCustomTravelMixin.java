package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.*;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native subclasses override WaterHurtingProjectile.travel; preserve each integrator. */
@Mixin(value={BouncyBubble.class,SurgingOrb.class},remap=false)
public abstract class ManagedCustomTravelMixin {
    @Unique private Vec3 goetyNouveau$travelStart;
    // Run the original integrator in native velocity units, then scale its displacement and
    // resulting velocity. The default factor is exactly one; rotation and drag remain native.
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$beforeTravel(CallbackInfo ci) {
        goetyNouveau$travelStart = null;
        if ((Object)this instanceof ManagedBolt state && state.goetyNouveau$managed() && state.goetyNouveau$arsDriven()) {
            var bolt = (WaterHurtingProjectile)(Object)this;
            if (bolt.level().isClientSide) {
                bolt.setPos(bolt.position().add(bolt.getDeltaMovement()));
                ProjectileUtil.rotateTowardsMovement(bolt, 0.2F);
            }
            ci.cancel(); return;
        }
        if (!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed()
                || state.goetyNouveau$arsDriven() || state.goetyNouveau$speedMultiplier() == 1.0F) return;
        var bolt = (WaterHurtingProjectile)(Object)this;
        goetyNouveau$travelStart = bolt.position();
        bolt.setDeltaMovement(bolt.getDeltaMovement().scale(1.0 / state.goetyNouveau$speedMultiplier()));
    }
    @Inject(method = "travel", at = @At("TAIL"))
    private void goetyNouveau$afterTravel(CallbackInfo ci) {
        if (goetyNouveau$travelStart == null) return;
        var bolt = (WaterHurtingProjectile)(Object)this;
        float scale = ((ManagedBolt)bolt).goetyNouveau$speedMultiplier();
        bolt.setPos(goetyNouveau$travelStart.add(bolt.position().subtract(goetyNouveau$travelStart).scale(scale)));
        bolt.setDeltaMovement(bolt.getDeltaMovement().scale(scale));
        goetyNouveau$travelStart = null;
    }
}
