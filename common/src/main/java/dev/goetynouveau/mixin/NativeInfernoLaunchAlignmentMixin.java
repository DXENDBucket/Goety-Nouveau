package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.HellBolt;
import dev.goetynouveau.casting.GoetyInfernoAim;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native constructors have the same descriptor on both loaders; no inherited method mapping. */
@Mixin(value=HellBolt.class,remap=false)
public abstract class NativeInfernoLaunchAlignmentMixin {
    @Inject(method="<init>(Lnet/minecraft/world/entity/LivingEntity;DDDLnet/minecraft/world/level/Level;)V",at=@At("TAIL"))
    private void goetyNouveau$aimMouth(LivingEntity shooter,double x,double y,double z,Level world,CallbackInfo ci) {
        GoetyInfernoAim.align(shooter,x,y,z);
    }
}
