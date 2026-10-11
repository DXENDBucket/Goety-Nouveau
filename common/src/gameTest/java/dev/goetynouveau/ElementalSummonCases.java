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

public final class ElementalSummonCases {
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
    private static void curios(ServerPlayer p,net.minecraft.world.item.Item... items) {
        var inventory=top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(p).orElseThrow(IllegalStateException::new);
        int next=0;
        for(var handler:inventory.getCurios().values()) for(int i=0;i<handler.getStacks().getSlots();i++)
            handler.getStacks().setStackInSlot(i,next<items.length?new ItemStack(items[next++]):ItemStack.EMPTY);
        if(next!=items.length) throw new IllegalStateException("Not enough loaded Curios slots");
    }
    public static void blazeEquipmentQuotaAndExpiry(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.BlazeLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.BlazeLimit.set(2);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));var other=cow(h,scene(h).add(8,0,6));
        try {
            resolve(h,p,GlyphCatalog.BLAZING,new EntityHitResult(target),true,other,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var first=entities(h,BlazeServant.class).get(0);
            h.assertTrue(first.getType()==ModEntityType.BLAZE_SERVANT.get() && first.getTrueOwner()==p && first.getTarget()==target
                    && first.getLifespan()>=1200 && first.getFireBallDamage()>0,"Native blaze body, caster ownership, selected enemy, lifespan and actual fireball potency");
            curios(p,ModItems.UNHOLY_ROBE.get(),ModItems.UNHOLY_HAT.get());h.assertTrue(CuriosFinder.hasUnholySet(p),"Native full Unholy set detection");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));
            resolve(h,p,GlyphCatalog.BLAZING,new EntityHitResult(target),true);
            var blazes=entities(h,BlazeServant.class);
            h.assertTrue(blazes.size()==2 && blazes.stream().anyMatch(e->e.getType()==ModEntityType.INFERNO.get()),"Inferno variant and ordinary blaze share quota, native staff batch is clipped");
            resolve(h,p,GlyphCatalog.BLAZING,block(scene(h)),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.BLAZING,block(scene(h)),true);
            h.assertTrue(blazes.stream().allMatch(e->e.distanceTo(p)<1),"Book-only native recall at full shared quota");
            p.setShiftKeyDown(false);curios(p);clear(h);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.BLAZING,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.BLAZING,block(scene(h)),true);
            var expiring=entities(h,BlazeServant.class).get(0);expiring.setNoAi(true);expiring.setLimitedLife(1);expiring.setHealth(1);tick(expiring,100);
            h.assertTrue(!expiring.isAlive(),"Actual native blaze expiry without addon timer");h.succeed();
        }finally {clear(h);target.discard();other.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.BlazeLimit.set(limit);}
    }
    public static void ghastEquipmentPowerAndRecall(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.GhastLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.GhastLimit.set(3);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,1200));
            resolve(h,p,GlyphCatalog.GHASTLY,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var minis=entities(h,com.Polarice3.Goety.common.entities.hostile.servants.Malghast.class);
            int potency=dev.goetynouveau.api.NativeSpellStats.defaults(GlyphCatalog.GHASTLY.focus().spell()).potency()+1;
            float base=ModEntityType.MINI_GHAST.get().create(h.getLevel()).getExplosionPower();
            h.assertTrue(minis.size()==3 && minis.stream().allMatch(e->e.getType()==ModEntityType.MINI_GHAST.get() && e.getTrueOwner()==p && e.getTarget()==target
                    && e.getY()==target.getY()+1 && e.getLifespan()>=1200 && Math.abs(e.getFireBallDamage()-(potency-1))<.01
                    && Math.abs(e.getExplosionPower()-(base+potency/2.5F))<.01),"Three native airborne mini-ghasts, weakness-adjusted damage, explosion potency, owner and target");
            h.assertTrue(minis.stream().allMatch(e->!e.getActiveEffects().isEmpty()==NativeSummonHooks.buffGhast()),"Loader-native ghast buff distinction");
            resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),false);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),true);
            h.assertTrue(minis.stream().allMatch(e->e.distanceTo(p)<1),"Ghast species share quota and native recall");
            clear(h);p.setShiftKeyDown(false);p.removeEffect(net.minecraft.world.effect.MobEffects.WEAKNESS);curios(p,ModItems.NETHER_ROBE.get());
            h.assertTrue(((SummonSpell)GlyphCatalog.GHASTLY.focus().spell()).NetherPower(p),"Actual native robe power");
            resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),true);
            var full=entities(h,com.Polarice3.Goety.common.entities.hostile.servants.Malghast.class);
            h.assertTrue(full.size()==1 && full.get(0).getType()==ModEntityType.GHAST_SERVANT.get(),"Nether robe selects one large ghast and native flying placement");
            clear(h);curios(p,ModItems.UNHOLY_ROBE.get(),ModItems.UNHOLY_HAT.get());p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));
            resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),true);
            full=entities(h,com.Polarice3.Goety.common.entities.hostile.servants.Malghast.class);
            h.assertTrue(full.size()>=1 && full.size()<=3 && full.stream().allMatch(e->e.getType()==ModEntityType.MALGHAST.get()),"Full Unholy set selects native Malghasts; staff keeps reduced native large batch");
            clear(h);curios(p);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.GHASTLY,block(scene(h)),true);
            var expiring=entities(h,com.Polarice3.Goety.common.entities.hostile.servants.Malghast.class).get(0);expiring.setNoAi(true);expiring.setLimitedLife(1);expiring.setHealth(1);tick(expiring,100);
            h.assertTrue(!expiring.isAlive(),"Actual native ghast expiry");h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.GhastLimit.set(limit);}
    }
    public static void guardianWaterQuotaAndNativeDown(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.GuardianLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.GuardianLimit.set(2);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));resolve(h,p,GlyphCatalog.GUARDIAN,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var guards=entities(h,com.Polarice3.Goety.common.entities.ally.GuardianServant.class);
            h.assertTrue(guards.size()==2 && guards.stream().allMatch(e->e.getTrueOwner()==p && e.getTarget()==target && e.getLifespan()>=1200 && e.isPersistenceRequired()),"Native Abyss staff guardian pair, owner, selected target, persistence and duration");
            resolve(h,p,GlyphCatalog.GUARDIAN,block(scene(h)),false);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.GUARDIAN,block(scene(h)),true);
            h.assertTrue(guards.stream().allMatch(e->e.distanceTo(p)<1),"Native guardians recall on land at full quota");clear(h);p.setShiftKeyDown(false);
            var start=BlockPos.containing(scene(h));
            for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++) for(int y=0;y<5;y++) h.getLevel().setBlock(start.offset(x,y,z),Blocks.WATER.defaultBlockState(),2);
            p.setPos(scene(h).add(-10,1,0));p.setNoGravity(true);p.doTick();p.doTick();h.assertTrue(p.isUnderWater(),"Native underwater fixture");
            resolve(h,p,GlyphCatalog.GUARDIAN,new EntityHitResult(target),true);
            var wet=entities(h,com.Polarice3.Goety.common.entities.ally.GuardianServant.class).get(0);
            h.assertTrue(h.getLevel().isWaterAt(wet.blockPosition()) && wet.getTrueOwner()==p,"Water candidate remains in water around Ars-selected location");
            clear(h);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.GUARDIAN,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);SpellConfig.SummonDown.set(true);resolve(h,p,GlyphCatalog.GUARDIAN,block(scene(h)),true);
            h.assertTrue(((SummonSpell)GlyphCatalog.GUARDIAN.focus().spell()).hasSummonDown(p),"Successful native guardian summon installs native SummonDown");
            var expiring=entities(h,com.Polarice3.Goety.common.entities.ally.GuardianServant.class).get(0);expiring.setNoAi(true);expiring.setLimitedLife(1);expiring.setHealth(1);tick(expiring,100);
            h.assertTrue(!expiring.isAlive(),"Actual guardian expiry");h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.GuardianLimit.set(limit);}
    }
}
