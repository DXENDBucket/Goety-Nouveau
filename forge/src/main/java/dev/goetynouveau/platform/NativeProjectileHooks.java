package dev.goetynouveau.platform;

import com.Polarice3.Goety.utils.ItemHelper;
import dev.goetynouveau.api.ProjectileLaunch;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Preserve the original Helm equipment branch, relative to Ars aim rather than caster view. */
public final class NativeProjectileHooks {
    private NativeProjectileHooks() {}
    public static List<ProjectileLaunch> fireball(LivingEntity caster, Vec3 carrierOrigin, Vec3 direction, boolean staff) {
        var result = new ArrayList<ProjectileLaunch>();
        boolean helm = staff && ItemHelper.hasMaleficHelm(caster);
        var origin = helm ? carrierOrigin.add(0, 0.1, 0).add(direction.scale(0.5))
                : carrierOrigin.add(direction.x / 2, -0.1, direction.z / 2);
        result.add(new ProjectileLaunch(origin, helm ? direction.scale(2) : direction));
        if (staff) for (int i = 0; i < 2; ++i) {
            var aim = helm ? direction.yRot((float)Math.toRadians(i == 0 ? -10 : 10)) : direction;
            var side = helm ? carrierOrigin.add(0, 0.1, 0).add(aim.scale(0.5))
                    : origin.add(caster.getRandom().nextGaussian(), 0, caster.getRandom().nextGaussian());
            result.add(new ProjectileLaunch(side, helm ? aim.scale(2) : aim));
        }
        return result;
    }
    public static void speed(AbstractHurtingProjectile projectile, float multiplier) {
        acceleration(projectile, multiplier);
        projectile.setDeltaMovement(projectile.getDeltaMovement().scale(multiplier));
    }
    public static void acceleration(AbstractHurtingProjectile projectile, float multiplier) {
        projectile.xPower *= multiplier; projectile.yPower *= multiplier; projectile.zPower *= multiplier;
    }
}
