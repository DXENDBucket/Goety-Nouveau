package dev.goetynouveau.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Virtual invocation preserves the complete native Goety impact implementation. */
@Mixin(Projectile.class)
public interface ProjectileHitAccess {
    @Invoker("onHit") void goetyNouveau$hit(HitResult hit);
    @Invoker("canHitEntity") boolean goetyNouveau$canHit(Entity entity);
}
