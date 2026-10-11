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

public final class CreatureSummonCases {
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
    public static void slimeVariantsSplittingAndQuota(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var limit=SpellConfig.SlimyLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.SlimyLimit.set(4);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            var staffs=List.of(ModItems.NECRO_STAFF.get(),ModItems.NETHER_STAFF.get(),ModItems.ABYSS_STAFF.get());
            var types=List.of(ModEntityType.CRYPT_SLIME_SERVANT.get(),ModEntityType.MAGMA_CUBE_SERVANT.get(),ModEntityType.TROPICAL_SLIME_SERVANT.get());
            for(int i=0;i<staffs.size();i++) {
                clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(staffs.get(i)));resolve(h,p,GlyphCatalog.SLIMY,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
                var slimes=entities(h,com.Polarice3.Goety.common.entities.ally.SlimeServant.class);var type=types.get(i);
                h.assertTrue(slimes.size()==2 && slimes.stream().allMatch(e->e.getType()==type && e.getSize()==2 && e.getTrueOwner()==p && e.getTarget()==target && e.getLifespan()>=1200),"Native slime staff variant, size, owner, target and duration: "+type);
            }
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);resolve(h,p,GlyphCatalog.SLIMY,block(scene(h)),true);
            var parent=entities(h,com.Polarice3.Goety.common.entities.ally.SlimeServant.class).get(0);int life=parent.getLifespan();
            parent.setHealth(0);parent.remove(Entity.RemovalReason.KILLED);
            var children=entities(h,com.Polarice3.Goety.common.entities.ally.SlimeServant.class);
            h.assertTrue(children.size()>=2 && children.size()<=4 && children.stream().allMatch(e->e.getSize()==1 && e.getTrueOwner()==p && e.getLifespan()==life),"Actual native splitting retains owner and remaining lifespan");
            SpellConfig.SlimyLimit.set(children.size());resolve(h,p,GlyphCatalog.SLIMY,block(scene(h)),false);
            p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.SLIMY,block(scene(h)),true);
            h.assertTrue(children.stream().allMatch(e->e.distanceTo(p)<1),"Split children share native quota and recall");h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.SlimyLimit.set(limit);}
    }
    public static void carrionAndSpriteNativeChildren(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var cl=SpellConfig.CarrionLimit.get();var sl=SpellConfig.SpriteLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.CarrionLimit.set(4);SpellConfig.SpriteLimit.set(2);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            var maggot=ModEntityType.CARRION_MAGGOT.get().create(h.getLevel());maggot.setTrueOwner(p);maggot.setPos(scene(h).add(5,0,0));maggot.setLimitedLife(1234);h.getLevel().addFreshEntity(maggot);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));
            resolve(h,p,GlyphCatalog.CARRION,new EntityHitResult(target),true,AugmentExtendTime.INSTANCE);
            var flies=entities(h,CarrionFly.class);
            h.assertTrue(flies.size()==3 && flies.stream().allMatch(e->e.getTrueOwner()==p && e.getTarget()==target && e.getY()==target.getY()+1 && e.getLifespan()>=1200 && e.isPersistenceRequired()),"Native maggot shares quota; fly batch stays airborne with owner and duration");
            resolve(h,p,GlyphCatalog.CARRION,block(scene(h)),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.CARRION,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).stream().allMatch(e->e.distanceTo(p)<1),"Native recall includes both maggots and flies");
            clear(h);p.setShiftKeyDown(false);SpellConfig.CarrionLimit.set(8);
            resolve(h,p,GlyphCatalog.CARRION,block(scene(h)),true);
            var fly=entities(h,CarrionFly.class).get(0);
            target.setHealth(1);h.assertTrue(fly.doHurtTarget(200,target) && !target.isAlive(),"Native servant attack kills a real victim");
            var maggots=entities(h,com.Polarice3.Goety.common.entities.neutral.CarrionMaggot.class);
            h.assertTrue(!maggots.isEmpty() && maggots.stream().allMatch(e->e.getTrueOwner()==p && e.getLifespan()==fly.getLifespan()),"Real death callback creates owned maggots with remaining native life");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
            resolve(h,p,GlyphCatalog.SPRITE,block(scene(h)),true,AugmentAmplify.INSTANCE);
            var sprites=entities(h,com.Polarice3.Goety.common.entities.ally.SpriteMob.class);
            h.assertTrue(sprites.size()==2 && sprites.stream().allMatch(e->e.getTrueOwner()==p && e.isPersistenceRequired() && e.getY()==scene(h).y+1 && e.getLifespan()>=600),"Native Sprite staff batch is quota-clipped, airborne, with persistence supplied by native owned finalization");
            resolve(h,p,GlyphCatalog.SPRITE,block(scene(h)),false);
            p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.SPRITE,block(scene(h)),true);
            h.assertTrue(sprites.stream().allMatch(e->e.distanceTo(p)<1),"Sprite recall at full quota");
            var sprite=sprites.get(0);sprite.setLimitedLife(1);sprite.setHealth(1);tick(sprite,100);h.assertTrue(!sprite.isAlive(),"Native Sprite expires through its actual lifecycle");
            h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.CarrionLimit.set(cl);SpellConfig.SpriteLimit.set(sl);}
    }
    public static void whisperAndFrostbornVariants(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var wl=SpellConfig.WhisperLimit.get();var il=SpellConfig.IceGolemLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.WhisperLimit.set(2);SpellConfig.IceGolemLimit.set(1);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));resolve(h,p,GlyphCatalog.WHISPER,new EntityHitResult(target),true,AugmentExtendTime.INSTANCE);
            var whispers=entities(h,Owned.class);h.assertTrue(whispers.size()==2 && whispers.stream().allMatch(e->e.getType()==ModEntityType.WHISPERER.get() && e.getTarget()==target && e.getLifespan()>=1200),"Native Whisperer Wild staff batch and selected target");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));resolve(h,p,GlyphCatalog.WHISPER,block(scene(h)),true);
            var wave=entities(h,Owned.class).get(0);h.assertTrue(wave.getType()==ModEntityType.WAVEWHISPERER.get(),"Abyss staff native Wavewhisperer");
            p.setShiftKeyDown(true);resolve(h,p,GlyphCatalog.WHISPER,block(scene(h)),true);h.assertTrue(wave.distanceTo(p)<1,"Native Wavewhisperer recall does not acquire unrelated aquatic exclusion");
            clear(h);p.setShiftKeyDown(false);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            resolve(h,p,GlyphCatalog.FROSTBORN,new EntityHitResult(target),true,AugmentAmplify.INSTANCE);
            var golems=entities(h,Owned.class);h.assertTrue(golems.size()==1 && golems.get(0).getType()==ModEntityType.ICE_GOLEM.get() && golems.get(0).getTrueOwner()==p && golems.get(0).getTarget()==target,"Native Ice Golem quota, body and target");
            resolve(h,p,GlyphCatalog.FROSTBORN,block(scene(h)),false);
            clear(h);SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.FROSTBORN,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);resolve(h,p,GlyphCatalog.FROSTBORN,block(scene(h)),true);h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.WhisperLimit.set(wl);SpellConfig.IceGolemLimit.set(il);}
    }
    public static void guardNativeFormationAndEquipment(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();var vl=SpellConfig.VanguardLimit.get();var bl=SpellConfig.BlackguardLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);SpellConfig.VanguardLimit.set(7);SpellConfig.BlackguardLimit.set(7);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));
        try {
            p.setYRot(0);p.setYHeadRot(0);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));
            for(var glyph:List.of(GlyphCatalog.VANGUARD,GlyphCatalog.BLACKGUARD)) {
                clear(h);resolve(h,p,glyph,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
                var guards=entities(h,Owned.class);
                h.assertTrue(guards.size()==7 && guards.stream().allMatch(e->e.getTrueOwner()==p && e.getTarget()==target && e.getLifespan()>=1200 && e.getMainHandItem().isEmpty()),"Native seven-guard formation, model weapons without inventory items, lifetime and selected target: "+glyph.focus()+" -> "+guards.size());
                h.assertTrue(guards.stream().allMatch(e->Math.abs(e.getZ()-(target.getZ()+2))<0.01) && guards.stream().map(e->e.getX()).distinct().count()==7,"Nameless row is centered on selected target with original spacing");
                resolve(h,p,glyph,block(scene(h)),false);
                p.setShiftKeyDown(true);resolve(h,p,glyph,block(scene(h)),true);h.assertTrue(guards.stream().allMatch(e->e.distanceTo(p)<1),"Native guard recall at full quota");p.setShiftKeyDown(false);
            }
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.OMINOUS_STAFF.get()));resolve(h,p,GlyphCatalog.BLACKGUARD,block(scene(h)),true);
            var blackguards=entities(h,Owned.class);var spell=(SummonSpell)GlyphCatalog.BLACKGUARD.focus().spell();
            int expected=NativeSummonHooks.guardCount(EffectFocus.BLACKGUARD,spell,p,p.getOffhandItem());
            h.assertTrue(blackguards.size()==expected && blackguards.stream().allMatch(e->e.getType()==NativeSummonHooks.blackguardType(p.getOffhandItem())),"Loader-native Ominous Blackguard count and royal variant");
            clear(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));resolve(h,p,GlyphCatalog.VANGUARD,block(scene(h)),true);
            h.assertTrue(entities(h,Owned.class).size()>=2 && entities(h,Owned.class).size()<=7,"Matching staff keeps native random 2-7 batch");h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.VanguardLimit.set(vl);SpellConfig.BlackguardLimit.set(bl);}
    }
}
