package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.entities.IOwned;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.illager.raider.*;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.AbstractSkeletonServant;
import com.Polarice3.Goety.common.entities.ally.undead.zombie.ZombieServant;
import com.Polarice3.Goety.common.entities.neutral.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.common.magic.spells.necromancy.*;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.platform.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        int amount=Math.min(remaining,count(focus,spell,caster,staff));
        LivingEntity target=hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l
                && l.isAlive() && l.level()==world && l!=caster && !MobUtil.areAllies(caster,l)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(l) ? l : null;
        boolean added=false;
        for(int i=0;i<amount;i++) {
            var servant=create(focus,world,spell,staff);
            if(servant==null) continue;
            var pos=focus==EffectFocus.VEX ? BlockPos.containing(center).offset(-2+world.random.nextInt(5),1,-2+world.random.nextInt(5))
                    : BlockFinder.SummonRadius(BlockPos.containing(center),servant,world);
            if(!world.hasChunkAt(pos) || pos.getY()<world.getMinBuildHeight() || pos.getY()>=world.getMaxBuildHeight()) { servant.discard(); continue; }
            if((focus==EffectFocus.ZOMBIE || focus==EffectFocus.SKELETON) && !special(spell,staff)) {
                var variant=servant.getVariant(caster instanceof Player p?p:null,world,pos);
                if(variant!=null && variant.create(world) instanceof Owned alternative && spell.summonPredicate().test(alternative)) {
                    servant.discard(); servant=alternative;
                }
            }
            servant.setTrueOwner(owner); servant.moveTo(pos,0,0);
            if(focus!=EffectFocus.VEX && servant.getType()!=ModEntityType.DROWNED_SERVANT.get() && servant.getType()!=ModEntityType.SUNKEN_SKELETON_SERVANT.get()) MobUtil.moveDownToGround(servant);
            if(world.getBlockCollisions(servant,servant.getBoundingBox()).iterator().hasNext()) { servant.discard(); continue; }
            servant.setLimitedLife(MobUtil.getSummonLifespan(world)*duration);
            if(focus!=EffectFocus.VEX) servant.setPersistenceRequired();
            if(servant instanceof AbstractSkeletonServant skeleton) skeleton.setArrowPower(potency);
            NativeSummonHooks.finalizeSpawn(world,servant,servant.blockPosition());
            if(servant instanceof AllyVex vex) { vex.setBoundOrigin(pos); if(potency>0) NativeSummonHooks.sharpen(vex,potency); }
            spell.buffSummon(caster,servant,potency); spell.SummonSap(caster,servant);
            if(target!=null) servant.setTarget(target); else spell.setTarget(caster,servant);
            if(world.addFreshEntity(servant)) {
                added=true; if(focus!=EffectFocus.VEX) spell.uponSummon(world,caster,staff,servant);
                spell.summonAdvancement(caster,servant);
            } else servant.discard();
        }
        if(added) {
            spell.SummonDown(caster);
            if(focus==EffectFocus.VEX) spell.playSound(world,caster,SoundEvents.EVOKER_CAST_SPELL);
            else if(focus==EffectFocus.LEAPING) spell.playSound(world,caster,com.Polarice3.Goety.init.ModSounds.SUMMON_SPELL.get());
            else SoundUtil.playNecromancerSummon(caster);
        }
        return added;
    }
    private static int count(EffectFocus focus,SummonSpell spell,LivingEntity caster,ItemStack staff) {
        if(focus==EffectFocus.LEAPING) return spell.rightStaff(staff) && caster.getRandom().nextBoolean()?2:1;
        if(focus==EffectFocus.VEX) return spell.rightStaff(staff)?3+caster.getRandom().nextInt(3):3;
        if(staff.is(ModItems.NAMELESS_STAFF.get())) return 7;
        return spell.rightStaff(staff)?2+caster.getRandom().nextInt(4):special(spell,staff)?2:1;
    }
    private static boolean special(SummonSpell spell,ItemStack staff) {
        return spell instanceof ZombieSpell z ? z.specialStaffs(staff) : spell instanceof SkeletonSpell s && s.specialStaffs(staff);
    }
    private static Owned create(EffectFocus focus,ServerLevel world,SummonSpell spell,ItemStack staff) {
        if(focus==EffectFocus.VEX) return ModEntityType.VEX_SERVANT.get().create(world);
        if(focus==EffectFocus.LEAPING) return ModEntityType.LEAPLEAF.get().create(world);
        boolean zombie=focus==EffectFocus.ZOMBIE;
        EntityType<? extends Owned> type=zombie?ModEntityType.ZOMBIE_SERVANT.get():ModEntityType.SKELETON_SERVANT.get();
        if(special(spell,staff)) {
            if(spell.typeStaff(staff,SpellType.FROST)) type=zombie?ModEntityType.FROZEN_ZOMBIE_SERVANT.get():ModEntityType.STRAY_SERVANT.get();
            else if(spell.typeStaff(staff,SpellType.STORM)) type=zombie?ModEntityType.FRAYED_SERVANT.get():ModEntityType.RATTLED_SERVANT.get();
            else if(spell.typeStaff(staff,SpellType.WILD)) type=zombie?ModEntityType.JUNGLE_ZOMBIE_SERVANT.get():ModEntityType.MOSSY_SKELETON_SERVANT.get();
            else if(spell.typeStaff(staff,SpellType.NETHER)) type=zombie?ModEntityType.ZPIGLIN_SERVANT.get():ModEntityType.WITHER_SKELETON_SERVANT.get();
            else if(spell.typeStaff(staff,SpellType.ABYSS)) type=zombie?ModEntityType.DROWNED_SERVANT.get():ModEntityType.SUNKEN_SKELETON_SERVANT.get();
            else if(staff.is(ModItems.OMINOUS_STAFF.get())) type=zombie?ModEntityType.ZOMBIE_VINDICATOR_SERVANT.get():ModEntityType.SKELETON_PILLAGER_SERVANT.get();
        }
        return type.create(world);
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
            for(var servant:owned) if(!(focus==EffectFocus.SKELETON && servant instanceof DrownedNecromancer) && ((IOwned)servant).getTrueOwner()==caster && !SEHelper.isGrounded(caster,servant)) { spell.teleportServants(caster,servant); eligible=true; }
            if(!eligible) return false;
        }
        spell.commonResultHit(world,caster); return true;
    }
}
