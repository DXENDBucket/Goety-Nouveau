package dev.goetynouveau.platform;

import dev.goetynouveau.api.ProjectileLaunch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** 3.2.0 has no Fireball Malefic Helm geometry branch. Do not import the Forge branch into it. */
public final class NativeProjectileHooks {
    private NativeProjectileHooks() {}
    public static List<ProjectileLaunch> fireball(LivingEntity caster, Vec3 carrierOrigin, Vec3 direction, boolean staff) {
        var result = new ArrayList<ProjectileLaunch>();
        var origin = carrierOrigin.add(direction.x / 2, -0.1, direction.z / 2);
        result.add(new ProjectileLaunch(origin, direction));
        if (staff) for (int i = 0; i < 2; ++i)
            result.add(new ProjectileLaunch(origin.add(caster.getRandom().nextGaussian(), 0, caster.getRandom().nextGaussian()), direction));
        return result;
    }
    public static void speed(AbstractHurtingProjectile projectile, float multiplier) {
        acceleration(projectile, multiplier);
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(multiplier));
    }
    public static void acceleration(AbstractHurtingProjectile projectile, float multiplier) { projectile.accelerationPower *= multiplier; }
}
