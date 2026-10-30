package dev.goetynouveau.mixin;

import com.Polarice3.Goety.api.magic.IBreathingSpell;
import com.Polarice3.Goety.common.magic.BreathingSpell;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyBreathGeometry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import java.util.List;
import java.util.function.Predicate;

/** Override inherited interface defaults only; ordinary Goety calls delegate unchanged. */
@Mixin(value = BreathingSpell.class, remap = false)
public abstract class NativeBreathingGeometryMixin implements IBreathingSpell {
    @Override public List<Entity> getBreathTarget(LivingEntity caster, double range) {
        if (GoetyBreathGeometry.current() == null) return GoetyBreathGeometry.nativeDefaults().getBreathTarget(caster, range);
        return getBreathTarget(caster, range, entity -> true);
    }
    @Override public List<Entity> getBreathTarget(LivingEntity caster, double range, Predicate<? super Entity> filter) {
        var view = GoetyBreathGeometry.current();
        if (view == null) return GoetyBreathGeometry.nativeDefaults().getBreathTarget(caster, range, filter);
        var host = GoetyBreathGeometry.spatial(caster);
        return MobUtil.getTargets(host.level(), host, host.getEyePosition(),
                dev.goetynouveau.casting.GoetyExecutorFacing.look(host), range, 3, entity -> entity != caster && entity != host
                && net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                && MobUtil.LIVING_OR_PART.test(entity) && !dev.arsconflux.api.targeting.Allies.areAllies(caster, entity) && filter.test(entity));
    }
    @Override public void breathAttack(ParticleOptions particle, LivingEntity caster, boolean spreadOut,
                                        int count, double velocity, double spread) {
        if(GoetyBreathGeometry.current()==null)GoetyBreathGeometry.nativeDefaults().breathAttack(particle,caster,spreadOut,count,velocity,spread);
        else dev.goetynouveau.casting.GoetyBreathVisuals.breath(particle,GoetyBreathGeometry.spatial(caster),spreadOut,count,velocity,spread);
    }
    @Override public void dragonBreathAttack(ParticleOptions particle, LivingEntity caster, int count,
                                              double velocity, double angle) {
        if(GoetyBreathGeometry.current()==null)GoetyBreathGeometry.nativeDefaults().dragonBreathAttack(particle,caster,count,velocity,angle);
        else dev.goetynouveau.casting.GoetyBreathVisuals.dragon(particle,GoetyBreathGeometry.spatial(caster),count,velocity,angle);
    }
}
