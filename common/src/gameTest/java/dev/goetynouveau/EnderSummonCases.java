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

public final class EnderSummonCases {
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
        entities(h,Owned.class).forEach(Entity::discard); entities(h,net.minecraft.world.entity.projectile.Projectile.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    private static List<Owned> owned(GameTestHelper h,GoetyEffectGlyph glyph,ServerPlayer p) {
        var spell=(SummonSpell)glyph.focus().spell();
        return entities(h,Owned.class).stream().filter(e->e.getTrueOwner()==p && spell.summonPredicate().test(e)).toList();
    }
    public static void nativeBatchesQuotaRecallAndResources(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();
        var wl=SpellConfig.WatchlingLimit.get();var sl=SpellConfig.SnarelingLimit.get();var bl=SpellConfig.BlastlingLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);
        SpellConfig.WatchlingLimit.set(2);SpellConfig.SnarelingLimit.set(2);SpellConfig.BlastlingLimit.set(2);prepare(h,p);
        var target=cow(h,scene(h).add(0,0,6));var other=cow(h,scene(h).add(8,0,6));
        try {
            var glyphs=List.of(GlyphCatalog.WATCHING,GlyphCatalog.SNARING,GlyphCatalog.BLASTING);
            var types=List.of(ModEntityType.WATCHLING_SERVANT.get(),ModEntityType.SNARELING_SERVANT.get(),ModEntityType.BLASTLING_SERVANT.get());
            for(int i=0;i<glyphs.size();i++) {
                var glyph=glyphs.get(i);var type=types.get(i);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
                p.setShiftKeyDown(true);resolve(h,p,glyph,block(scene(h)),false);p.setShiftKeyDown(false);
                resolve(h,p,glyph,new EntityHitResult(target),true,other,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
                var first=owned(h,glyph,p);h.assertTrue(first.size()==1 && first.get(0).getType()==type && first.get(0).getTarget()==target
                        && first.get(0).getLifespan()>=1200 && first.get(0).isPersistenceRequired() && !first.get(0).getActiveEffects().isEmpty(),
                        "Native species, target, potency buff, persistence and duration; explicit damage owner does not own summon: "+glyph.focus());
                var outsider=(Owned)type.create(h.getLevel());outsider.setTrueOwner(other);outsider.setPos(scene(h).add(8,0,-6));outsider.setLimitedLife(1200);h.getLevel().addFreshEntity(outsider);
                p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOID_STAFF.get()));
                resolve(h,p,glyph,new EntityHitResult(target),true);h.assertTrue(owned(h,glyph,p).size()==2,"Native 2-5 staff batch is clipped to one remaining slot, excluding another owner: "+glyph.focus());
                resolve(h,p,glyph,block(scene(h)),false);
                p.setShiftKeyDown(true);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);resolve(h,p,glyph,block(scene(h)),true);
                h.assertTrue(owned(h,glyph,p).stream().allMatch(e->e.distanceTo(p)<1) && outsider.distanceTo(p)>8,"Book-only native recall respects owner: "+glyph.focus());p.setShiftKeyDown(false);
                outsider.discard();
            }
            h.assertTrue(entities(h,Owned.class).size()==6,"Three native quota predicates remain independent");
            clear(h);SEHelper.setSESouls(p,0);
            for(var glyph:glyphs) resolve(h,p,glyph,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE);SpellConfig.SummonDown.set(true);
            for(var glyph:glyphs) { resolve(h,p,glyph,block(scene(h)),true);h.assertTrue(((SummonSpell)glyph.focus().spell()).hasSummonDown(p),"Native SummonDown remains active"); }
            h.succeed();
        }finally {clear(h);target.discard();other.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);SpellConfig.WatchlingLimit.set(wl);SpellConfig.SnarelingLimit.set(sl);SpellConfig.BlastlingLimit.set(bl);}
    }
    public static void nativeAttacksHidingAndExpiry(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get();var down=SpellConfig.SummonDown.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);SpellConfig.SummonDown.set(false);prepare(h,p);
        var target=cow(h,scene(h).add(6,0,0));
        try {
            resolve(h,p,GlyphCatalog.WATCHING,block(scene(h)),true);
            resolve(h,p,GlyphCatalog.SNARING,block(scene(h)),true);
            resolve(h,p,GlyphCatalog.BLASTING,block(scene(h)),true);
            var watch=entities(h,com.Polarice3.Goety.common.entities.ally.ender.WatchlingServant.class).get(0);
            var snare=entities(h,com.Polarice3.Goety.common.entities.ally.ender.SnarelingServant.class).get(0);
            var blast=entities(h,com.Polarice3.Goety.common.entities.ally.ender.BlastlingServant.class).get(0);
            snare.performRangedAttack(target,0);
            var shots=entities(h,com.Polarice3.Goety.common.entities.projectiles.SnarelingShot.class);
            h.assertTrue(shots.size()==1 && shots.get(0).getOwner()==snare && shots.get(0).getDeltaMovement().lengthSqr()>0,"Real native snareling ranged callback creates its owned moving shot");
            // Blastling's performRangedAttack is intentionally empty upstream. Its actual
            // ranged Goal owns the pre-attack, alternating shots and post-attack states.
            blast.setPos(scene(h).add(-6,0,0));blast.setTarget(target);
            var goal=new com.Polarice3.Goety.common.entities.neutral.ender.AbstractBlastling.BlastlingRangedGoal(blast);
            h.assertTrue(goal.canUse(),"Native Blastling goal accepts the live visible selected enemy");goal.start();
            for(int i=0;i<30;i++) goal.tick();
            var goo=entities(h,com.Polarice3.Goety.common.entities.projectiles.EnderGoo.class);
            h.assertTrue(goo.size()==2 && goo.stream().allMatch(e->e.getOwner()==blast)
                    && blast.getCurrentAnimation()==blast.getAnimationState(com.Polarice3.Goety.common.entities.neutral.ender.AbstractBlastling.ATTACK),
                    "Actual native alternating-arm attack goal emits two owned goo projectiles and attack animation");goal.stop();
            h.assertTrue(blast.getCurrentAnimation()==blast.getAnimationState(com.Polarice3.Goety.common.entities.neutral.ender.AbstractBlastling.POST_ATTACK),"Native ranged goal completes the post-attack animation");
            for(int i=0;i<10;i++) watch.startHide();h.assertTrue(watch.isHiding(),"Native Watchling pre-hide sequence remains active");
            for(var servant:List.of(watch,snare,blast)) {
                servant.setHide(false);servant.setTarget(null);servant.setNoAi(true);servant.setLimitedLife(1);servant.setHealth(1);tick(servant,100);
                h.assertTrue(!servant.isAlive(),"Actual native ender servant expiry: "+servant.getType());
            }
            h.succeed();
        }finally {clear(h);target.discard();p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);SpellConfig.SummonDown.set(down);}
    }
}
