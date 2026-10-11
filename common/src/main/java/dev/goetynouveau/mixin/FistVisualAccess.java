package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Native lifetime determines whether a new observer needs the retreat animation too. */
@Mixin(value=EarthFist.class,remap=false)
public interface FistVisualAccess {
    @Accessor("lifeTicks") int goetyNouveau$remainingLife();
}
