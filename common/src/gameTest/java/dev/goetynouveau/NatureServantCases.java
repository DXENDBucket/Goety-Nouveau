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

public final class NatureServantCases {
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
        entities(h,Owned.class).forEach(Entity::discard); entities(h,GlowLight.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    public static void windSelectedPointStaffAndAllies(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var enemy=cow(h,scene(h).add(0,0,2)); var ally=cow(h,scene(h).add(1,0,2));
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("gust"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            p.setYRot(90); // Caster looks away; the injected Ars incoming direction points south.
            resolve(h,p,GlyphCatalog.WIND_BLAST,block(scene(h)),true);
            h.assertTrue(enemy.getDeltaMovement().z>1 && ally.getDeltaMovement().equals(Vec3.ZERO),"Gust uses the Ars origin/direction and ignores native allies");
            enemy.setDeltaMovement(Vec3.ZERO); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            resolve(h,p,GlyphCatalog.WIND_BLAST,block(scene(h)),true,AugmentAmplify.INSTANCE);
            h.assertTrue(enemy.getDeltaMovement().z>2 && enemy.hasEffect(NativeEffectHooks.freezing(1,0).getEffect()),"Native frost staff adds freezing and potency still scales force");
            enemy.setDeltaMovement(Vec3.ZERO); SEHelper.setSESouls(p,0);
            resolve(h,p,GlyphCatalog.WIND_BLAST,block(scene(h)),false);
            h.assertTrue(enemy.getDeltaMovement().equals(Vec3.ZERO),"No resource means no gust effects");
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.WIND_BLAST,block(scene(h)),true);
            h.succeed();
        } finally { enemy.discard(); ally.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void nativeVinesAndTurrets(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var target=cow(h,scene(h));
        try {
            resolve(h,p,GlyphCatalog.OVERGROWTH,new EntityHitResult(target),true);
            var vines=entities(h,AbstractMonolith.class);
            h.assertTrue(!vines.isEmpty() && vines.stream().allMatch(v->v.getTrueOwner()==p && v instanceof QuickGrowingVine),"Entity-targeted growth uses native vine structures and ownership");
            clear(h); resolve(h,p,GlyphCatalog.OVERGROWTH,block(scene(h)),true);
            h.assertTrue(!entities(h,QuickGrowingVine.class).isEmpty(),"Location-targeted growth keeps the native cube formation");
            clear(h); p.setShiftKeyDown(true); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));
            resolve(h,p,GlyphCatalog.OVERGROWTH,new EntityHitResult(target),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var turrets=entities(h,PoisonQuillVine.class);
            h.assertTrue(turrets.size()==2 && turrets.stream().allMatch(v->v.getTrueOwner()==p && v.getTarget()==target && v.getLifeSpan()>=14),"Sneaking with Wild staff creates two owned native turrets aimed at the selected enemy");
            for(var turret:turrets) { turret.setLifeSpan(0); tick(turret,250); }
            h.assertTrue(turrets.stream().allMatch(Entity::isRemoved),"Native turrets actually expire rather than leaving persistent clones");
            clear(h); SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.OVERGROWTH,block(scene(h)),false);
            h.assertTrue(entities(h,AbstractMonolith.class).isEmpty(),"Exhaustion creates no vines");
            h.succeed();
        } finally { clear(h); target.discard(); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void nativeServantsSharedLimitsVariantsAndRecall(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); var zl=SpellConfig.ZombieLimit.get(); var sl=SpellConfig.SkeletonLimit.get(); var vl=SpellConfig.VexLimit.get(); var down=SpellConfig.SummonDown.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); SpellConfig.ZombieLimit.set(3); SpellConfig.SkeletonLimit.set(3); SpellConfig.VexLimit.set(2); SpellConfig.SummonDown.set(true); prepare(h,p);
        var target=cow(h,scene(h));
        try {
            // A native servant created outside the addon occupies the same configured quota.
            var nativeZombie=ModEntityType.ZOMBIE_SERVANT.get().create(h.getLevel()); nativeZombie.setTrueOwner(p); nativeZombie.setPos(scene(h).add(4,0,0)); h.getLevel().addFreshEntity(nativeZombie);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));
            resolve(h,p,GlyphCatalog.ZOMBIE,new EntityHitResult(target),true,AugmentExtendTime.INSTANCE);
            var zs=entities(h,Owned.class).stream().filter(e->GlyphCatalog.ZOMBIE.focus().spell() instanceof SummonSpell s && s.summonPredicate().test(e)).toList();
            h.assertTrue(zs.size()==3 && zs.stream().filter(z->z!=nativeZombie).allMatch(z->z.getTrueOwner()==p && z.getTarget()==target && z.hasLifespan() && z.getLifespan()>=1200 && z.getLifespan()<=4760 && z.getLifespan()%40==0),"Native zombie quota/target/lifetime: "+zs.stream().map(z->z.getType()+" owner="+(z.getTrueOwner()==p)+" target="+(z.getTarget()==target)+" lifetime="+z.hasLifespan()+"/"+z.getLifespan()).toList());
            h.assertTrue(((SummonSpell)GlyphCatalog.ZOMBIE.focus().spell()).hasSummonDown(p),"Successful batch invokes the native SummonDown mechanism");
            resolve(h,p,GlyphCatalog.ZOMBIE,new EntityHitResult(target),false);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); p.setShiftKeyDown(true);
            resolve(h,p,GlyphCatalog.ZOMBIE,block(scene(h)),true);
            h.assertTrue(zs.stream().allMatch(z->z.distanceTo(p)<1),"Book-only sneaking recalls servants even at a full native quota");
            clear(h); p.setShiftKeyDown(false); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            resolve(h,p,GlyphCatalog.SKELETON,new EntityHitResult(target),true,AugmentAmplify.INSTANCE);
            var ss=entities(h,AbstractSkeletonServant.class);
            h.assertTrue(ss.size()==2 && ss.stream().allMatch(s->s.getType()==ModEntityType.STRAY_SERVANT.get() && s.getTrueOwner()==p && s.getTarget()==target && s.hasEffect(MobEffects.WEAKNESS)),"Frost staff chooses native strays and existing summon decay weakens the new batch");
            clear(h); p.removeAllEffects(); p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            resolve(h,p,GlyphCatalog.VEX,new EntityHitResult(target),true,AugmentAmplify.INSTANCE);
            var vs=entities(h,AllyVex.class);
            h.assertTrue(vs.size()==2 && vs.stream().allMatch(v->v.getTrueOwner()==p && v.getTarget()==target && v.getMainHandItem().isEnchanted()),"Native Vex batch is capped by remaining quota and retains its sharpened weapon");
            resolve(h,p,GlyphCatalog.VEX,new EntityHitResult(target),false);
            p.setShiftKeyDown(true); resolve(h,p,GlyphCatalog.VEX,block(scene(h)),true);
            h.assertTrue(vs.stream().noneMatch(Entity::isAlive),"Vex sneak behavior kills its servants, rather than pretending to recall them");
            clear(h); p.setShiftKeyDown(false);
            var skull=ModEntityType.HAUNTED_SKULL.get().create(h.getLevel()); skull.setTrueOwner(p); skull.setPos(scene(h)); h.getLevel().addFreshEntity(skull);
            p.setShiftKeyDown(true); resolve(h,p,GlyphCatalog.HAUNTED_SKULL,block(scene(h)),true);
            h.assertTrue(skull.distanceTo(p)<1,"Existing skull focus also recalls with only an Ars book");
            clear(h); p.setShiftKeyDown(false); SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.SKELETON,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.SKELETON,block(scene(h)),true);
            var skeleton=entities(h,AbstractSkeletonServant.class).get(0); float health=skeleton.getHealth(); skeleton.setLifespan(1); skeleton.ownedTick();
            h.assertTrue(skeleton.getHealth()<health,"Native ordinary servants take lifespan starvation damage; unlike skulls they do not immediately discard");
            h.succeed();
        } finally { clear(h); target.discard(); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); SpellConfig.ZombieLimit.set(zl); SpellConfig.SkeletonLimit.set(sl); SpellConfig.VexLimit.set(vl); SpellConfig.SummonDown.set(down); }
    }
    private static EntityProjectileSpell launch(GameTestHelper h,ServerPlayer p,Marker marker,boolean orbit) {
        var cast=new SpellResolver(new SpellContext(h.getLevel(),new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GLOW_LIGHT,marker),p,LivingCaster.from(p),p.getMainHandItem()));
        cast.spellContext.nextPart();
        EntityProjectileSpell carrier=orbit?new EntityOrbitProjectile(h.getLevel(),cast,p):new EntityProjectileSpell(h.getLevel(),cast);
        carrier.setPos(scene(h).add(0,1,0)); carrier.setDeltaMovement(0,0,.75);
        h.assertTrue(ProjectileDispatch.convert(carrier),"Conflux recognizes Glow Light as a projectile replacement"); return carrier;
    }
    public static void lightNativeFlightHitsAndRepeating(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var target=cow(h,scene(h).add(0,0,5));
        try {
            var marker=new Marker(); launch(h,p,marker,false); var shot=entities(h,GlowLight.class).get(0);
            h.assertTrue(shot.isNoGravity() && Math.abs(shot.getDeltaMovement().length()-2.5)<.001 && shot.getOwner()==p,"Light retains native no-gravity speed and original owner");
            GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(target));
            h.assertTrue(target.hasEffect(MobEffects.GLOWING) && marker.hits==1 && shot.isRemoved(),"Complete native entity hit applies glowing and runs the remaining Ars effect once");
            clear(h); marker=new Marker(); launch(h,p,marker,false); shot=entities(h,GlowLight.class).get(0);
            var pos=BlockPos.containing(scene(h).add(0,0,8));
            GoetyNativeProjectileCasting.nativeImpact(shot,new BlockHitResult(Vec3.atCenterOf(pos.below()),Direction.UP,pos.below(),false));
            h.assertTrue(h.getLevel().getBlockState(pos).is(shot.LightBlock()) && marker.hits==1,"Complete native block hit places its light block and continues the recipe");
            clear(h); marker=new Marker();
            var quoteContext=new SpellContext(h.getLevel(),new Spell(MethodProjectile.INSTANCE,GlyphCatalog.GLOW_LIGHT),p,LivingCaster.from(p),p.getMainHandItem());
            int fee=GoetyResources.quote(quoteContext,BoltFocus.GLOW_LIGHT.spell()).amount(); SEHelper.setSESouls(p,fee*2);
            var carrier=launch(h,p,marker,true); shot=entities(h,GlowLight.class).get(0); var before=shot.position(); tick(shot,1);
            h.assertTrue(shot.position().distanceTo(before)<.001 && SEHelper.getSESouls(p)==fee*2,"Special carrier owns motion and does not prepay, while native client particle layers remain available");
            for(int i=0;i<3;i++) {
                dev.arsconflux.api.glyph.ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));
                h.assertTrue(marker.hits==Math.min(i+1,2),"Repeating light pays and continues once per successful impact");
            }
            h.assertTrue(SEHelper.getSESouls(p)==0 && shot.isRemoved(),"Resource exhaustion ends the repeating payload");
            h.succeed();
        } finally { clear(h); target.discard(); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker(){super(CarrierRegistry.id("goety_nouveau:glyph_nature_test_marker"),"Marker");}
        public int getDefaultManaCost(){return 0;} public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;} public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;} public Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
    }
}
