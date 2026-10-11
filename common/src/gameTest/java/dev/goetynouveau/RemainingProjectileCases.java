package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.entities.util.Cushion;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Native entities and full native virtual hits, not duplicated approximations of damage. */
public final class RemainingProjectileCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(3,0,3)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-48,pos.z);
    }
    private static void prepare(GameTestHelper h, ServerPlayer p) {
        p.setPos(scene(h)); h.getLevel().addNewPlayer(p); p.setYRot(0); p.setYHeadRot(0); p.setXRot(0); p.setGameMode(GameType.SURVIVAL);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,100000);
        var pos=BlockPos.containing(scene(h));
        for(int x=-5;x<=5;x++) for(int z=-3;z<=18;z++) for(int y=-2;y<=6;y++)
            h.getLevel().setBlock(pos.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
    }
    private static List<Projectile> shots(GameTestHelper h, ServerPlayer p) {
        return h.getLevel().getEntitiesOfClass(Projectile.class,new AABB(scene(h),scene(h)).inflate(96),e->e.getOwner()==p);
    }
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer p, Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
    }
    private static EntityProjectileSpell launch(GameTestHelper h, ServerPlayer p, BoltFocus focus, Marker marker, boolean orbit) {
        var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.projectile(focus),marker));
        cast.spellContext.nextPart();
        EntityProjectileSpell carrier=orbit?new EntityOrbitProjectile(h.getLevel(),cast,p):new EntityProjectileSpell(h.getLevel(),cast);
        if(orbit) carrier.setPos(((EntityOrbitProjectile)carrier).getNextHitPosition());
        carrier.setDeltaMovement(p.getViewVector(1).scale(.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Core dispatch accepts "+focus); return carrier;
    }
    private static int price(GameTestHelper h,ServerPlayer p,BoltFocus focus) {
        return GoetyResources.quote(resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.projectile(focus))).spellContext,focus.spell()).amount();
    }
    private static void clear(GameTestHelper h,ServerPlayer p) {
        // Tests execute synchronously; clear only objects created in this fixture's local volume.
        for(var e:h.getLevel().getEntities(p,new AABB(scene(h),scene(h)).inflate(40))) e.discard();
        ExecutionSessions.tick();
    }
    public static void nativeLaunchesAndFullHits(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            for(var focus:BoltFocus.values()) if(focus.additional() && focus!=BoltFocus.GLOW_LIGHT) {
                clear(h,p); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
                focus.spell().SpellResult(h.getLevel(),p,ItemStack.EMPTY,focus.spell().defaultStats());
                var reference=shots(h,p).get(0); var origin=reference.position(); var family=reference.getClass();
                clear(h,p); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
                var marker=new Marker(); int before=SEHelper.getSESouls(p),fee=price(h,p,focus);
                launch(h,p,focus,marker,false); var batch=shots(h,p);
                h.assertTrue(batch.size()==1 && batch.get(0).getClass()==family,"Native family preserved for "+focus);
                var shot=batch.get(0);
                h.assertTrue(shot.getOwner()==p,"Original native owner preserved for "+focus);
                h.assertTrue(shot.position().distanceTo(origin)<.0001,"Native default launch offset preserved for "+focus+" native="+origin+" actual="+shot.position());
                h.assertTrue(SEHelper.getSESouls(p)==before-fee,"One launch charge for "+focus);
                for(int i=0;i<3;i++) shot.tick();
                h.assertTrue(!shot.isRemoved() && shot.position().distanceTo(origin)>.01,"Native flight and tick operate for "+focus);
                var cow=EntityType.COW.create(h.getLevel()); cow.setPos(scene(h).add(0,0,12)); cow.setNoAi(true); cow.setNoGravity(true);
                cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500); cow.setHealth(500); h.getLevel().addFreshEntity(cow);
                for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++)
                    h.getLevel().setBlockAndUpdate(cow.blockPosition().offset(x,-1,z),Blocks.STONE.defaultBlockState());
                shot.setPos(cow.position().add(0,.5,0));
                float nativeSwordDamage = -1;
                if (focus == BoltFocus.SWORD) {
                    var nativeSword = new SwordProjectile(p,h.getLevel(),new ItemStack(Items.IRON_SWORD));
                    ((ProjectileHitAccess)nativeSword).goetyNouveau$hit(new EntityHitResult(cow));
                    nativeSwordDamage = 500 - cow.getHealth(); cow.setHealth(500); cow.invulnerableTime=0;
                }
                GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(cow));
                h.assertTrue((focus == BoltFocus.SWORD ? Math.abs(500-cow.getHealth()-nativeSwordDamage)<.001 : cow.getHealth()<500) && marker.hits==1,"Full native hit and one Ars continuation for "+focus+" hp="+cow.getHealth()+" hits="+marker.hits+" nativeSwordDamage="+nativeSwordDamage+" caster="+p.getHealth()+" removed="+shot.isRemoved());
                h.assertTrue(SEHelper.getSESouls(p)==before-fee,"Impact does not charge the paid launch again for "+focus);
                if(focus==BoltFocus.ICE_SPIKE) h.assertTrue(cow.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.freezing(1,0).getEffect()),"Ice Spike retains native freezing");
                if(focus==BoltFocus.VOID_BOMB) {
                    long children=h.getLevel().getEntities(cow,cow.getBoundingBox().inflate(10)).stream().filter(e->e instanceof VoidShock).count();
                    h.assertTrue(children==8,"Void bomb retains all eight native secondary shocks");
                    GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(cow));
                    h.assertTrue(marker.hits==1,"Settling void bomb cannot duplicate the continuation");
                }
                if(focus==BoltFocus.BLOSSOM || focus==BoltFocus.MAGMA)
                    h.assertTrue(h.getLevel().getEntities(cow,cow.getBoundingBox().inflate(10)).stream().anyMatch(e->e!=shot && !(e instanceof Projectile)),"Native persistent/summoned aftermath retained for "+focus+" owner="+shot.getOwner());
            }
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void repeatingCarriersAndDeniedBilling(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            for(var focus:BoltFocus.values()) if(focus.additional() && focus!=BoltFocus.GLOW_LIGHT) {
                clear(h,p); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
                int fee=price(h,p,focus); SEHelper.setSESouls(p,fee*2); var marker=new Marker();
                var carrier=launch(h,p,focus,marker,true); h.assertTrue(!shots(h,p).isEmpty(),"Repeated payload spawns for "+focus); var shot=shots(h,p).get(0); var start=shot.position();
                shot.tick(); h.assertTrue(shot.position().distanceTo(start)<.0001,"No duplicate special-carrier movement for "+focus);
                h.assertTrue(SEHelper.getSESouls(p)==fee*2,"Repeated carriers do not prepay for "+focus);
                var target=EntityType.COW.create(h.getLevel()); target.setPos(scene(h).add(0,0,12)); target.setNoAi(true); target.setNoGravity(true);
                target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); target.setHealth(1000); h.getLevel().addFreshEntity(target);
                for(int i=0;i<3;i++) {
                    target.invulnerableTime=0;
                    dev.arsconflux.api.glyph.ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(target));
                    h.assertTrue(marker.hits==Math.min(i+1,2),"Paid repeated continuation count for "+focus);
                }
                h.assertTrue(SEHelper.getSESouls(p)==0 && shot.isRemoved(),"Exhaustion ends native execution for "+focus);
            }
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void salvosPiercingSwordAndCreative(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            var focuses=List.of(BoltFocus.LAVABALL,BoltFocus.BOUNCY_BUBBLE,BoltFocus.SMACK_STONE,BoltFocus.VOID_BOMB);
            var staffs=List.of(ModItems.NETHER_STAFF.get(),ModItems.ABYSS_STAFF.get(),ModItems.GEO_STAFF.get(),ModItems.VOID_STAFF.get());
            for(int i=0;i<focuses.size();i++) {
                clear(h,p); var focus=focuses.get(i); p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(staffs.get(i)));
                int before=SEHelper.getSESouls(p),fee=price(h,p,focus); launch(h,p,focus,new Marker(),false);
                h.assertTrue(shots(h,p).size()==3 && SEHelper.getSESouls(p)==before-fee,"Native staff salvo is one paid batch: "+focus+" shots="+shots(h,p).size()+" souls="+SEHelper.getSESouls(p)+" fee="+fee);
            }
            clear(h,p); p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            launch(h,p,BoltFocus.ICE_SPIKE,new Marker(),false);
            h.assertTrue(shots(h,p).get(0) instanceof IceSpear spear && spear.getPierceLevel()>0,"Frost staff retains native piercing spear");
            clear(h,p); p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); int balance=SEHelper.getSESouls(p);
            launch(h,p,BoltFocus.SWORD,new Marker(),false); h.assertTrue(shots(h,p).isEmpty() && SEHelper.getSESouls(p)==balance,"No sword means no projectile or fee");
            var sword=new ItemStack(Items.IRON_SWORD); sword.setDamageValue(sword.getMaxDamage()-5); p.setItemInHand(InteractionHand.OFF_HAND,sword);
            var marker=new Marker(); launch(h,p,BoltFocus.SWORD,marker,false);
            h.assertTrue(sword.isEmpty() && shots(h,p).size()==1,"Native launch consumes ten weapon durability even if it breaks");
            var cow=EntityType.COW.create(h.getLevel()); cow.setPos(scene(h).add(0,0,12)); h.getLevel().addFreshEntity(cow);
            GoetyNativeProjectileCasting.nativeImpact(shots(h,p).get(0),new EntityHitResult(cow));
            h.assertTrue(marker.hits==1 && cow.getHealth()<cow.getMaxHealth(),"Already-launched sword remains valid after the held weapon breaks");
            clear(h,p); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD)); SEHelper.setSESouls(p,0); p.setGameMode(GameType.CREATIVE);
            for(var focus:BoltFocus.values()) if(focus.additional() && focus!=BoltFocus.GLOW_LIGHT) {
                clear(h,p); launch(h,p,focus,new Marker(),false);
                h.assertTrue(!shots(h,p).isEmpty() && SEHelper.getSESouls(p)==0,"Creative launch is free for "+focus);
            }
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void nativeCollisionHooks(GameTestHelper h, ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            for(var focus:BoltFocus.values()) if(focus.additional() && focus!=BoltFocus.GLOW_LIGHT) {
                clear(h,p); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.IRON_SWORD));
                var marker=new Marker(); int before=SEHelper.getSESouls(p),fee=price(h,p,focus);
                launch(h,p,focus,marker,false); var shot=shots(h,p).get(0);
                var wall=shot.blockPosition().offset(0,0,4);
                for(int x=-3;x<=3;x++) for(int y=-10;y<=5;y++)
                    h.getLevel().setBlockAndUpdate(wall.offset(x,y,0),Blocks.STONE.defaultBlockState());
                for(int i=0;i<45 && marker.hits==0 && !shot.isRemoved();i++) { ++shot.tickCount; shot.tick(); ExecutionSessions.tick(); }
                h.assertTrue(marker.hits==1,"Actual native block collision continues Ars exactly once for "+focus);
                h.assertTrue(SEHelper.getSESouls(p)==before-fee,"Native collision does not charge the launch twice for "+focus);
                for(int x=-3;x<=3;x++) for(int y=-10;y<=5;y++)
                    h.getLevel().setBlockAndUpdate(wall.offset(x,y,0),Blocks.AIR.defaultBlockState());
            }
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void unaugmentedCushionWorks(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p); var point=scene(h).add(0,0,12); var block=BlockPos.containing(point).below();
            h.getLevel().setBlockAndUpdate(block,Blocks.STONE.defaultBlockState());
            var cast=resolver(h,p,new Spell(MethodTouch.INSTANCE,GlyphCatalog.CUSHION)); cast.spellContext.nextPart();
            int fee=GoetyResources.quote(cast.spellContext,GlyphCatalog.CUSHION.focus().spell()).amount();
            cast.onResolveEffect(h.getLevel(),new BlockHitResult(point,Direction.UP,block,false));
            var cushions=h.getLevel().getEntitiesOfClass(Cushion.class,new AABB(point,point).inflate(5));
            h.assertTrue(cushions.size()==1 && cushions.get(0).getRadius()==0 && SEHelper.getSESouls(p)==100000-fee,"Unaugmented native Cushion has a valid base radius and pays once");
            var cushion=cushions.get(0); cushion.setNoGravity(true);
            var cow=EntityType.COW.create(h.getLevel()); cow.setPos(point.add(1,1,0)); cow.setNoAi(true); cow.setNoGravity(true); h.getLevel().addFreshEntity(cow);
            cow.fallDistance=15; cushion.lifeTicks=180; cushion.tick();
            h.assertTrue(cow.fallDistance==0 && !cushion.isRemoved(),"Native Cushion actually resets fall distance after its warmup");
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker() { super(CarrierRegistry.id("goety_nouveau:glyph_projectile_test_marker"),"Marker"); }
        public int getDefaultManaCost(){return 0;} public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;} public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;} public Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
    }
}
