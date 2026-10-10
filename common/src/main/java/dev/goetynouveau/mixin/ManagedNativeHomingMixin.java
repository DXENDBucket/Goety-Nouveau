package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.*;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Native homing remains active for ordinary launches. Ars carriers own special trajectory direction. */
@Mixin(value={ElectroOrb.class,SurgingOrb.class},remap=false)
public abstract class ManagedNativeHomingMixin {
    @Unique private Vec3 goetyNouveau$carrierVelocity;
    @Inject(method="tick",remap=true,at=@At("HEAD"))
    private void goetyNouveau$before(CallbackInfo ci) {
        goetyNouveau$carrierVelocity = (Object)this instanceof ManagedBolt state && state.goetyNouveau$managed()
                && state.goetyNouveau$arsDriven() ? ((Projectile)(Object)this).getDeltaMovement() : null;
    }
    @Inject(method="tick",remap=true,at=@At("RETURN"))
    private void goetyNouveau$after(CallbackInfo ci) {
        if (goetyNouveau$carrierVelocity != null) ((Projectile)(Object)this).setDeltaMovement(goetyNouveau$carrierVelocity);
        goetyNouveau$carrierVelocity = null;
    }
}
