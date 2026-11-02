package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.entities.util.VoidLightningTrap;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import dev.goetynouveau.platform.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Five focused fixtures use the real native payloads and transactions, not a model of their behavior. */
public final class BossAbilityCases {
    private static Vec3 scene(GameTestHelper h){var p=h.absoluteVec(new Vec3(4,0,4));return new Vec3(p.x,h.getLevel().getMaxBuildHeight()-48,p.z);}
    private static void prepare(GameTestHelper h,ServerPlayer p){
        var center=BlockPos.containing(scene(h));
        for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)for(int y=-1;y<=7;y++)
            h.getLevel().setBlock(center.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-6,0,0));p.setGameMode(GameType.SURVIVAL);
        h.getLevel().addNewPlayer(p);SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,100000);
    }
    private static LivingEntity target(GameTestHelper h,Vec3 pos){
        var cow=EntityType.COW.create(h.getLevel());cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);
        cow.setHealth(500);cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(pos);h.getLevel().addFreshEntity(cow);return cow;
    }
    private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,Spell spell){return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));}
    private static void effect(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit){
        var cast=resolver(h,p,new Spell(MethodTouch.INSTANCE,glyph));cast.spellContext.nextPart();cast.onResolveEffect(h.getLevel(),hit);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type){return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(22));}
    private static void clear(GameTestHelper h,ServerPlayer p){
        for(var e:h.getLevel().getEntities(p,new AABB(scene(h),scene(h)).inflate(22)))e.discard();ExecutionSessions.tick();
    }
    private static void cleanup(GameTestHelper h,ServerPlayer p,ResourceSource old){clear(h,p);p.discard();GoetyConfig.RESOURCE_SOURCE.set(old);}
    public static void cloud(GameTestHelper h,ServerPlayer p){
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try{
            prepare(h,p);var enemy=target(h,scene(h));
            effect(h,p,GlyphCatalog.HELL_CLOUD,new EntityHitResult(enemy));
            var cloud=entities(h,HellCloud.class).get(0);
            h.assertTrue(cloud.getOwner()==p && cloud.getY()>enemy.getY(),"Native cloud appears overhead and retains real owner");
            h.assertTrue(SEHelper.getSESouls(p)==99920,"One successful cloud pays 80 soul energy");
            for(int i=0;i<99;i++){cloud.tickCount++;cloud.tick();}
            h.assertTrue(enemy.getHealth()==500,"Native 100-tick activation delay prevents early rain damage");
            cloud.tickCount++;cloud.tick();cloud.tickCount++;cloud.tick();
            h.assertTrue(enemy.getHealth()<500 && enemy.hasEffect(NativeEffectHooks.burnHex(1).getEffect()),"Native rain damages and bridge-only curse completes the boss effect");
            float health=p.getHealth();cloud.hurtEntities(p);h.assertTrue(p.getHealth()==health,"Cloud cannot hurt its real source");
            for(int i=0;i<102;i++){cloud.tickCount++;cloud.tick();}
            h.assertTrue(cloud.isRemoved(),"Native cloud naturally expires");
            var ordinary=new HellCloud(h.getLevel(),p,enemy);enemy.removeAllEffects();enemy.invulnerableTime=0;ordinary.hurtEntities(enemy);
            h.assertTrue(!enemy.hasEffect(NativeEffectHooks.burnHex(1).getEffect()),"Ordinary native player-owned clouds do not acquire the bridge-only boss curse");
            ordinary.discard();
            effect(h,p,GlyphCatalog.HELL_CLOUD,new EntityHitResult(enemy));cloud=entities(h,HellCloud.class).get(0);
            p.setHealth(0);cloud.tick();h.assertTrue(cloud.isRemoved(),"Marked cloud cleans up after source death");h.succeed();
        }finally{cleanup(h,p,old);}
    }
    public static void trap(GameTestHelper h,ServerPlayer p){
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try{
            prepare(h,p);var enemy=target(h,scene(h));effect(h,p,GlyphCatalog.VOID_LIGHTNING_TRAP,new EntityHitResult(enemy));
            var trap=entities(h,VoidLightningTrap.class).get(0);
            h.assertTrue(trap.getOwner()==p && trap.getDuration()==40 && trap.getDamage()>0,"Native trap preserves owner, telegraph delay and configured boss damage");
            h.assertTrue(SEHelper.getSESouls(p)==99940,"Trap pays once on successful placement");
            for(int i=0;i<39;i++){trap.tickCount++;trap.tick();}
            h.assertTrue(enemy.getHealth()==500,"Native trap does not damage during its telegraph");
            p.setPos(scene(h));float health=p.getHealth();trap.tickCount++;trap.tick();
            h.assertTrue(enemy.getHealth()<500 && trap.isRemoved(),"Native delayed lightning actually damages and finishes");
            h.assertTrue(p.getHealth()==health,"Native area and follow-up cannot strike the caster");
            SEHelper.setSESouls(p,0);effect(h,p,GlyphCatalog.VOID_LIGHTNING_TRAP,new EntityHitResult(enemy));
            h.assertTrue(entities(h,VoidLightningTrap.class).isEmpty(),"Unfunded trap creates no telegraph entity");h.succeed();
        }finally{cleanup(h,p,old);}
    }
    public static void hysteria(GameTestHelper h,ServerPlayer p){
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try{
            prepare(h,p);var enemy=target(h,scene(h));effect(h,p,GlyphCatalog.HYSTERIA,new EntityHitResult(enemy));
            h.assertTrue(SEHelper.getSESouls(p)==100000 && !enemy.hasEffect(NativeEffectHooks.hysteria(1,0).getEffect()),"Buff refuses a hostile recipient without charging");
            // Forge native Hysteria hurts players (Neo directly changes health). Expire vanilla spawn protection first.
            for(int i=0;i<80;i++){p.doTick();p.tick();}
            double attack=p.getAttributeValue(Attributes.ATTACK_DAMAGE);effect(h,p,GlyphCatalog.HYSTERIA,new EntityHitResult(p));
            h.assertTrue(p.hasEffect(NativeEffectHooks.hysteria(1,0).getEffect()) && p.getAttributeValue(Attributes.ATTACK_DAMAGE)>attack,"Native frenzy supplies its attribute modifiers");
            h.assertTrue(SEHelper.getSESouls(p)==99950,"Native frenzy pays once");
            effect(h,p,GlyphCatalog.HYSTERIA,new EntityHitResult(p));h.assertTrue(SEHelper.getSESouls(p)==99950,"Equivalent active frenzy is not purchased twice");
            float health=p.getHealth();for(int i=0;i<20;i++){p.doTick();p.tick();}
            h.assertTrue(p.getHealth()<health,"Original effect still consumes health over time");h.succeed();
        }finally{cleanup(h,p,old);}
    }
    private static Projectile launch(GameTestHelper h,ServerPlayer p,BoltFocus focus,boolean orbit,Marker marker){
        var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.projectile(focus),marker));cast.spellContext.nextPart();
        EntityProjectileSpell carrier=orbit?new EntityOrbitProjectile(h.getLevel(),cast,p):new EntityProjectileSpell(h.getLevel(),cast);
        carrier.setPos(scene(h).add(0,1,0));carrier.setDeltaMovement(new Vec3(0,0,.75));
        h.assertTrue(ProjectileDispatch.convert(carrier),"Core converts the native ability payload");
        var type=focus==BoltFocus.DEATH_ARROW?DeathArrow.class:IllBomb.class;
        return entities(h,type).get(0);
    }
    public static void arrow(GameTestHelper h,ServerPlayer p){
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try{
            prepare(h,p);
            for(boolean orbit:List.of(false,true)){
                clear(h,p);SEHelper.setSESouls(p,100000);var marker=new Marker();var arrow=(DeathArrow)launch(h,p,BoltFocus.DEATH_ARROW,orbit,marker);
                h.assertTrue(orbit || Math.abs(arrow.getDeltaMovement().length()-2.4)<.001,"Native Apostle overworld launch speed remains unchanged without acceleration augments");
                h.assertTrue(arrow.getOwner()==p && arrow.pickup==AbstractArrow.Pickup.CREATIVE_ONLY,"Original owner and no free reusable ammunition");
                var enemy=target(h,arrow.position().add(0,0,2));float health=enemy.getHealth();
                GoetyNativeProjectileCasting.nativeImpact(arrow,new EntityHitResult(enemy));
                h.assertTrue(enemy.getHealth()<health && marker.hits==1,"Native arrow damage and exactly one Ars continuation");
                h.assertTrue(SEHelper.getSESouls(p)==99970,"Ordinary or repeated carrier pays once for the successful arrow activation");
                h.assertTrue(enemy.invulnerableTime==0,"Native death arrow retains its post-hit invulnerability reset");
            }
            h.succeed();
        }finally{cleanup(h,p,old);}
    }
    public static void bomb(GameTestHelper h,ServerPlayer p){
        var old=GoetyConfig.RESOURCE_SOURCE.get();GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try{
            prepare(h,p);var marker=new Marker();var bomb=(IllBomb)launch(h,p,BoltFocus.ILL_BOMB,false,marker);
            var pos=BlockPos.containing(bomb.position().add(0,0,1));h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);
            GoetyNativeProjectileCasting.nativeImpact(bomb,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
            h.assertTrue(bomb.getBounceTimes()==1 && !bomb.isRemoved() && bomb.getDeltaMovement().z<0,"Native first wall hit rebounds, rather than being replaced by an immediate generic explosion");
            h.assertTrue(SEHelper.getSESouls(p)==99960 && marker.hits==1,"Bounce continues at the Ars block hit and pays only the original launch");
            // Native shield reflection must create a new owner, not a dummy or a second paid launch.
            var blocker=EntityType.PIGLIN.create(h.getLevel());blocker.setNoAi(true);blocker.setPos(scene(h).add(1,0,0));
            blocker.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(Items.SHIELD));blocker.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
            h.getLevel().addFreshEntity(blocker);for(int i=0;i<6;i++)blocker.tick();
            GoetyNativeProjectileCasting.nativeImpact(bomb,new EntityHitResult(blocker));
            h.assertTrue(bomb.isRemoved() && entities(h,IllBomb.class).stream().anyMatch(b->b.getOwner()==blocker),"Original native shield reflection transfers ownership");
            h.assertTrue(SEHelper.getSESouls(p)==99960,"Native reflected projectile does not debit the original caster again");
            // Reproducible native RNG branches; set seeds only after creating every participant.
            for(int seed:List.of(0,4096)){
                var sample=new IllBomb(p,h.getLevel());sample.setPos(scene(h));
                var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ILL_BOMB));
                GoetyAreaProvenance.mark(sample,CastContext.of(cast.spellContext,null,null));
                int count=entities(h,FireworkRocketEntity.class).size();p.setPos(scene(h));float health=p.getHealth();
                h.getLevel().random.setSeed(seed);sample.explode();
                h.assertTrue(p.getHealth()==health,"Both native explosion branches protect the marked source");
                h.assertTrue((entities(h,FireworkRocketEntity.class).size()>count)==(seed==4096),"Seed selects the expected actual native firework/plain branch");
            }
            float baseLoss=0;
            for(int potency:List.of(0,3)){
                clear(h,p);p.setPos(scene(h).add(-6,0,0));
                var sample=new IllBomb(p,h.getLevel());sample.setPos(scene(h).add(0,1,0));
                var cast=resolver(h,p,new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ILL_BOMB));
                GoetyAreaProvenance.mark(sample,CastContext.of(cast.spellContext,null,null));
                sample.getPersistentData().putFloat(GoetyBossDamageScope.BONUS,potency);
                var victim=target(h,sample.position());float before=victim.getHealth();
                h.getLevel().random.setSeed(0);sample.explode();float loss=before-victim.getHealth();
                if(potency==0)baseLoss=loss;else h.assertTrue(Math.abs(loss-baseLoss-3)<.01,"Native bomb potency enters the real explosion damage once, before common damage scaling");
            }
            for(int mode=0;mode<3;mode++){
                clear(h,p);p.setPos(scene(h).add(-6,0,0));var receipt=new Marker();
                var sample=(IllBomb)launch(h,p,BoltFocus.ILL_BOMB,false,receipt);
                var victim=target(h,sample.position());victim.setInvulnerable(mode!=0);
                h.getLevel().random.setSeed(mode==2?4096:0);
                GoetyNativeProjectileCasting.nativeImpact(sample,new EntityHitResult(victim));
                h.assertTrue(receipt.hits==(mode==1?0:1),"Plain damage needs an accepted hit; a created delayed firework payload continues once; canceled damage alone does not");
            }
            h.succeed();
        }finally{cleanup(h,p,old);}
    }
    private static final class Marker extends AbstractEffect {
        int hits;Marker(){super(CarrierRegistry.id("goety_nouveau:glyph_boss_test_marker"),"Marker");}
        public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}
        public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}
        public boolean isEnabled(){return true;}public Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
        public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
    }
}
