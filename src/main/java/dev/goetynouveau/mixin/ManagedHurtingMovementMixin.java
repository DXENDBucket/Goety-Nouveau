package dev.goetynouveau.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.platform.NativeProjectileHooks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Predicate;

/** Keep the native tick (fire flag, bubbles, smoke and subclass trails). Only suppress
 * collision and server movement owned by the off-world Ars trajectory. */
@Mixin(AbstractHurtingProjectile.class)
public abstract class ManagedHurtingMovementMixin {
    @Unique private boolean goetyNouveau$clientSpeedConfigured;
    @Unique private static boolean goetyNouveau$driven(Entity entity) {
        return entity instanceof ManagedBolt state && state.goetyNouveau$managed() && state.goetyNouveau$arsDriven();
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void goetyNouveau$nativeSpeed(CallbackInfo ci) {
        var projectile = (AbstractHurtingProjectile)(Object)this;
        if (projectile instanceof ManagedBolt state && state.goetyNouveau$managed()
                && !state.goetyNouveau$arsDriven() && projectile.level().isClientSide && !goetyNouveau$clientSpeedConfigured) {
            NativeProjectileHooks.acceleration(projectile, state.goetyNouveau$speedMultiplier());
            goetyNouveau$clientSpeedConfigured = true;
        }
    }
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;"))
    private HitResult goetyNouveau$collision(Entity entity, Predicate<Entity> predicate, net.minecraft.world.level.ClipContext.Block clip, Operation<HitResult> original) {
        if (goetyNouveau$driven(entity))
            return BlockHitResult.miss(entity.position(), Direction.UP, entity.blockPosition());
        return original.call(entity, predicate, clip);
    }
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void goetyNouveau$velocity(AbstractHurtingProjectile entity, Vec3 velocity, Operation<Void> original) {
        if (!goetyNouveau$driven(entity)) original.call(entity, velocity);
    }
    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/AbstractHurtingProjectile;setPos(DDD)V"))
    private void goetyNouveau$position(AbstractHurtingProjectile entity, double x, double y, double z, Operation<Void> original) {
        if (!goetyNouveau$driven(entity) || entity.level().isClientSide) original.call(entity, x, y, z);
    }
}
