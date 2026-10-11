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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class RemainingSummonCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-36,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        entities(h,LivingEntity.class).stream().filter(e->e!=p).forEach(Entity::discard);
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
        entities(h,Owned.class).forEach(Entity::discard); entities(h,net.minecraft.world.entity.projectile.Projectile.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    private static List<Owned> owned(GameTestHelper h,GoetyEffectGlyph glyph,ServerPlayer p) {
        var spell=(SummonSpell)glyph.focus().spell();
        return entities(h,Owned.class).stream().filter(e->e.getTrueOwner()==p && spell.summonPredicate().test(e)).toList();
    }
    private static void geoRobe(ServerPlayer p) {
        var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);
        for(var handler:inventory.getCurios().values()) for(int i=0;i<handler.getStacks().getSlots();i++)
            handler.getStacks().setStackInSlot(i,ItemStack.EMPTY);
        // Ars Elemental adds an an_focus slot excluded by Curios' native equipped search.
        // Put the robe in its actual tagged body slot, as a player would.
        var body=inventory.getCurios().get("body");
        if(body==null || body.getStacks().getSlots()==0) throw new IllegalStateException("Missing body slot");
        body.getStacks().setStackInSlot(0,new ItemStack(ModItems.GEO_ROBE.get()));
    }
    private static void fillQuota(GameTestHelper h,GoetyEffectGlyph glyph,ServerPlayer p,int slotsLeft) {
        var spell=(SummonSpell)glyph.focus().spell();
        int need=spell.summonLimit()-owned(h,glyph,p).size()-slotsLeft;
        var type=owned(h,glyph,p).get(0).getType();
        for(int i=0;i<need;i++) {
            var e=(Owned)type.create(h.getLevel());e.setTrueOwner(p);e.setNoAi(true);e.setPos(scene(h).add(8,0,8));
            e.setLimitedLife(1200);h.getLevel().addFreshEntity(e);
        }
    }
    public static void ministrosityVariantsQuotaAndRecall(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));var other=cow(h,scene(h).add(8,0,-6));
        var glyph=GlyphCatalog.MINISTROUS;
        Runnable cleanup=()-> { clear(h);target.discard();other.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down); };
        try {
            resolve(h,p,glyph,new EntityHitResult(target),true,other,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var first=owned(h,glyph,p).get(0);
            h.assertTrue(first.getType()==ModEntityType.STONE_MINISTROSITY.get()&&first.getTarget()==target
                    && first.getLifespan()>=1200&&first.isPersistenceRequired()&&!first.getActiveEffects().isEmpty(),
                    "Native stone servant, original owner, selected enemy, buffs and extended life");
            clear(h);geoRobe(p);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
            // Curios 9 caches misses for the current game tick. A real equipment change
            // becomes visible naturally on the next tick; do not mutate its private caches.
            h.runAfterDelay(1,()-> {
                try {
                    h.assertTrue(((SummonSpell)glyph.focus().spell()).GeoPower(p),"Native GeoPower recognizes the body-slot robe on the next game tick");
                    resolve(h,p,glyph,block(scene(h)),true);
                    var red=owned(h,glyph,p);
                    h.assertTrue(red.size()>=2&&red.size()<=3&&red.stream().allMatch(e->e.getType()==ModEntityType.REDSTONE_MINISTROSITY.get()),
                    "Actual native GeoPower robe and staff batch: "+red.stream().map(e->e.getType().toString()).toList());
                    fillQuota(h,glyph,p,1);resolve(h,p,glyph,block(scene(h)),true);
                    h.assertTrue(owned(h,glyph,p).size()==((SummonSpell)glyph.focus().spell()).summonLimit(),"Native staff batch clips to remaining owner quota");
                    resolve(h,p,glyph,block(scene(h)),false);
                    p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);
                    resolve(h,p,glyph,block(scene(h)),true);
                    h.assertTrue(owned(h,glyph,p).stream().allMatch(e->e.distanceTo(p)<1),"Book-only native recall remains valid at quota");
                    p.setShiftKeyDown(false);var expiring=owned(h,glyph,p).get(0);expiring.setNoAi(true);expiring.setHealth(1);expiring.setLimitedLife(1);tick(expiring,100);
                    h.assertTrue(!expiring.isAlive(),"Native golem limited-life callback expires");clear(h);SEHelper.setSESouls(p,0);
                    resolve(h,p,glyph,block(scene(h)),false);p.setGameMode(GameType.CREATIVE);SpellConfig.SummonDown.set(true);
                    resolve(h,p,glyph,block(scene(h)),true);
                    h.assertTrue(((SummonSpell)glyph.focus().spell()).hasSummonDown(p),"Native SummonDown persists on creative successful result");h.succeed();
                } finally { cleanup.run(); }
            });
        } catch(RuntimeException | Error error) { cleanup.run();throw error; }
    }
    public static void burningHoglinFlanksExplosionAndQuota(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));var glyph=GlyphCatalog.HOGGING;
        try {
            resolve(h,p,glyph,block(scene(h)),true);
            var single=(BurningHoglin)owned(h,glyph,p).get(0);
            h.assertTrue(single.getWindUpTime()==20&&single.getLifespan()==100&&single.getTrueOwner()==p,
                    "Ordinary hoglin retains native 20-tick windup and short 100-tick life");
            h.assertTrue(Math.abs(single.getX()-scene(h).x-2)<1,"First hoglin is beside the Ars impact, not beside the original caster");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));
            resolve(h,p,glyph,new EntityHitResult(target),true,AugmentAOE.INSTANCE,AugmentExtendTime.INSTANCE);
            var pair=owned(h,glyph,p);
            double power=5+glyph.focus().spell().defaultStats().getRadius()+1;
            h.assertTrue(pair.size()==2&&pair.stream().allMatch(e->e instanceof BurningHoglin hog&&hog.getWindUpTime()==10
                    &&hog.getLifespan()==200&&Math.abs(hog.getExplosionPower()-power)<.01&&hog.getTarget()==target),
                    "Native Nether staff flank pair, faster windup, radius, duration and selected enemy");
            var exploding=(BurningHoglin)pair.get(0);target.invulnerableTime=0;exploding.doHurtTarget(target);
            h.assertTrue(exploding.isRemoved()&&target.getHealth()<200,"Actual native contact explosion harms enemy and discards the summon");
            clear(h);p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            resolve(h,p,glyph,block(scene(h)),true);
            h.assertTrue(owned(h,glyph,p).size()==1,"Native Hogging sneaking still creates a summon; it is not ordinary servant recall");p.setShiftKeyDown(false);
            fillQuota(h,glyph,p,1);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));
            resolve(h,p,glyph,block(scene(h)),true);resolve(h,p,glyph,block(scene(h)),false);
            h.assertTrue(owned(h,glyph,p).size()==((SummonSpell)glyph.focus().spell()).summonLimit(),"Hoglin pair clips to its real shared quota");
            var expired=(BurningHoglin)owned(h,glyph,p).get(0);expired.lifeSpanDamage();
            h.assertTrue(expired.isRemoved(),"Native lifetime callback explodes and removes hoglin");
            clear(h);SEHelper.setSESouls(p,0);resolve(h,p,glyph,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,glyph,block(scene(h)),true);h.succeed();
        } finally { clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down); }
    }
    public static void breezeBatchQuotaAndRecall(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);prepare(h,p);
        var glyph=GlyphCatalog.BREEZE;var target=cow(h,scene(h).add(0,0,6));
        try {
            h.assertTrue(NativeSummonHooks.breezeSupported()&&GlyphCatalog.ALL.contains(glyph),"Breeze is registered only on the actual native platform");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WIND_STAFF.get()));
            resolve(h,p,glyph,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var pair=owned(h,glyph,p);
            h.assertTrue(pair.size()==2&&pair.stream().allMatch(e->e.getTarget()==target&&e.getLifespan()>=1200&&e.isPersistenceRequired()),
                    "Native wind staff pair, owner, enemy, duration and persistence");
            fillQuota(h,glyph,p,1);resolve(h,p,glyph,block(scene(h)),true);resolve(h,p,glyph,block(scene(h)),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,glyph,block(scene(h)),true);
            h.assertTrue(owned(h,glyph,p).stream().allMatch(e->e.distanceTo(p)<1),"Native breeze recall works at its shared quota");
            p.setShiftKeyDown(false);var expired=owned(h,glyph,p).get(0);expired.setNoAi(true);expired.setHealth(1);expired.setLimitedLife(1);tick(expired,100);
            h.assertTrue(!expired.isAlive(),"Actual native breeze expiry");clear(h);SEHelper.setSESouls(p,0);
            resolve(h,p,glyph,block(scene(h)),false);p.setGameMode(GameType.CREATIVE);resolve(h,p,glyph,block(scene(h)),true);h.succeed();
        } finally { clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down); }
    }
}
