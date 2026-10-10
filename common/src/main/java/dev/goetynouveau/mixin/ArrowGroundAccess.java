package dev.goetynouveau.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractArrow.class)
public interface ArrowGroundAccess {
    @Accessor("inGround") void goetyNouveau$inGround(boolean value);
}
