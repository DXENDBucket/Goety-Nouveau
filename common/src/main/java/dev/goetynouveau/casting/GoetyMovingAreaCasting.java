package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;

/** One paid native mobile area, launched at the Ars hit; native entities own motion, attacks and expiry. */
public final class GoetyMovingAreaCasting {
    private GoetyMovingAreaCasting() {}
    private static int duration(double ticks,double modifier){double result=ticks*Math.max(0,1+modifier);return !Double.isFinite(result)||result<=0?0:(int)Math.min(Integer.MAX_VALUE,Math.ceil(result));}
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars){
        var caster=frame.caster();if(caster==null || !caster.isAlive() || caster.level()!=world)return false;
        var selected=frame.target() instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l?l:null;
        if(frame.target() instanceof EntityHitResult && (selected==null || !selected.isAlive() || selected.isRemoved() || selected.level()!=world))return false;
        var point=selected!=null?selected.position():frame.target().getLocation();
        if(!Double.isFinite(point.lengthSqr()) || !world.isInWorldBounds(BlockPos.containing(point)) || !world.hasChunkAt(BlockPos.containing(point)))return false;
        var direction=frame.incomingDirection();if(direction.lengthSqr()<1e-10 || focus==EffectFocus.TIDAL && direction.horizontalDistanceSqr()<1e-10)direction=caster.getLookAngle();
        if(!Double.isFinite(direction.lengthSqr()) || direction.lengthSqr()<1e-10)return false;
        direction=direction.normalize();
        var owner=frame.damageOwner()!=null?frame.damageOwner():caster;var spell=(Spell)focus.spell();var staff=GoetyResources.staff(caster);
        var stats=WandUtil.getStats(caster,spell);
        if(focus==EffectFocus.CYCLONE){
            int potency=Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier());double life=stats.getDuration();
            double radius=stats.getRadius()+ars.getAoeMultiplier();float velocity=stats.getVelocity();
            if(spell.rightStaff(staff)){radius+=1;++potency;}
            if(WandUtil.enchantedFocus(caster)){
                potency=(int)Math.min(Integer.MAX_VALUE,(long)potency*(WandUtil.getPotencyLevel(caster)+1L));
                life*=NativeEffectHooks.duration(caster)+1.0;velocity+=NativeEffectHooks.velocity(caster);radius+=NativeEffectHooks.radius(caster)/10.0;
            }
            int lifetime=duration(life,ars.getDurationMultiplier());if(lifetime<=0 || !Double.isFinite(radius) || radius<=0)return false;
            var area=CuriosFinder.hasUnholySet(caster)?new FireTornado(world,owner,direction.x,direction.y,direction.z):new Cyclone(world,owner,direction.x,direction.y,direction.z);
            area.setOwner(owner);if(selected!=null && selected!=owner && !dev.arsconflux.api.targeting.Allies.areAllies(caster,selected) && !dev.arsconflux.api.targeting.Allies.areAllies(owner,selected))area.setTarget(selected);
            area.setDamage(potency);area.setTotalLife(lifetime);area.setBoltSpeed((int)velocity);area.setSize((float)radius);
            var origin=selected!=null?selected.getEyePosition().add(0,-.2,0):point.add(0,.1,0);
            area.setPos(origin.add(direction.scale(.5)));GoetyAreaProvenance.mark(area,frame);
            if(!world.addFreshEntity(area))return false;spell.playSound(world,area,spell.CastingSound(caster));return true;
        }
        if(focus==EffectFocus.TIDAL){
            float potency=Math.max(0,stats.getPotency()+(float)ars.getAmpMultiplier());double extra=stats.getDuration();
            if(WandUtil.enchantedFocus(caster)){potency+=WandUtil.getPotencyLevel(caster)/2f;extra+=NativeEffectHooks.duration(caster);}
            // At an Ars impact the wave is already at its destination, retaining ten active aftermath ticks.
            int lifespan=duration(10+extra,ars.getDurationMultiplier());if(lifespan<=0)return false;
            int spread=spell.rightStaff(staff)?1:0;boolean added=false;
            if(frame.target() instanceof BlockHitResult b)point=point.add(b.getDirection().getStepX()*.01,b.getDirection().getStepY()*.01,b.getDirection().getStepZ()*.01);
            for(int i=-spread;i<=spread;i++){
                var wave=new TidalSurge(world,owner);wave.setPos(point);wave.setLifespan(lifespan);wave.setWaveScale(wave.getWaveScale()+potency/2f);
                wave.setYRot((float)-Math.toDegrees(Math.atan2(direction.x,direction.z))+i*10);GoetyAreaProvenance.mark(wave,frame);added|=world.addFreshEntity(wave);
            }
            if(added)world.playSound(null,point.x,point.y,point.z,SoundEvents.PLAYER_SPLASH_HIGH_SPEED,spell.getSoundSource(),1,1);return added;
        }
        return false;
    }
}
