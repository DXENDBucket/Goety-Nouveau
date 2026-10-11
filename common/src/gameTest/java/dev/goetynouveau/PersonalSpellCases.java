package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
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

public final class PersonalSpellCases {
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
        resolver.spellContext.nextPart(); int before=SEHelper.getSESouls(p);float healthBefore=p.getHealth();
        int cost=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null)
                .withDamageOwner(owner==null?p:owner).withIncomingDirection(new Vec3(0,0,1)));
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(succeeds&&!p.isCreative()?cost:0),
                glyph.focus()+" charges once only on a successful trigger: "+before+" -> "+SEHelper.getSESouls(p)+", cost "+cost+", caster health "+healthBefore+" -> "+p.getHealth()+", pvp "+h.getLevel().getServer().isPvpAllowed());
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
        entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class).forEach(Entity::discard);
    }
    private static void reset(ServerPlayer p,float health) {
        p.invulnerableTime=0; p.hurtTime=0; p.setHealth(health);
    }
    private static void curios(ServerPlayer p,net.minecraft.world.item.Item... items) {
        var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);
        int next=0;
        for(var handler:inventory.getCurios().values()) for(int i=0;i<handler.getStacks().getSlots();i++)
            handler.getStacks().setStackInSlot(i,next<items.length?new ItemStack(items[next++]):ItemStack.EMPTY);
        if(next!=items.length) throw new IllegalStateException("Not enough loaded Curios slots");
    }
    public static void killingFeedbackTargetAndResources(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); var feedback=SpellConfig.KillingFeedback.get();var pvp=h.getLevel().getServer().isPvpAllowed();h.getLevel().getServer().setPvpAllowed(true);
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.KillingFeedback.set(.5);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6)); var owner=cow(h,scene(h).add(8,0,6));
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("curse"+UUID.randomUUID().toString().substring(0,8));
        try {
            p.setYRot(180);target.setHealth(10);reset(p,20);
            h.assertTrue(p.canHarmPlayer(p) && !p.isInvulnerableTo(ModDamageSource.deathCurse(p)),"Real mortal player fixture permits native self-attributed feedback");
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),true,owner);
            h.assertTrue(!target.isAlive() && Math.abs(p.getHealth()-15)<.01 && target.getLastHurtByMob()==owner,
                    "Ars target outside caster view receives actual death curse; caster pays feedback, explicit owner gets attribution");
            target.discard();target=cow(h,scene(h).add(0,0,6));target.setHealth(100);reset(p,4);
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),true);
            h.assertTrue(Math.abs(p.getHealth()-1)<.01 && Math.abs(target.getHealth()-97)<.01,"Native low-health feedback clamp reduces both damage values");
            reset(p,20);p.setInvulnerable(true);target.invulnerableTime=0;
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),false);
            h.assertTrue(target.getHealth()==97 && p.getHealth()==20,"Survival feedback rejection prevents target damage and billing");
            p.setInvulnerable(false);target.setInvulnerable(true);target.setHealth(10);reset(p,20);
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),true);
            h.assertTrue(target.getHealth()==10 && p.getHealth()==15,"Real feedback still charges when target rejects damage");
            target.setInvulnerable(false);reset(p,20);SEHelper.setSESouls(p,0);
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),false);
            h.assertTrue(p.getHealth()==20 && target.getHealth()==10,"Resource exhaustion precedes either health cost");
            SEHelper.setSESouls(p,10000);board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(target.getScoreboardName(),team);
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),false);
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(p),false);resolve(h,p,GlyphCatalog.KILLING,block(scene(h)),false);
            board.removePlayerFromTeam(target.getScoreboardName(),team);p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);reset(p,20);
            // Use a separate damage owner: native player-attributed kills grant soul energy,
            // which would mix kill rewards into the creative no-payment assertion.
            resolve(h,p,GlyphCatalog.KILLING,new EntityHitResult(target),true,owner);
            h.assertTrue(!target.isAlive() && p.getHealth()==20,"Creative death curse is free and bypasses feedback immunity");h.succeed();
        }finally { target.discard();owner.discard();p.discard();board.removePlayerTeam(team);GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.KillingFeedback.set(feedback);h.getLevel().getServer().setPvpAllowed(pvp); }
    }
    public static void illusionRecastMetadataAndLifecycle(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6)); var other=cow(h,scene(h).add(8,0,6));
        try {
            resolve(h,p,GlyphCatalog.ILLUSION,new EntityHitResult(target),true,other);
            var clones=entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class);
            h.assertTrue(clones.size()==4 && clones.stream().allMatch(e->e.getTrueOwner()==p && e.getOwnerId().equals(p.getUUID())
                    && e.getOwnerClientId()==p.getId() && e.getTarget()==target && e.getLifespan()==1200 && e.getMainHandItem().is(net.minecraft.world.item.Items.BOW)),
                    "Four native clones retain caster ownership, synchronized owner identity, selected enemy, lifetime and bow");
            var first=clones.get(0);var replica=com.Polarice3.Goety.common.entities.ModEntityType.DOPPELGANGER.get().create(h.getLevel());
            replica.getEntityData().assignValues(first.getEntityData().getNonDefaultValues());
            h.assertTrue(replica.getOwnerId().equals(p.getUUID()) && replica.getOwnerClientId()==p.getId(),"Spawn/tracking metadata carries true owner to other observers");replica.discard();
            p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),false);
            h.assertTrue(clones.size()==entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class).size(),"Native ten-tick grace protects fresh clones from dismiss");
            p.setShiftKeyDown(false);clones.forEach(e->e.tickCount=11);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));
            resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),true);
            h.assertTrue(clones.stream().allMatch(Entity::isRemoved) && entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class).size()==8,
                    "Successful recast dismisses mature old batch and Nameless creates eight native doubles");
            var fresh=entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class);fresh.forEach(e->e.tickCount=11);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),true);
            h.assertTrue(fresh.stream().allMatch(Entity::isRemoved),"Book-only sneak cast uses native dismiss, not teleport");
            resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),false);p.setShiftKeyDown(false);resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),true);
            fresh=entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class);var victim=fresh.get(0);
            victim.hurt(victim.damageSources().magic(),1);h.assertTrue(victim.isRemoved(),"Actual native clone disappears when hit");
            var expiring=fresh.get(1);expiring.setNoAi(true);expiring.setLimitedLife(1);tick(expiring,3);h.assertTrue(expiring.isRemoved(),"Native lifetime dismisses clone without addon timer");
            clear(h);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.ILLUSION,block(scene(h)),true);h.succeed();
        }finally {clear(h);target.discard();other.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    public static void illusionNativeEquipmentBranches(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            curios(p,ModItems.ILLUSION_ROBE.get());h.assertTrue(CuriosFinder.hasIllusionRobe(p),"Actual native Curios robe detection");
            resolve(h,p,GlyphCatalog.ILLUSION,new EntityHitResult(target),true);
            h.assertTrue(p.hasEffect(MobEffects.INVISIBILITY) && target.hasEffect(MobEffects.BLINDNESS)
                    && entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class).stream().allMatch(e->e.isUpgraded()),
                    "Native illusion robe upgrades clones, cloaks caster and blinds Ars-selected enemy");
            clear(h);curios(p,ModItems.NAMELESS_CROWN.get(),ModItems.NAMELESS_CAPE.get());h.assertTrue(CuriosFinder.hasNamelessSet(p),"Actual native Nameless set detection");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));resolve(h,p,GlyphCatalog.ILLUSION,new EntityHitResult(target),true);
            var clones=entities(h,com.Polarice3.Goety.common.entities.ally.Doppelganger.class);
            h.assertTrue(clones.size()==8 && clones.stream().allMatch(e->e.isUndeadClone() && e.getLifespan()==MathHelper.secondsToTicks(2.875F)
                    && e.getMainHandItem().is(ModItems.NAMELESS_STAFF.get()) && Math.abs(e.position().subtract(target.position()).horizontalDistance()-4)<.01),
                    "Native Nameless set creates eight short-lived staff clones in a four-block ring around actual target");
            var shot=clones.get(0);shot.performRangedAttack(target,0);h.assertTrue(shot.hasShot(),"Undead clone fires actual native NecroBolt and records its one-shot state");h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    }
    public static void boltingAimDamageAndCleanup(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);prepare(h,p);
        var origin=p.position();var enemy=cow(h,origin.add(0,0,.6));var ally=cow(h,origin.add(.5,0,.6));var owner=cow(h,origin.add(8,0,0));
        var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("dash"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);
        Runnable cleanup=()->{p.horizontalCollision=true;p.discard();enemy.discard();ally.discard();owner.discard();board.removePlayerTeam(team);GoetyConfig.RESOURCE_SOURCE.set(old);};
        try {
            resolve(h,p,GlyphCatalog.BOLTING,block(origin.add(6,0,0)),true);
            double baseline=p.getDeltaMovement().length();h.assertTrue(p.getDeltaMovement().x>2 && Math.abs(p.getDeltaMovement().z)<.001,"Block hit directs actual caster dash, independently of facing");
            resolve(h,p,GlyphCatalog.BOLTING,block(origin.add(6,0,0)),true,AugmentAccelerate.INSTANCE);
            h.assertTrue(p.getDeltaMovement().length()>baseline,"Ars Accelerate scales native dash speed");
            p.setYRot(0);p.setXRot(0);resolve(h,p,GlyphCatalog.BOLTING,new EntityHitResult(p),true);
            h.assertTrue(p.getDeltaMovement().z>2 && Math.abs(p.getDeltaMovement().x)<.001,"Self hit uses caster facing");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));resolve(h,p,GlyphCatalog.BOLTING,new EntityHitResult(enemy),true);
            h.assertTrue(p.getDeltaMovement().length()>baseline && enemy.getDeltaMovement().lengthSqr()==0,"Matching staff increases speed without moving selected enemy");
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);SEHelper.setSESouls(p,0);var before=p.getDeltaMovement();
            resolve(h,p,GlyphCatalog.BOLTING,new EntityHitResult(enemy),false);h.assertTrue(p.getDeltaMovement().equals(before),"No resource means no movement");
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.BOLTING,new EntityHitResult(enemy),true);
            p.setGameMode(GameType.SURVIVAL);SEHelper.setSESouls(p,10000);resolve(h,p,GlyphCatalog.BOLTING,new EntityHitResult(enemy),true,owner,AugmentAmplify.INSTANCE);
            h.assertTrue(MiscCapHelper.getCustomSpinTexture(p)!=null,"Native queued task starts synchronized spin texture immediately");
            p.setDeltaMovement(Vec3.ZERO);p.setNoGravity(true);int paid=SEHelper.getSESouls(p);
            h.runAfterDelay(12,()->{
                try {
                    h.assertTrue(enemy.getHealth()<200 && enemy.getLastHurtByMob()==owner && ally.getHealth()==200,"Native timed queue deals real dash damage with explicit owner and caster alliance filtering");
                    h.assertTrue(MiscCapHelper.getCustomSpinTexture(p)==null && SEHelper.getSESouls(p)==paid,"Native timed queue cleans spin state and does not rebill each attack tick");h.succeed();
                }finally {cleanup.run();}
            });
        }catch(Throwable error){cleanup.run();throw error;}
    }
}
