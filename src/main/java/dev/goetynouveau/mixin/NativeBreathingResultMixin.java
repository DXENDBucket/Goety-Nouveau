package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.FireBreathSpell;
import com.Polarice3.Goety.common.magic.spells.frost.FrostBreathSpell;
import com.Polarice3.Goety.common.magic.spells.wild.SwarmSpell;
import dev.goetynouveau.casting.GoetyBreathGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Geometry changes are scoped to Ars; native spell stats, gear and damage still use the caster. */
@Mixin(value = {FireBreathSpell.class, FrostBreathSpell.class, SwarmSpell.class}, remap = false)
public abstract class NativeBreathingResultMixin {
    @Redirect(method = "SpellResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEyePosition()Lnet/minecraft/world/phys/Vec3;", remap = true), require = 0)
    private Vec3 goetyNouveau$eyes(LivingEntity caster) { return GoetyBreathGeometry.spatial(caster).getEyePosition(); }
    @Redirect(method = "SpellResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;", remap = true), require = 0)
    private Vec3 goetyNouveau$look(LivingEntity caster) { return dev.goetynouveau.casting.GoetyExecutorFacing.look(caster); }
    @Redirect(method = "SpellResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", remap = true), require = 0)
    private double goetyNouveau$x(LivingEntity caster) { return GoetyBreathGeometry.spatial(caster).getX(); }
    @Redirect(method = "SpellResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getY()D", remap = true), require = 0)
    private double goetyNouveau$y(LivingEntity caster) { return GoetyBreathGeometry.spatial(caster).getY(); }
    @Redirect(method = "SpellResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getZ()D", remap = true), require = 0)
    private double goetyNouveau$z(LivingEntity caster) { return GoetyBreathGeometry.spatial(caster).getZ(); }
}
