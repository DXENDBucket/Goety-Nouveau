package dev.goetynouveau.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;

/** Add native potency before entering Conflux's damage/power pipeline, never after percentage scaling. */
@Mixin({Explosion.class,FireworkRocketEntity.class})
public abstract class NativeBossBonusMixin {
    private static float goetyNouveau$amount(DamageSource damage,float amount) {
        Entity payload=GoetyBossDamageScope.current();
        if(payload==null)payload=damage.getDirectEntity();
        return payload!=null && GoetyAreaProvenance.marked(payload)
                ? amount+payload.getPersistentData().getFloat(GoetyBossDamageScope.BONUS):amount;
    }
    @Group(name="goetyNouveau$bonus",min=1)
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),require=0)
    private boolean goetyNouveau$entity(Entity target,DamageSource source,float amount,Operation<Boolean> original) {
        return original.call(target,source,goetyNouveau$amount(source,amount));
    }
    @Group(name="goetyNouveau$bonus",min=1)
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),require=0)
    private boolean goetyNouveau$living(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original) {
        return original.call(target,source,goetyNouveau$amount(source,amount));
    }
}
