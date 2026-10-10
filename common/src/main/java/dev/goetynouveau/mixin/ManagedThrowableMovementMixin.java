package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.SpellThrowableProjectile;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only replace the common movement/collision step; subclasses still tick their trails and lifecycle. */
@Mixin(value=SpellThrowableProjectile.class,remap=false)
public abstract class ManagedThrowableMovementMixin {
    @Inject(method="tick",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$movement(CallbackInfo ci) {
        if(!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed() || !state.goetyNouveau$arsDriven()) return;
        var projectile=(SpellThrowableProjectile)(Object)this; projectile.baseTick();
        if(projectile.level().isClientSide) {
            projectile.setPos(projectile.position().add(projectile.getDeltaMovement()));
            ProjectileUtil.rotateTowardsMovement(projectile,.2F);
        }
        ci.cancel();
    }
}
