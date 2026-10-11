package dev.goetynouveau.platform;

import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.utils.ColorUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Preserve the pinned native version's fire particles and mapped ignition API. */
public final class NativeAreaHooks {
    private NativeAreaHooks() {}
    public static SoundEvent explosionSound() { return SoundEvents.GENERIC_EXPLODE; }
    public static void ignite(LivingEntity target, int seconds) { target.setSecondsOnFire(seconds); }
    public static void fireBlastParticles(ServerLevel world, Vec3 point, float radius, boolean staff, ColorUtil color) {
        world.sendParticles(new ShockwaveParticleOption(color.red(),color.green(),color.blue(),radius,1,true),
                point.x,point.y+.5,point.z,1,0,0,0,0);
        int amount = world.random.nextIntBetweenInclusive(4, staff ? 16 : 8);
        for (int i=0; i<amount; i++) {
            var pos = point.add(0,.5,0).offsetRandom(world.random, radius);
            world.sendParticles(com.Polarice3.Goety.client.particles.ModParticleTypes.BIG_FIRE.get(),
                    pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
}
