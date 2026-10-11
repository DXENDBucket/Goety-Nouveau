package dev.goetynouveau;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAccelerate;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.BoltFocus;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.AugmentBurning;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class SoulLightCases {
 private static final class Marker extends AbstractEffect {
  int hits;Marker(){super(CarrierRegistry.id("goety_nouveau:glyph_soul_light_test_marker"),"Marker");}
  public int getDefaultManaCost(){return 0;}public int getCastingCost(){return 0;}
  public SpellTier defaultTier(){return SpellTier.ONE;}public SpellTier getConfigTier(){return SpellTier.ONE;}
  public boolean isEnabled(){return true;}public Set<AbstractAugment> getCompatibleAugments(){return Set.of();}
  public void onResolve(HitResult hit,Level world,LivingEntity caster,SpellStats stats,SpellContext context,SpellResolver resolver){hits++;}
 }
 private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,Marker marker,AbstractAugment... modifiers){
  var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.SOUL_LIGHT);for(var a:modifiers)spell=spell.add(a);spell=spell.add(marker);
  return new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
 }
 private static EntityProjectileSpell launch(GameTestHelper h,ServerPlayer p,Marker marker,boolean orbit,AbstractAugment... modifiers){
  var cast=resolver(h,p,marker,modifiers);cast.spellContext.nextPart();EntityProjectileSpell carrier=orbit?new EntityOrbitProjectile(h.getLevel(),cast,p):new EntityProjectileSpell(h.getLevel(),cast);
  carrier.setPos(p.getEyePosition().add(0,-.1,0));carrier.setDeltaMovement(0,0,.75);h.assertTrue(ProjectileDispatch.convert(carrier),"Core recognizes Soul Light payload");return carrier;
 }
 private static SoulLight shot(GameTestHelper h){return RainStormCases.entities(h,SoulLight.class).stream().filter(e->!e.isRemoved()).findFirst().orElse(null);}
 private static int fee(GameTestHelper h,ServerPlayer p){return GoetyResources.quote(resolver(h,p,new Marker()).spellContext,BoltFocus.SOUL_LIGHT.spell()).amount();}
 private static void prepare(GameTestHelper h,ServerPlayer p){RainStormCases.prepare(h,p);p.setPos(RainStormCases.scene(h));p.setYRot(0);p.setXRot(0);}
 private static void clear(GameTestHelper h,ServerPlayer p){RainStormCases.clear(h,p);}
 private static void finish(GameTestHelper h,ServerPlayer p){p.discard();clear(h,p);}
 public static void actualNativeFlightAndParticles(GameTestHelper h,ServerPlayer p){
  prepare(h,p);var spell=BoltFocus.SOUL_LIGHT.spell();spell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,spell.defaultStats());var reference=shot(h);h.assertTrue(reference!=null,"Native callback produces actual SoulLight");var origin=reference.position();clear(h,p);
  int before=SEHelper.getSESouls(p),fee=fee(h,p);launch(h,p,new Marker(),false);var shot=shot(h);
  h.assertTrue(shot!=null && shot.getOwner()==p && shot.position().distanceToSqr(origin)<.0001 && Math.abs(shot.getDeltaMovement().length()-2.5)<.001 && shot.isNoGravity(),"Native entity, eye origin, facing + nominal launch speed and no-gravity retained");
  h.assertTrue(shot.sourceParticle()==ModParticleTypes.SOUL_LIGHT_EFFECT.get() && shot.trailParticle()==ModParticleTypes.BULLET_EFFECT.get() && shot.LightBlock()==ModBlocks.SOUL_LIGHT_BLOCK.get(),"Actual native soul source particles, bullet trail and soul block, rather than GlowLight assets");
  h.assertTrue(SEHelper.getSESouls(p)==before-fee && !shot.shouldBeSaved(),"One source launch charge and transient managed carrier");
  var start=shot.position();h.runAfterDelay(3,()->{h.assertTrue(!shot.isRemoved() && shot.position().distanceTo(start)>5 && SEHelper.getSESouls(p)==before-fee,"Actual world tick drives complete native flight without repeated launch charges");finish(h,p);h.succeed();});
 }
 public static void fullNativeEntityAndBlockImpacts(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var cow=EntityType.COW.create(h.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(p.position().add(0,0,8));h.getLevel().addFreshEntity(cow);
   var marker=new Marker();launch(h,p,marker,false);var shot=shot(h);GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(cow));
   h.assertTrue(cow.getEffect(MobEffects.GLOWING)!=null && cow.getEffect(MobEffects.GLOWING).getDuration()==200 && cow.getHealth()==cow.getMaxHealth() && marker.hits==1 && shot.isRemoved(),"Actual native entity hit glows without damage and continues Ars exactly once");
   GoetyNativeProjectileCasting.nativeImpact(shot,new EntityHitResult(cow));h.assertTrue(marker.hits==1,"Removed ordinary light cannot run continuation twice");
   clear(h,p);marker=new Marker();launch(h,p,marker,false);shot=shot(h);var pos=p.blockPosition().offset(0,0,9);h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);
   GoetyNativeProjectileCasting.nativeImpact(shot,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
   h.assertTrue(h.getLevel().getBlockState(pos.north()).is(ModBlocks.SOUL_LIGHT_BLOCK.get()) && marker.hits==1,"Real native hit uses block face to place Soul Light, then continues Ars");
   clear(h,p);marker=new Marker();launch(h,p,marker,false);shot=shot(h);h.getLevel().setBlock(pos.north(),Blocks.STONE.defaultBlockState(),2);
   GoetyNativeProjectileCasting.nativeImpact(shot,new BlockHitResult(Vec3.atCenterOf(pos),Direction.NORTH,pos,false));
   h.assertTrue(h.getLevel().getBlockState(pos.north()).is(Blocks.STONE) && marker.hits==1,"Native placement does not replace solid neighbors but still completes hit continuation");h.succeed();
  }finally{finish(h,p);}
 }
 public static void repeatingCarrierPaymentAndParticleContract(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var cow=EntityType.COW.create(h.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(p.position().add(0,0,8));h.getLevel().addFreshEntity(cow);
   int fee=fee(h,p);SEHelper.setSESouls(p,fee*2);var marker=new Marker();var carrier=launch(h,p,marker,true);var shot=shot(h);var before=shot.position();shot.tick();
   h.assertTrue(((ManagedBolt)shot).goetyNouveau$arsDriven() && !shot.isRemoved() && shot.position().distanceToSqr(before)<.0001 && SEHelper.getSESouls(p)==fee*2,"Ars orbit owns movement, native tick keeps client particle virtuals, no upfront fee");
   for(int i=0;i<3;i++){ArsSpellAccess.resolver(carrier).onResolveEffect(h.getLevel(),new EntityHitResult(cow));h.assertTrue(marker.hits==Math.min(i+1,2),"Successful repeated native hit charges and continues once");}
   h.assertTrue(SEHelper.getSESouls(p)==0 && shot.isRemoved() && cow.hasEffect(MobEffects.GLOWING),"Native glow results and exhausted repeating resource termination");h.succeed();
  }finally{finish(h,p);}
 }
 public static void creativeResourceValidationAndAcceleration(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   SEHelper.setSESouls(p,0);launch(h,p,new Marker(),false);h.assertTrue(shot(h)==null,"Unfunded source cannot launch a native light");
   p.setGameMode(GameType.CREATIVE);launch(h,p,new Marker(),false,AugmentBurning.INSTANCE);h.assertTrue(shot(h)==null,"Native unsupported Burning is rejected even in creative");
   launch(h,p,new Marker(),false);h.assertTrue(shot(h)!=null && SEHelper.getSESouls(p)==0,"Creative native projectile is free");clear(h,p);
   launch(h,p,new Marker(),false,AugmentAccelerate.INSTANCE);h.assertTrue(shot(h)!=null && Math.abs(shot(h).getDeltaMovement().length()-(2.5*1.25/.75))<.001,"Ars acceleration scales the native 2.5 launch once");h.succeed();
  }finally{finish(h,p);}
 }
}
