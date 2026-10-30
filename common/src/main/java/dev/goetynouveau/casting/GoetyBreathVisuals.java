package dev.goetynouveau.casting;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Native Goety IBreathingSpell particle geometry with the scoped executor's head view.
 * Adapted from Goety (MIT); see THIRD_PARTY_NOTICES.md. No entity posture is changed. */
public final class GoetyBreathVisuals {
    public static void breath(ParticleOptions particle,LivingEntity host,boolean spreadOut,int count,double velocity,double spread) {
        var look=GoetyExecutorFacing.look(host);
        var mouth=host.getEyePosition().add(look.scale(.9));
        for(int i=0;i<Math.max(1,count);i++) {
            double width=spread+host.getRandom().nextDouble()*(spread/2);
            double speed=velocity+host.getRandom().nextDouble()*velocity;
            var jitter=new Vec3(host.getRandom().nextGaussian()*.0075*width,
                    host.getRandom().nextGaussian()*.0075*width,host.getRandom().nextGaussian()*.0075*width);
            var direction=look.add(jitter).scale(speed);
            var point=spreadOut?mouth.add(host.getRandom().nextGaussian()/2,
                    host.getRandom().nextGaussian()/2,host.getRandom().nextGaussian()/2):mouth;
            emit(host,particle,point,direction);
        }
    }
    public static void dragon(ParticleOptions particle,LivingEntity host,int count,double velocity,double angle) {
        var look=GoetyExecutorFacing.look(host);
        var mouth=host.getEyePosition().add(look.scale(.9));
        double speed=velocity+host.getRandom().nextDouble()*velocity;
        for(int i=0;i<count;i++) {
            var point=mouth.add(host.getRandom().nextDouble()*.3-.15,
                    host.getRandom().nextDouble()*.3-.15,host.getRandom().nextDouble()*.3-.15);
            var jitter=new Vec3(host.getRandom().nextDouble()*2*angle-angle,
                    host.getRandom().nextDouble()*2*angle-angle,host.getRandom().nextDouble()*2*angle-angle).normalize();
            emit(host,particle,point,look.normalize().scale(3).add(jitter).normalize().scale(speed));
        }
    }
    private static void emit(LivingEntity host,ParticleOptions particle,Vec3 point,Vec3 direction) {
        if(host.level() instanceof ServerLevel world)
            world.sendParticles(particle,point.x,point.y,point.z,0,direction.x,direction.y,direction.z,1);
        else host.level().addAlwaysVisibleParticle(particle,point.x,point.y,point.z,direction.x,direction.y,direction.z);
    }
    private GoetyBreathVisuals() {}
}
