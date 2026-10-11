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
    public static boolean temblorSupported(){return false;}
    public static boolean temblor(ServerLevel world,dev.arsconflux.api.context.CastContext frame,com.hollingsworth.arsnouveau.api.spell.SpellStats ars){return false;}
    public static SoundEvent explosionSound() { return SoundEvents.GENERIC_EXPLODE.value(); }
    public static void ignite(LivingEntity target, int seconds) { target.igniteForSeconds(seconds); }
    public static void fireBlastParticles(ServerLevel world, Vec3 point, float radius, boolean staff, ColorUtil color) {
        world.sendParticles(new ShockwaveParticleOption(color.red(),color.green(),color.blue(),radius,1,true),
                point.x,point.y+.5,point.z,0,0,0,0,0);
    }
}
