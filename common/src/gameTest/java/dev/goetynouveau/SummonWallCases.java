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

public final class SummonWallCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-64,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var start=BlockPos.containing(scene(h));
        for(int x=-18;x<=18;x++) for(int z=-18;z<=18;z++) for(int y=-1;y<=38;y++)
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
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(40));
    }
    private static void tick(Entity entity,int ticks) {
        for(int i=0;i<ticks&&!entity.isRemoved();i++) { ++entity.tickCount; entity.tick(); }
    }
    private static void clear(GameTestHelper h) {
        entities(h,Owned.class).forEach(Entity::discard); entities(h,com.Polarice3.Goety.common.entities.projectiles.Spike.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    public static void nativeBeastVariantsAndRecall(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.HuntingLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.HuntingLimit.set(3);prepare(h,p);
        var enemy=cow(h,scene(h).add(0,0,3));
        try {
            var staffs=List.of(ModItems.NECRO_STAFF.get(),ModItems.STORM_STAFF.get(),ModItems.WIND_STAFF.get(),ModItems.ABYSS_STAFF.get(),ModItems.FROST_STAFF.get(),ModItems.NETHER_STAFF.get());
            var types=List.of(ModEntityType.SKELETON_WOLF.get(),ModEntityType.STORMHOUND.get(),ModEntityType.TWILIGHT_GOAT.get(),ModEntityType.SNAPPER.get(),ModEntityType.WINTER_WOLF.get(),ModEntityType.HELLHOUND.get());
            for(int i=0;i<staffs.size();i++) {
                clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(staffs.get(i)));
                resolve(h,p,GlyphCatalog.HUNTING,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
                var beasts=entities(h,Owned.class);var expected=types.get(i);
                h.assertTrue(beasts.size()==2 && beasts.stream().allMatch(e->e.getType()==expected && e.getTrueOwner()==p && e.getTarget()==enemy && e.hasLifespan() && e.getLifespan()>=1200),"Hunting retains staff variant, owner, target and lifetime: "+expected+" -> "+beasts);
            }
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);
            resolve(h,p,GlyphCatalog.HUNTING,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).stream().allMatch(e->e.distanceTo(p)<1),"Book-only beast recall");
            clear(h);p.setShiftKeyDown(false);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
            resolve(h,p,GlyphCatalog.HUNTING,new EntityHitResult(enemy),true);
            p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.HUNTING,block(scene(h)),false);
            h.assertTrue(entities(h,Owned.class).stream().noneMatch(e->e.distanceTo(p)<1),"Native aquatic recall requires caster underwater");
            clear(h);p.setShiftKeyDown(false);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));
            resolve(h,p,GlyphCatalog.MAULING,new EntityHitResult(enemy),true);
            h.assertTrue(entities(h,Owned.class).size()==2 && entities(h,Owned.class).stream().allMatch(e->e.getType()==ModEntityType.BEAR_SERVANT.get()),"Matching Wild staff summons two bears");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));resolve(h,p,GlyphCatalog.MAULING,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).get(0).getType()==ModEntityType.POLAR_BEAR_SERVANT.get(),"Native polar bear variant");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));resolve(h,p,GlyphCatalog.MAULING,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).get(0).getType()==ModEntityType.GNASHER.get(),"Native Gnasher variant");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.HUNTING,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.HUNTING,block(scene(h)),true);h.succeed();
        }finally {clear(h);enemy.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.HuntingLimit.set(limit);}
    }
    public static void nativeUndeadQuotaAndTarget(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var rl=SpellConfig.ReaperLimit.get();var wl=SpellConfig.WraithLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.ReaperLimit.set(2);SpellConfig.WraithLimit.set(2);prepare(h,p);
        var enemy=cow(h,scene(h).add(0,0,4));
        try {
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));
            resolve(h,p,GlyphCatalog.REAPER,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var reapers=entities(h,Owned.class);h.assertTrue(reapers.size()==2 && reapers.stream().allMatch(e->e.getType()==ModEntityType.REAPER_SERVANT.get() && e.getTrueOwner()==p && e.getTarget()==enemy && e.getLifespan()>=1200),"Native Reaper quota clips batch and retains owner, selected target and extended life");
            resolve(h,p,GlyphCatalog.REAPER,new EntityHitResult(enemy),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.REAPER,block(scene(h)),true);
            h.assertTrue(reapers.stream().allMatch(e->e.distanceTo(p)<1),"Reapers recall at full quota with only Ars book");
            clear(h);p.setShiftKeyDown(false);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));
            resolve(h,p,GlyphCatalog.WRAITH,new EntityHitResult(enemy),true);
            h.assertTrue(entities(h,Owned.class).stream().allMatch(e->e.getType()==ModEntityType.MUCK_WRAITH_SERVANT.get() && e.getTarget()==enemy),"Wild staff produces native Muck Wraith");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));resolve(h,p,GlyphCatalog.WRAITH,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).size()>=1 && entities(h,Owned.class).size()<=2,"Loader-native batch honors Wraith quota");
            h.succeed();
        }finally {clear(h);enemy.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.ReaperLimit.set(rl);SpellConfig.WraithLimit.set(wl);}
    }
    public static void nativePhantomFlightAndLifetime(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.PhantomLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.PhantomLimit.set(1);prepare(h,p);
        var enemy=cow(h,scene(h).add(0,0,5));
        try {
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));
            resolve(h,p,GlyphCatalog.PHANTOM,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var phantoms=entities(h,com.Polarice3.Goety.common.entities.ally.undead.PhantomServant.class);
            h.assertTrue(phantoms.size()==1,"Native Phantom quota clips batch");var phantom=phantoms.get(0);
            h.assertTrue(phantom.getTrueOwner()==p && phantom.getTarget()==enemy && phantom.getPhantomSize()==1 && phantom.getY()>enemy.getY()+15 && phantom.getLifespan()>=1200,"Phantom retains airborne placement, native size, owner and lifetime");
            resolve(h,p,GlyphCatalog.PHANTOM,new EntityHitResult(enemy),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.PHANTOM,block(scene(h)),true);
            h.assertTrue(phantom.distanceTo(p)<1,"Native flying recall works at quota");
            phantom.setLimitedLife(1);phantom.setHealth(1);tick(phantom,100);
            h.assertTrue(!phantom.isAlive(),"Native limited-life tick actually expires servant");
            h.succeed();
        }finally {clear(h);enemy.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.PhantomLimit.set(limit);}
    }
    public static void nativeWallsBombsAndCooldown(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var cd=GoetyConfig.COOLDOWNS_ENABLED.get();var chant=GoetyConfig.CHANTING_ENABLED.get();var creative=GoetyConfig.CREATIVE_COOLDOWN.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);GoetyConfig.COOLDOWNS_ENABLED.set(true);GoetyConfig.CHANTING_ENABLED.set(false);GoetyConfig.CREATIVE_COOLDOWN.set(false);prepare(h,p);
        var focus=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(CarrierRegistry.id("goety:barricade_focus"));
        try {
            resolve(h,p,GlyphCatalog.BARRICADE,block(scene(h)),true,AugmentExtendTime.INSTANCE);
            var walls=entities(h,AbstractMonolith.class);h.assertTrue(!walls.isEmpty() && walls.stream().allMatch(e->e.getTrueOwner()==p && e.getLifeSpan()==7),"Native owned walls and duration");
            walls.forEach(e->tick(e,400));h.assertTrue(walls.stream().allMatch(Entity::isRemoved),"Native walls emerge and retract at expiry");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));resolve(h,p,GlyphCatalog.BARRICADE,block(scene(h)),true);
            h.assertTrue(entities(h,AbstractMonolith.class).stream().allMatch(e->e.getType()==ModEntityType.GLACIAL_WALL.get()),"Frost staff creates native glacial walls");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));p.setShiftKeyDown(true);SEHelper.getCooldowns(p).remove(focus);
            var recipe=new Spell(MethodTouch.INSTANCE,GlyphCatalog.BARRICADE);
            h.assertTrue(dev.arsconflux.api.casting.ActiveCasts.execute(recipe,p,dev.arsconflux.api.casting.ActiveCastSource.CASTER_TOOL,()->{resolve(h,p,GlyphCatalog.BARRICADE,block(scene(h)),true,AugmentAmplify.INSTANCE);return true;},false),"Actual shared release executes native bomb");
            var bombs=entities(h,TotemicBomb.class);h.assertTrue(bombs.size()==1 || bombs.size()==4,"Native single or quadruple bomb pattern");
            h.assertTrue(bombs.stream().allMatch(e->e.getTrueOwner()==p && e.getExplosionPower()>=1.5F),"Native bomb owner and amplified explosion power");
            int penalty=bombs.size()==4?60:40;var nativeSpell=GlyphCatalog.BARRICADE.focus().spell();
            int expected=(int)((nativeSpell.defaultSpellCooldown()+penalty)*com.Polarice3.Goety.init.ModAttributes.getCooldownDiscount(p));
            h.assertTrue(GoetyFocusCooldowns.remaining(p,focus)==expected,"Base release cannot overwrite result-specific native cooldown");
            bombs.forEach(e->tick(e,130));h.assertTrue(bombs.stream().allMatch(Entity::isRemoved),"Native bomb fuse explodes and removes real entity");
            clear(h);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.BARRICADE,block(scene(h)),false);
            h.assertTrue(entities(h,AbstractMonolith.class).isEmpty(),"Unfunded activation cannot generate geometry");
            SEHelper.getCooldowns(p).remove(focus);p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.BARRICADE,block(scene(h)),true);
            h.assertTrue(!SEHelper.isOnCooldown(p,new ItemStack(focus)),"Creative result is free and cooldown bypassed");
            h.succeed();
        }finally {clear(h);p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);GoetyConfig.COOLDOWNS_ENABLED.set(cd);GoetyConfig.CHANTING_ENABLED.set(chant);GoetyConfig.CREATIVE_COOLDOWN.set(creative);}
    }
}
