package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.HellCloud;
import com.Polarice3.Goety.common.entities.util.VoidLightningTrap;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.targeting.Allies;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Non-focus native payloads. No surrogate boss, player, damage owner or renderer. */
public final class GoetyBossAbilities {
    private GoetyBossAbilities() {}
    public static boolean live(Entity payload) {
        var source=GoetyAreaProvenance.source(payload,null);
        return source!=null && source.isAlive() && !source.isRemoved() && source.level()==payload.level();
    }
    public static boolean friendly(Entity payload,Entity target) {
        var owner=GoetyAreaProvenance.damageOwner(payload,null);
        return GoetyAreaProvenance.friendly(payload,target)
                || owner!=null && (owner==target || Allies.areAllies(owner,target));
    }
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars) {
        var caster=frame.caster();
        LivingEntity selected=frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living?living:null;
        if(frame.target() instanceof EntityHitResult && (selected==null || !selected.isAlive() || selected.level()!=world))return false;
        var point=selected!=null?selected.position():frame.target().getLocation();
        if(!Double.isFinite(point.lengthSqr()) || !world.isInWorldBounds(BlockPos.containing(point))
                || !world.hasChunkAt(BlockPos.containing(point)))return false;
        int potency=Math.max(0,(int)ars.getAmpMultiplier()+WandUtil.getStats(caster,focus.spell()).getPotency());
        if(WandUtil.enchantedFocus(caster))potency+=WandUtil.getPotencyLevel(caster);
        if(focus==EffectFocus.HYSTERIA) {
            if(selected==null || selected!=caster && !Allies.areAllies(caster,selected))return false;
            int duration=(int)Math.min(72000,900*Math.max(0,1+ars.getDurationMultiplier()));
            if(duration<1)return false;
            var effect=NativeEffectHooks.hysteria(duration,potency);
            var current=selected.getEffect(effect.getEffect());
            if(current!=null && current.getAmplifier()>=potency && current.getDuration()>=duration)return false;
            if(!selected.addEffect(effect,caster))return false;
            world.sendParticles(ParticleTypes.ANGRY_VILLAGER,selected.getX(),selected.getEyeY()+.5,selected.getZ(),8,.3,.3,.3,0);
            selected.playSound(SoundEvents.FIRECHARGE_USE,1,1);
            return true;
        }
        double radius=1+ars.getAoeMultiplier();
        if(!Double.isFinite(radius) || radius<=0)return false;
        var owner=frame.damageOwner()!=null?frame.damageOwner():caster;
        if(focus==EffectFocus.HELL_CLOUD) {
            int life=(int)Math.min(72000,100*Math.max(0,1+ars.getDurationMultiplier()));
            if(life<1)return false;
            var cloud=new HellCloud(com.Polarice3.Goety.common.entities.ModEntityType.HELL_CLOUD.get(),world);
            cloud.setOwner(owner);cloud.setActivateTime(100);
            cloud.setRadius((float)Math.min(32,radius));
            cloud.setExtraDamage(potency);cloud.setLifeSpan(life);
            // One shared placement rule also verifies every covered chunk before querying collisions.
            if(!GoetyWeatherEffects.placeCloud(world,cloud,point))return false;
            GoetyAreaProvenance.mark(cloud,frame);
            if(!world.addFreshEntity(cloud))return false;
            cloud.playSound(com.Polarice3.Goety.init.ModSounds.HEAVY_WOOSH.get(),1,.5f);return true;
        }
        if(focus==EffectFocus.VOID_LIGHTNING_TRAP) {
            var trap=new VoidLightningTrap(world,point.x,point.y,point.z);
            trap.setOwner(owner);trap.setDuration(40);trap.setRadius((float)Math.min(32,radius));
            // Ender Keeper's configured attack damage, rather than an arbitrary second base.
            trap.setDamage(com.Polarice3.Goety.config.AttributesConfig.EnderKeeperDamage.get().floatValue()+potency);
            GoetyAreaProvenance.mark(trap,frame);
            return world.addFreshEntity(trap);
        }
        return false;
    }
}
