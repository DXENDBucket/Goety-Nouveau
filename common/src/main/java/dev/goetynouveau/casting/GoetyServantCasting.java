package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.illager.raider.*;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.AbstractSkeletonServant;
import com.Polarice3.Goety.common.entities.neutral.*;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.EntityHitResult;
import java.util.ArrayList;

/** Shared lifecycle, with explicit native per-focus variants. No integration-owned servant
 * resource pool, expiry timer or command AI. Adapted from the original MIT summon spells. */
public final class GoetyServantCasting {
    private GoetyServantCasting() {}
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SummonSpell spell,
                                NativeSpellStats defaults,SpellStats ars) {
        var caster=frame.caster(); var owner=NativeSummonHooks.owner(caster);
        var owned=new ArrayList<LivingEntity>();
        for(var entity:world.getAllEntities())
            if(entity instanceof LivingEntity living && living.isAlive() && entity instanceof IOwned ownedEntity
                    && owner.getUUID().equals(ownedEntity.getOwnerId()) && spell.summonPredicate().test(living)) owned.add(living);
        if(GoetyResources.sneaking(caster)) return recall(focus,world,caster,spell,owned);
        int remaining=Math.max(0,spell.summonLimit()-owned.size());
        if(remaining==0) return false;
        int potency=Math.max(0,defaults.potency()+(int)ars.getAmpMultiplier());
        int duration=Math.max(0,defaults.duration()+(int)ars.getDurationMultiplier());
        if(WandUtil.enchantedFocus(caster)) {
            potency+=WandUtil.getPotencyLevel(caster);
            duration+=NativeEffectHooks.duration(caster)+NativeSummonHooks.additionalDuration(caster);
        }
        if(duration<=0) return false;
        var hit=frame.target();
        var center=hit instanceof EntityHitResult e ? e.getEntity().position() : hit.getLocation();
        if(!Double.isFinite(center.lengthSqr()) || !world.hasChunkAt(BlockPos.containing(center))) return false;
        var staff=GoetyResources.staff(caster);
        int amount=Math.min(remaining,GoetyServantProfiles.count(focus,spell,caster,staff));
        LivingEntity target=hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l
                && l.isAlive() && l.level()==world && l!=caster && !MobUtil.areAllies(caster,l)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(l) ? l : null;
        boolean added=false;
        SpawnGroupData group=null;
        for(int i=0;i<amount;i++) {
            var servant=GoetyServantProfiles.seed(focus,world);
            if(servant==null) continue;
            var anchor=BlockPos.containing(center);
            // Native candidate searches must not pull additional chunks into an Ars hit.
            int search=focus==EffectFocus.PHANTOM?15:5;
            boolean loaded=true;
            for(int x:new int[]{-search,search}) for(int z:new int[]{-search,search})
                loaded &= world.hasChunkAt(anchor.offset(x,0,z));
            if(!loaded) { servant.discard(); continue; }
            BlockPos pos;
            if(focus==EffectFocus.VEX) pos=anchor.offset(-2+world.random.nextInt(5),1,-2+world.random.nextInt(5));
            else if(focus==EffectFocus.PHANTOM) {
                pos=BlockFinder.SummonFlyingRadius(anchor,servant,world,15);
                int top=Math.min(world.getMaxBuildHeight()-2,anchor.getY()+20+caster.getRandom().nextInt(15));
                while(pos.getY()<top && !world.getBlockState(pos.above()).blocksMotion()) pos=pos.above();
            } else if((focus==EffectFocus.HUNTING || focus==EffectFocus.MAULING) && caster.isUnderWater()) {
                pos=waterPosition(anchor,world);
            }
            else pos=BlockFinder.SummonRadius(anchor,servant,world);
            if(!world.hasChunkAt(pos) || pos.getY()<world.getMinBuildHeight() || pos.getY()>=world.getMaxBuildHeight()) { servant.discard(); continue; }
            servant=GoetyServantProfiles.variant(focus,servant,world,pos,spell,caster,staff);
            servant.setTrueOwner(owner); servant.moveTo(pos,0,0);
            if(GoetyServantProfiles.grounded(focus,servant,caster)) MobUtil.moveDownToGround(servant);
            if(world.getBlockCollisions(servant,servant.getBoundingBox()).iterator().hasNext()) { servant.discard(); continue; }
            if(focus==EffectFocus.PHANTOM) group=NativeSummonHooks.finalizeGroup(world,servant,servant.blockPosition(),group);
            servant.setLimitedLife(MobUtil.getSummonLifespan(world)*duration);
            if(focus!=EffectFocus.VEX && focus!=EffectFocus.PHANTOM) servant.setPersistenceRequired();
            if(servant instanceof AbstractSkeletonServant skeleton) skeleton.setArrowPower(potency);
            if(focus!=EffectFocus.PHANTOM) NativeSummonHooks.finalizeSpawn(world,servant,servant.blockPosition());
            if(focus==EffectFocus.HUNTING) NativeSummonHooks.huntingArmor(servant);
            if(servant instanceof com.Polarice3.Goety.common.entities.ally.undead.PhantomServant phantom && potency>0) {
                phantom.setPhantomSize(potency);
                if(phantom.getBoundingBox().maxY>world.getMaxBuildHeight() || world.getBlockCollisions(phantom,phantom.getBoundingBox()).iterator().hasNext()) { phantom.discard(); continue; }
            }
            if(servant instanceof AllyVex vex) { vex.setBoundOrigin(pos); if(potency>0) NativeSummonHooks.sharpen(vex,potency); }
            if(focus!=EffectFocus.PHANTOM || NativeSummonHooks.buffPhantom()) spell.buffSummon(caster,servant,potency); spell.SummonSap(caster,servant);
            if(target!=null) servant.setTarget(target); else spell.setTarget(caster,servant);
            if(focus==EffectFocus.PHANTOM?world.tryAddFreshEntityWithPassengers(servant):world.addFreshEntity(servant)) {
                added=true; if(focus!=EffectFocus.VEX && focus!=EffectFocus.PHANTOM) spell.uponSummon(world,caster,staff,servant);
                spell.summonAdvancement(caster,servant);
            } else servant.discard();
        }
        if(added) {
            spell.SummonDown(caster);
            if(focus==EffectFocus.VEX) spell.playSound(world,caster,SoundEvents.EVOKER_CAST_SPELL);
            else if(focus==EffectFocus.LEAPING || focus==EffectFocus.HUNTING || focus==EffectFocus.MAULING) spell.playSound(world,caster,com.Polarice3.Goety.init.ModSounds.SUMMON_SPELL.get());
            else SoundUtil.playNecromancerSummon(caster);
        }
        return added;
    }
    /** Native water candidate walk, with per-candidate loading/height guards instead of probing unloaded chunks. */
    private static BlockPos waterPosition(BlockPos anchor,ServerLevel world) {
        var candidate=anchor.mutable();
        for(int i=0;i<16;i++) {
            candidate.move(world.random.nextIntBetweenInclusive(-5,5),world.random.nextIntBetweenInclusive(-2,2),world.random.nextIntBetweenInclusive(-5,5));
            if(candidate.getY()<world.getMinBuildHeight() || candidate.getY()>=world.getMaxBuildHeight() || !world.hasChunkAt(candidate)) continue;
            if(BlockFinder.isEmptyBlock(world,candidate,world.getBlockState(candidate),world.getFluidState(candidate),ModEntityType.ZOMBIE_SERVANT.get(),true)) return candidate.immutable();
        }
        return anchor;
    }
    private static boolean recall(EffectFocus focus,ServerLevel world,LivingEntity caster,SummonSpell spell,ArrayList<LivingEntity> owned) {
        // Native Vex crouch-casting kills its servants; undead crouch-casting recalls them.
        if(focus==EffectFocus.VEX) {
            for(var entity:world.getAllEntities()) if(entity instanceof AllyIrk irk && irk.getTrueOwner()==caster && irk.isAlive()) owned.add(irk);
            owned.removeIf(servant -> ((IOwned)servant).getTrueOwner()!=caster);
            if(owned.isEmpty()) return false;
            for(var servant:owned) { if(servant instanceof AllyIrk) caster.heal(2); servant.kill(); }
        } else {
            if(focus==EffectFocus.ZOMBIE) for(var entity:world.getAllEntities())
                if(entity instanceof DrownedNecromancer necromancer && necromancer.getTrueOwner()==caster && necromancer.isAlive()) owned.add(necromancer);
            boolean eligible=false;
            for(var servant:owned) if(!(focus==EffectFocus.SKELETON && servant instanceof DrownedNecromancer) && ((IOwned)servant).getTrueOwner()==caster && GoetyServantProfiles.recallable(focus,servant,caster) && !SEHelper.isGrounded(caster,servant)) { spell.teleportServants(caster,servant); eligible=true; }
            if(!eligible) return false;
        }
        spell.commonResultHit(world,caster); return true;
    }
}
