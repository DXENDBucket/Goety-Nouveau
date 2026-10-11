package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.*;
import com.Polarice3.Goety.common.entities.ally.illager.raider.AllyVex;
import com.Polarice3.Goety.common.entities.ally.undead.skeleton.AbstractSkeletonServant;
import com.Polarice3.Goety.common.entities.projectiles.GlowLight;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class UtilitySpellCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-36,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var start=BlockPos.containing(scene(h));
        for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++) for(int y=-1;y<=9;y++)
            h.getLevel().setBlock(start.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-10,0,0)); p.setGameMode(GameType.SURVIVAL); h.getLevel().addNewPlayer(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(point);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200); h.getLevel().addFreshEntity(cow); return cow;
    }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                AbstractAugment... augments) { resolve(h,p,glyph,hit,succeeds,null,augments); }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                LivingEntity owner,AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph); for(var a:augments) spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        resolver.spellContext.nextPart(); int before=SEHelper.getSESouls(p);
        int cost=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null)
                .withDamageOwner(owner==null?p:owner).withIncomingDirection(new Vec3(0,0,1)));
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(succeeds&&!p.isCreative()?cost:0),
                glyph.focus()+" charges once only on a successful trigger: "+before+" -> "+SEHelper.getSESouls(p)+", cost "+cost);
    }
    private static BlockHitResult block(Vec3 point) {
        return new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(25));
    }
    private static void tick(Entity entity,int ticks) {
        for(int i=0;i<ticks&&!entity.isRemoved();i++) { ++entity.tickCount; entity.tick(); }
    }
    private static void clear(GameTestHelper h) {
        entities(h,Owned.class).forEach(Entity::discard); entities(h,com.Polarice3.Goety.common.entities.projectiles.Spike.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    public static void sensingRotationAndTrembling(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);prepare(h,p);
        var enemy=cow(h,scene(h).add(2,0,0));var ally=cow(h,scene(h).add(3,0,0));
        var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("util"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.SENSING,new EntityHitResult(ally),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var sensing=ally.getEffect(NativeEffectHooks.sensing(1,0).getEffect());
            h.assertTrue(sensing!=null && sensing.getAmplifier()>=1 && sensing.getDuration()==120,"Selected ally receives real amplified, extended native Tremor Sense");
            resolve(h,p,GlyphCatalog.SENSING,new EntityHitResult(enemy),false);
            h.assertTrue(!enemy.hasEffect(sensing.getEffect()),"Invalid enemy does not receive beneficial sensing");
            resolve(h,p,GlyphCatalog.SENSING,new EntityHitResult(p),true);
            var pos=BlockPos.containing(scene(h).add(0,0,6));h.getLevel().setBlock(pos,Blocks.OAK_LOG.defaultBlockState(),2);
            var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
            resolve(h,p,GlyphCatalog.ROTATION,hit,true);
            h.assertTrue(h.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)!=Direction.Axis.Y,"Actual selected block rotates");
            h.getLevel().setBlock(pos,Blocks.CHEST.defaultBlockState(),2);resolve(h,p,GlyphCatalog.ROTATION,hit,false);
            SEHelper.setSESouls(p,0);h.getLevel().setBlock(pos,Blocks.OAK_LOG.defaultBlockState(),2);resolve(h,p,GlyphCatalog.ROTATION,hit,false);
            h.assertTrue(h.getLevel().getBlockState(pos).getValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)==Direction.Axis.Y,"Resource failure cannot rotate before payment");
            SEHelper.setSESouls(p,10000);resolve(h,p,GlyphCatalog.TREMBLING,block(scene(h)),true);
            h.assertTrue(enemy.getDeltaMovement().x>0 && enemy.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && enemy.hasEffect(MobEffects.DIG_SLOWDOWN),"Native horn force and both debuffs operate at Ars point");
            h.assertTrue(ally.getDeltaMovement().equals(Vec3.ZERO)&&!ally.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),"Native ally filtering retained");
            p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.TREMBLING,block(scene(h)),true);
            h.succeed();
        } finally {enemy.discard();ally.discard();p.discard();board.removePlayerTeam(team);GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    public static void nativeChargeStackAndChain(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);prepare(h,p);
        var target=cow(h,scene(h));
        var wolf=EntityType.WOLF.create(h.getLevel());wolf.setPos(scene(h).add(0,0,2));wolf.setOwnerUUID(p.getUUID());wolf.setNoAi(true);h.getLevel().addFreshEntity(wolf);
        try {
            resolve(h,p,GlyphCatalog.CHARGE,new EntityHitResult(target),true);
            h.assertTrue(NativeEffectHooks.charged(target)!=null && NativeEffectHooks.charged(target).getAmplifier()==0,"First native electrical charge");
            resolve(h,p,GlyphCatalog.CHARGE,new EntityHitResult(target),true);
            h.assertTrue(NativeEffectHooks.charged(target).getAmplifier()==1,"Second trigger stacks native charge");
            var health=target.getHealth();resolve(h,p,GlyphCatalog.CHARGE,new EntityHitResult(target),true);
            h.assertTrue(target.getHealth()<health,"Overcharging runs native electrical damage");
            resolve(h,p,GlyphCatalog.CHARGE,block(scene(h)),false);
            SEHelper.setSESouls(p,0);var state=NativeEffectHooks.charged(target).getAmplifier();resolve(h,p,GlyphCatalog.CHARGE,new EntityHitResult(target),false);
            h.assertTrue(NativeEffectHooks.charged(target).getAmplifier()==state,"Exhaustion prevents stacking");
            SEHelper.setSESouls(p,10000);target.removeEffect(NativeEffectHooks.charged(target).getEffect());
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
            resolve(h,p,GlyphCatalog.CHARGE,new EntityHitResult(wolf),true);
            h.assertTrue(NativeEffectHooks.charged(target)!=null,"Native matching-staff chain reaches a recipient around the selected owned creature");
            h.succeed();
        }finally {wolf.discard();target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    public static void royalSpikesAndLeapleaf(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var limit=SpellConfig.LeapleafLimit.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.LeapleafLimit.set(2);prepare(h,p);
        var target=cow(h,scene(h));
        try {
            resolve(h,p,GlyphCatalog.SPIKES,block(scene(h)),true,AugmentAmplify.INSTANCE,dev.goetynouveau.glyph.AugmentBurning.INSTANCE);
            var spikes=entities(h,com.Polarice3.Goety.common.entities.projectiles.Spike.class);
            h.assertTrue(!spikes.isEmpty() && spikes.stream().allMatch(s->s.getOwner()==p && s.getExtraDamage()>=1 && s.getBurning()==1),"Native owned spikes preserve potency and burning");
            float health=target.getHealth();for(var spike:spikes)tick(spike,15);
            h.assertTrue(target.getHealth()<health,"Native spike tick really damages selected victim");
            for(var spike:spikes)tick(spike,650);h.assertTrue(spikes.stream().allMatch(Entity::isRemoved),"Native spike lifetime removes real entities");
            clear(h);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.SPIKES,block(scene(h)),true);
            h.assertTrue(entities(h,com.Polarice3.Goety.common.entities.projectiles.Spike.class).size()==13,"Sneak two native spike rings");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.OMINOUS_STAFF.get()));resolve(h,p,GlyphCatalog.SPIKES,block(scene(h)),true);
            h.assertTrue(entities(h,com.Polarice3.Goety.common.entities.projectiles.Spike.class).size()==24,"Matching staff adds native outer ring");
            clear(h);p.setShiftKeyDown(false);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            var nativeLeaf=ModEntityType.LEAPLEAF.get().create(h.getLevel());nativeLeaf.setTrueOwner(p);nativeLeaf.setPos(scene(h).add(5,0,0));h.getLevel().addFreshEntity(nativeLeaf);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));
            resolve(h,p,GlyphCatalog.LEAPING,new EntityHitResult(target),true,AugmentExtendTime.INSTANCE);
            var leaves=entities(h,com.Polarice3.Goety.common.entities.ally.Leapleaf.class);
            h.assertTrue(leaves.size()==2 && leaves.stream().filter(l->l!=nativeLeaf).allMatch(l->l.getTrueOwner()==p && l.getTarget()==target && l.getLifespan()>=1200 && l.getLifespan()<=4760),"Native owner quota clips staff batch; lifetime and selected target retained");
            resolve(h,p,GlyphCatalog.LEAPING,new EntityHitResult(target),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.LEAPING,block(scene(h)),true);
            h.assertTrue(leaves.stream().allMatch(l->l.distanceTo(p)<1),"Leapleaf recall works with only Ars book at full quota");
            clear(h);p.setShiftKeyDown(false);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.LEAPING,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.LEAPING,block(scene(h)),true);
            h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.LeapleafLimit.set(limit);}
    }
}
