package dev.goetynouveau.casting;

import dev.arsconflux.api.execution.ExecutionAnchor;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

/** Native disposable charging summons retain their own lifespan, explosion and death callbacks.
 * Adapted from Goety's MIT HoggingSpell; this is not the ordinary buffed-servant profile. */
public final class GoetyHoggingCasting {
    private GoetyHoggingCasting() {}
    public static boolean apply(ServerLevel world, CastContext frame, SummonSpell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var caster=frame.caster(); var owner=NativeSummonHooks.owner(caster);
        int remaining=Math.max(0,spell.summonLimit()-GoetyServantCasting.owned(world,owner,spell).size());
        if(remaining==0) return false;
        int duration=Math.max(0,defaults.duration()+(int)ars.getDurationMultiplier());
        double radius=Math.max(0,defaults.radius()+ars.getAoeMultiplier());
        if(WandUtil.enchantedFocus(caster)) {
            duration+=NativeEffectHooks.duration(caster)+NativeSummonHooks.additionalDuration(caster);
            radius+=NativeEffectHooks.radius(caster)*.5;
        }
        if(duration<=0 || !Double.isFinite(radius)) return false;
        var center=frame.target() instanceof EntityHitResult e ? e.getEntity().position() : frame.target().getLocation();
        if(!Double.isFinite(center.lengthSqr())) return false;
        var direction=frame.incomingDirection();
        if(direction==null || !Double.isFinite(direction.lengthSqr()) || direction.horizontalDistanceSqr()<1E-8) direction=ExecutionAnchor.viewDirection(caster);
        direction=new Vec3(direction.x,0,direction.z).normalize();
        if(direction.lengthSqr()<1E-8) direction=new Vec3(0,0,1);
        var side=new Vec3(direction.z,0,-direction.x).scale(2);
        var staff=GoetyResources.staff(caster); boolean paired=spell.rightStaff(staff);
        int count=Math.min(remaining,paired?2:1); boolean added=false;
        LivingEntity target=frame.target() instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l
                && l.isAlive() && l.level()==world && l!=caster && !dev.arsconflux.api.targeting.Allies.areAllies(caster,l)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(l) ? l : null;
        for(int i=0;i<count;i++) {
            var anchor=BlockPos.containing(center.add(side.scale(i==0?1:-1)));
            if(!world.hasChunkAt(anchor) || anchor.getY()<world.getMinBuildHeight() || anchor.getY()>=world.getMaxBuildHeight()) continue;
            var pos=BlockFinder.findGroundBelow(world,anchor);
            if(pos.getY()<world.getMinBuildHeight() || pos.getY()>=world.getMaxBuildHeight()) continue;
            var hoglin=ModEntityType.BURNING_HOGLIN.get().create(world);
            if(hoglin==null) continue;
            hoglin.setTrueOwner(owner); hoglin.setPos(Vec3.atBottomCenterOf(pos));
            if(world.getBlockCollisions(hoglin,hoglin.getBoundingBox()).iterator().hasNext()) { hoglin.discard(); continue; }
            if(paired) hoglin.setWindUpTime(10);
            hoglin.setLimitedLife((int)Math.min(Integer.MAX_VALUE,100L*duration));
            hoglin.setExplosionPower(hoglin.getExplosionPower()+(float)radius);
            NativeSummonHooks.finalizeSpawn(world,hoglin,pos);
            if(target!=null) hoglin.setTarget(target); else spell.setTarget(caster,hoglin);
            if(world.addFreshEntity(hoglin)) {
                spell.uponSummon(world,caster,staff,hoglin);
                if(i==0) spell.summonAdvancement(caster,hoglin);
                added=true;
            } else hoglin.discard();
        }
        if(added) { spell.SummonDown(caster); spell.playSound(world,caster,com.Polarice3.Goety.init.ModSounds.SUMMON_SPELL.get()); }
        return added;
    }
}
