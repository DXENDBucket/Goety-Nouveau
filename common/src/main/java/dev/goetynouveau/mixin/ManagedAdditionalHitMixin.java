package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Enter before each native override, so both its virtual entity hit and secondary effects run once. */
@Mixin(value={Lavaball.class,HellBlast.class,ElectroOrb.class,BlossomBall.class,MagmaBomb.class,VoidShockBomb.class,SmackStone.class,SnarelingShot.class,SnapFungus.class,BlastFungus.class,BerserkFungus.class},remap=false)
public abstract class ManagedAdditionalHitMixin {
    @Inject(method="onHit",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$impact(HitResult hit,CallbackInfo ci) {
        if(!((Object)this instanceof ManagedBolt state) || !state.goetyNouveau$managed() || state.goetyNouveau$handlingHit()) return;
        var projectile=(Projectile)(Object)this;
        if(projectile.level().isClientSide) return;
        GoetyNativeProjectileCasting.nativeImpact(projectile,hit); ci.cancel();
    }
}
