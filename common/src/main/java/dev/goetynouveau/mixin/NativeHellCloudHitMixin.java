package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.boss.Apostle;
import com.Polarice3.Goety.common.entities.projectiles.HellCloud;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve the native hit result/timing and grant its boss-only curse only to bridge-created clouds. */
@Mixin(value=HellCloud.class,remap=false)
public abstract class NativeHellCloudHitMixin {
    @WrapOperation(method="hurtEntities",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true))
    private boolean goetyNouveau$curse(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original) {
        var cloud=(HellCloud)(Object)this;
        if(GoetyAreaProvenance.marked(cloud) && GoetyBossAbilities.friendly(cloud,target))return false;
        boolean hit=original.call(target,source,amount);
        if(hit && GoetyAreaProvenance.marked(cloud) && !(cloud.getOwner() instanceof Apostle))
            target.addEffect(NativeEffectHooks.burnHex(1200),cloud.getOwner());
        return hit;
    }
}
