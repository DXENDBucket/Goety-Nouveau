package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.util.FirePillar;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.context.CastContexts;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.RiftVisualState;
import dev.goetynouveau.casting.NativeVisualTracking;
import dev.goetynouveau.mixin.FistVisualAccess;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Native world behavior and trigger billing, shared unchanged between the two pinned loaders. */
public final class AreaEffectCases {
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
                .withDamageOwner(owner==null?p:owner));
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
        entities(h,FirePillar.class).forEach(Entity::discard); entities(h,VoidRift.class).forEach(Entity::discard);
    }
    public static void instant(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("area"+UUID.randomUUID().toString().substring(0,8));
        var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(0,0,1)); var owner=cow(h,scene(h).add(-8,0,0));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.FIRE_BLAST,new EntityHitResult(enemy),true,owner,AugmentAmplify.INSTANCE,AugmentBurning.INSTANCE);
            h.assertTrue(enemy.getHealth()<200 && enemy.getLastHurtByMob()==owner && enemy.getRemainingFireTicks()>=200,
                    "Fire blast damages, ignites with Burning and credits the explicit damage owner");
            h.assertTrue(ally.getHealth()==200 && !ally.isOnFire() && enemy.getDeltaMovement().lengthSqr()>0,
                    "Native explosion knocks back enemies and protects the resource caster's allies");
            h.assertTrue(h.getLevel().getBlockState(BlockPos.containing(scene(h)).below()).is(Blocks.STONE),"Spell blast does not destroy terrain");
            enemy.invulnerableTime=0; enemy.setHealth(200); enemy.clearFire(); enemy.setDeltaMovement(Vec3.ZERO);
            enemy.getAttribute(Attributes.ARMOR).setBaseValue(100); enemy.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(100);
            resolve(h,p,GlyphCatalog.SONIC_BOOM,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE);
            float damage=SpellConfig.SonicBoomDamage.get().floatValue()*WandUtil.damageMultiply()+1;
            h.assertTrue(Math.abs(enemy.getHealth()-(200-damage))<.001 && enemy.getDeltaMovement().length()>2,
                    "Selected-target sonic boom bypasses armor/toughness and applies native knockback");
            resolve(h,p,GlyphCatalog.SONIC_BOOM,new EntityHitResult(ally),false);
            enemy.invulnerableTime=0; enemy.setInvulnerable(true); resolve(h,p,GlyphCatalog.SONIC_BOOM,new EntityHitResult(enemy),false);
            enemy.setInvulnerable(false);
            resolve(h,p,GlyphCatalog.SONIC_BOOM,block(scene(h).add(4,0,0)),true);
            resolve(h,p,GlyphCatalog.SONIC_BOOM,new EntityHitResult(p),true);
            SEHelper.setSESouls(p,0); float health=enemy.getHealth();
            resolve(h,p,GlyphCatalog.FIRE_BLAST,new EntityHitResult(enemy),false);
            h.assertTrue(enemy.getHealth()==health,"Empty resource pool prevents the explosion");
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.FIRE_BLAST,block(scene(h).add(6,0,0)),true);
            h.succeed();
        } finally { enemy.discard(); ally.discard(); owner.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void pillars(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(1,0,0));
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("fire"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.FLAME_STRIKE,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
            var pillars=entities(h,FirePillar.class); int base= pillars.size();
            h.assertTrue(base>0 && pillars.stream().allMatch(e->e.getOwner()==p && e.getDuration()==360 && e.getExtraDamage()==1 && e.warmUp==0),
                    "Targeted native ring retains owner, amp, tick duration and no duplicated preparation");
            var pillar=pillars.get(0); enemy.setPos(pillar.position()); ally.setPos(pillar.position().add(0,0,.2));
            tick(pillar,1); h.assertTrue(enemy.getHealth()<200 && enemy.isOnFire() && ally.getHealth()==200 && pillar.isActivated(),
                    "Actual native pillar tick emits/activates, burns and damages enemies, excluding allies");
            tick(pillar,360); h.assertTrue(pillar.isRemoved(),"Native fire pillar expires at its configured tick duration");
            clear(h); enemy.setPos(scene(h));
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));
            resolve(h,p,GlyphCatalog.FLAME_STRIKE,new EntityHitResult(enemy),true);
            h.assertTrue(entities(h,FirePillar.class).size()>base,"Nether staff adds the native outer ring");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); p.setShiftKeyDown(true);
            resolve(h,p,GlyphCatalog.FLAME_STRIKE,new EntityHitResult(enemy),true);
            h.assertTrue(entities(h,FirePillar.class).size()==16,"Book-only sneak switches to native two-ring radial formation");
            clear(h); p.setShiftKeyDown(false);
            resolve(h,p,GlyphCatalog.FLAME_STRIKE,block(scene(h)),false,AugmentDurationDown.INSTANCE);
            SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.FLAME_STRIKE,block(scene(h)),false);
            h.assertTrue(entities(h,FirePillar.class).isEmpty(),"No duration or resource leaves no charged fire pillars");
            h.succeed();
        } finally { clear(h); enemy.discard(); ally.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void rift(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        boolean grief=h.getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(false,h.getLevel().getServer());
        var enemy=cow(h,scene(h).add(0,0,.1)); var distant=cow(h,scene(h).add(5,0,0)); var ally=cow(h,scene(h).add(1,0,0));
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("rift"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h)),true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE,AugmentExtendTime.INSTANCE);
            var rift=entities(h,VoidRift.class).get(0);
            h.assertTrue(rift.getOwner()==p && rift.getDuration()==600 && rift.getExtraDamage()==1 && rift.warmUp==0,
                    "Native rift retains owner, amp and scaled tick duration without a second cast warmup");
            h.assertTrue(rift.getSize()==GlyphCatalog.VOID_RIFT.focus().spell().defaultStats().getRadius()+1 && !rift.isStaff(),
                    "AOE scales native size without a fabricated staff bonus");
            tick(rift,1);
            h.assertTrue(!rift.isRemoved() && rift.isActivated() && enemy.getHealth()<200 && distant.getDeltaMovement().x<0
                    && ally.getHealth()==200 && ally.getDeltaMovement().equals(Vec3.ZERO),
                    "Actual rift tick activates, pulls enemies, damages the center and ignores allies");
            enemy.discard(); distant.discard(); ally.discard();
            tick(rift,599); h.assertTrue(rift.isClosing && !rift.isRemoved(),"Rift enters the native closing animation before removal");
            var burstVictim=cow(h,scene(h).add(4,0,0));
            try {
                h.assertTrue(burstVictim.distanceTo(rift)>rift.getBoundingBox().getSize(),"Closing-burst victim is outside the central damage zone");
                tick(rift,20); h.assertTrue(rift.isRemoved() && burstVictim.getHealth()<200,
                        "Rift's native closing burst hurts an enemy outside its center and removes the entity");
            } finally { burstVictim.discard(); }
            var original=new VoidRift(h.getLevel(),scene(h).x,scene(h).y,scene(h).z);
            original.setOwner(p); original.setDuration(300); original.setWarmUp(0); h.getLevel().addFreshEntity(original);
            tick(original,1); h.assertTrue(original.isRemoved(),"Unmarked native rifts still cancel when their player stops native wand casting");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOID_STAFF.get()));
            resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h).add(0,2,0)),true);
            h.assertTrue(entities(h,VoidRift.class).get(0).isStaff(),"Void staff enables native rift behavior");
            clear(h); resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h)),false,AugmentDurationDown.INSTANCE);
            SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h)),true);
            h.succeed();
        } finally {
            clear(h); enemy.discard(); distant.discard(); ally.discard(); p.discard(); board.removePlayerTeam(team);
            GoetyConfig.RESOURCE_SOURCE.set(old); h.getLevel().getGameRules().getRule(GameRules.RULE_MOBGRIEFING).set(grief,h.getLevel().getServer());
        }
    }
    public static void riftVisuals(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var replicas=new ArrayList<VoidRift>();
        try {
            resolve(h,p,GlyphCatalog.VOID_RIFT,block(scene(h)),true); // No AOE: the previously missing default case.
            var rift=entities(h,VoidRift.class).get(0);
            h.assertTrue(rift.getSize()==0,"Keep native zero bonus size rather than buffing the spell to fix rendering");
            rift.refreshDimensions(); // Native pose/size refresh may collapse the physical bounds at zero bonus.
            h.assertTrue(rift.getBoundingBox().getSize()==0,"Native zero-size physical bounds remain unchanged");
            h.assertTrue(rift.shouldRenderAtSqrDistance(64*64) && rift.getBoundingBoxForCulling().getSize()>0,
                    "Default Ars rift uses visible billboard bounds for distance and frustum culling");
            h.assertTrue(rift.isActivated() && rift.getAnimation()==3 && ((RiftVisualState)rift).goetyNouveau$visualPhase()==RiftVisualState.ACTIVE,
                    "Default rift publishes its active visual phase before the first native tick");
            var replica=new VoidRift(h.getLevel(),scene(h).x,scene(h).y,scene(h).z); replicas.add(replica);
            // Use native spawn/tracking metadata, deliberately without delivering entity event 100.
            replica.getEntityData().assignValues(rift.getEntityData().getNonDefaultValues());
            h.assertTrue(replica.isActivated() && replica.getAnimation()==3
                    && replica.getResourceLocation().toString().endsWith("rift_1.png")
                    && replica.shouldRenderAtSqrDistance(64*64),
                    "An initial or late tracker restores the native active texture without a one-shot activation event");
            rift.tickCount=rift.getDuration()-1; tick(rift,1);
            h.assertTrue(rift.isClosing && ((RiftVisualState)rift).goetyNouveau$visualPhase()==RiftVisualState.CLOSING,
                    "Native closing phase is also durable tracking metadata");
            replica=new VoidRift(h.getLevel(),scene(h).x,scene(h).y,scene(h).z); replicas.add(replica);
            replica.getEntityData().assignValues(rift.getEntityData().getNonDefaultValues());
            h.assertTrue(replica.isActivated() && replica.isClosing && replica.getAnimation()==2,
                    "A tracker entering during closure gets the native closing texture without event 101");
            var saved=new CompoundTag(); rift.addAdditionalSaveData(saved);
            var restored=new VoidRift(h.getLevel(),scene(h).x,scene(h).y,scene(h).z); replicas.add(restored);
            restored.readAdditionalSaveData(saved);
            h.assertTrue(restored.isClosing && ((RiftVisualState)restored).goetyNouveau$visualPhase()==RiftVisualState.CLOSING,
                    "Saved rifts restore durable closing state instead of reverting to invisible or opening visuals");
            var original=new VoidRift(h.getLevel(),scene(h).x,scene(h).y,scene(h).z); replicas.add(original);
            original.setOwner(p);
            h.assertTrue(((RiftVisualState)original).goetyNouveau$visualPhase()==RiftVisualState.NATIVE
                    && original.getBoundingBoxForCulling().equals(original.getBoundingBox()) && !original.isActivated(),
                    "Unmarked native rifts retain original rendering and activation behavior");
            tick(original,1); h.assertTrue(original.isRemoved(),"Original native wand cancellation still applies");
            h.succeed();
        } finally { clear(h); replicas.forEach(Entity::discard); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void fistTracking(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var replicas=new ArrayList<EarthFist>();
        try {
            resolve(h,p,GlyphCatalog.EARTH_FIST,block(scene(h)),true);
            var fist=entities(h,EarthFist.class).get(0);
            h.assertTrue(NativeVisualTracking.events(fist).isEmpty(),"Native fist warmup is not prematurely displayed to new observers");
            for(int i=0;i<40&&!fist.isSentSpikeEvent();i++) tick(fist,1);
            h.assertTrue(fist.isSentSpikeEvent() && NativeVisualTracking.events(fist).equals(List.of((byte)4)),
                    "A late observer receives the original native emergence event after the fist has emerged");
            var replica=new EarthFist(h.getLevel(),scene(h),p); replicas.add(replica);
            for(byte event:NativeVisualTracking.events(fist)) replica.handleEntityEvent(event);
            h.assertTrue(replica.isSentSpikeEvent() && replica.attackAnimationState.isStarted() && replica.getBoundingBox().getSize()>0,
                    "Replayed native event restores the actual renderer gate, growth animation and visible dimensions");
            for(int i=0;i<40&&((FistVisualAccess)fist).goetyNouveau$remainingLife()>0;i++) tick(fist,1);
            h.assertTrue(!fist.isRemoved() && NativeVisualTracking.events(fist).equals(List.of((byte)4,(byte)5)),
                    "Observers entering during native retreat receive emergence and retreat in order");
            replica=new EarthFist(h.getLevel(),scene(h),p); replicas.add(replica);
            for(byte event:NativeVisualTracking.events(fist)) replica.handleEntityEvent(event);
            h.assertTrue(replica.isSentSpikeEvent() && !replica.attackAnimationState.isStarted() && replica.retreatAnimationState.isStarted(),
                    "Late observer restores native retreat animation rather than leaving an invisible fist");
            var original=new EarthFist(h.getLevel(),scene(h),p); replicas.add(original);
            for(int i=0;i<40&&!original.isSentSpikeEvent();i++) tick(original,1);
            h.assertTrue(NativeVisualTracking.events(original).isEmpty(),"Unmarked native casts do not receive bridge animation replays");
            tick(fist,10); h.assertTrue(fist.isRemoved() && NativeVisualTracking.events(fist).isEmpty(),"Removed fists never get replayed");
            h.succeed();
        } finally {
            entities(h,EarthFist.class).forEach(Entity::discard); replicas.forEach(Entity::discard); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old);
        }
    }
}
