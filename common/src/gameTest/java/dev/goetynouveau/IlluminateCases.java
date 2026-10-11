package dev.goetynouveau;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.magic.spells.utility.IlluminateSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDurationDown;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;

public final class IlluminateCases {
 private static Mob host(GameTestHelper h,ServerPlayer p){var e=RainStormCases.host(h,p);e.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));return e;}
 private static int cast(GameTestHelper h,ServerPlayer p,HitResult hit,int payments,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.ILLUMINATE);for(var a:augments)spell=spell.add(a);
  var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));resolver.spellContext.nextPart();
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,GlyphCatalog.ILLUMINATE.focus().spell(),java.util.List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(p.isCreative()?0:payments*fee),"Only successful light changes pay native fee: before="+before+", after="+SEHelper.getSESouls(p)+", fee="+fee+", lights="+lights(h));return fee;
 }
 private static int lights(GameTestHelper h){return (int)BlockPos.betweenClosedStream(BlockPos.containing(RainStormCases.scene(h)).offset(-16,-8,-16),BlockPos.containing(RainStormCases.scene(h)).offset(28,8,16)).filter(pos->h.getLevel().getBlockState(pos).is(ModBlocks.GLOW_LIGHT_BLOCK.get())).count();}
 private static void empty(GameTestHelper h){var center=BlockPos.containing(RainStormCases.scene(h));for(int x=-48;x<=32;x++)for(int z=-20;z<=20;z++)for(int y=-10;y<=10;y++)h.getLevel().setBlock(center.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);}
 private static void clean(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);empty(h);}
 public static void zeroNativePeriodIsRejected(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);empty(h);int old=SpellConfig.IlluminateDuration.get();try{
   var host=host(h,p);h.getLevel().setBlock(host.blockPosition().below(),Blocks.STONE.defaultBlockState(),2);SpellConfig.IlluminateDuration.set(0);
   cast(h,p,new EntityHitResult(host),0);h.assertTrue(lights(h)==0 && !ExecutionSessions.isCasting(p),"Zero native modulo period is rejected before native callback or payment");h.succeed();
  }finally{SpellConfig.IlluminateDuration.set(old);clean(h,p);}
 }
 public static void actualNativePlacementAndSourceGeometry(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);empty(h);try{
   var host=host(h,p);var base=host.blockPosition().below();h.getLevel().setBlock(base,Blocks.STONE.defaultBlockState(),2);
   int fee=cast(h,p,new EntityHitResult(host),1);h.assertTrue(lights(h)==1 && ExecutionSessions.isCasting(p),"One actual native light is placed at the remote executor and channel remains active");
   h.assertTrue(p.position().distanceTo(host.position())>16,"Original source is outside native scan range");
   int balance=SEHelper.getSESouls(p);ExecutionSessions.tick();ExecutionSessions.tick();h.assertTrue(lights(h)==1 && SEHelper.getSESouls(p)==balance,"Repeated same-world-tick calls cannot emit or charge twice");
   h.assertTrue(GoetyNativeGeometry.current()==null && fee>0,"Native observer/source scope is restored");h.succeed();
  }finally{clean(h,p);}
 }
 public static void emptySearchLiveMovementCadenceAndFunds(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);empty(h);var host=host(h,p);int fee=cast(h,p,new EntityHitResult(host),0),before=SEHelper.getSESouls(p);int interval=Math.max(1,SpellConfig.IlluminateDuration.get());
  h.assertTrue(lights(h)==0 && ExecutionSessions.isCasting(p),"An empty search is unpaid but can continue moving");
  host.setPos(host.position().add(12,0,0));h.getLevel().setBlock(host.blockPosition().below(),Blocks.STONE.defaultBlockState(),2);
  h.runAfterDelay(Math.max(0,interval-1),()->{
   h.assertTrue(lights(h)==0 && SEHelper.getSESouls(p)==before,"No native placement before its configured interval");
   h.runAfterDelay(2,()->{
    h.assertTrue(lights(h)==1 && SEHelper.getSESouls(p)==before-fee,"Live moved executor emits next native light with one fee");SEHelper.setSESouls(p,0);
    h.runAfterDelay(2,()->{h.assertTrue(!ExecutionSessions.isCasting(p),"Empty account stops future placement");clean(h,p);h.succeed();});
   });
  });
 }
 public static void nativeReplacementBoundsAndPreparation(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);empty(h);try{
   var host=host(h,p);var spell=(IlluminateSpell)GlyphCatalog.ILLUMINATE.focus().spell();var pos=host.blockPosition();h.getLevel().setBlock(pos,ModBlocks.SOUL_LIGHT_BLOCK.get().defaultBlockState(),2);
   int[] observed={0};var view=new GoetyNativeGeometry.View(CastContext.detached(p,new EntityHitResult(host),null),host,spell,ignored->observed[0]++);
   boolean replaced=GoetyNativeGeometry.scoped(view,()->spell.replaceSoulLight(pos,h.getLevel()));
   h.assertTrue(replaced && observed[0]==1 && h.getLevel().getBlockState(pos).is(ModBlocks.GLOW_LIGHT_BLOCK.get()),"Actual native Soul Light conversion reports one successful result");
   h.assertTrue(!GoetyNativeGeometry.scoped(view,()->spell.replaceSoulLight(pos,h.getLevel())) && observed[0]==1,"Failed native conversion does not report a payment result");
   var outside=new BlockPos(pos.getX(),h.getLevel().getMinBuildHeight()-1,pos.getZ());
   h.assertTrue(!GoetyNativeGeometry.scoped(view,()->spell.tryToPlaceAtPos(h.getLevel(),p,outside)),"Addon scan rejects build-boundary reads");
   var unloaded=new BlockPos(25000000,100,25000000);h.assertTrue(!h.getLevel().hasChunkAt(unloaded),"Remote test chunk is unloaded");
   h.assertTrue(!GoetyNativeGeometry.scoped(view,()->spell.tryToPlaceAtPos(h.getLevel(),p,unloaded)) && !h.getLevel().hasChunkAt(unloaded),"Native placement cannot load an absent chunk through addon scan");
   int old=SpellConfig.IlluminateChargeUp.get();try{SpellConfig.IlluminateChargeUp.set(9);h.assertTrue(GoetyActiveCastProvider.preparationTicks(spell,p,ItemStack.EMPTY)==9,"Use native charge-up rather than 72000-tick held item lifetime");}finally{SpellConfig.IlluminateChargeUp.set(old);}
   h.succeed();
  }finally{clean(h,p);}
 }
 public static void rejectedExecutorsCreativeAndBoundedExpiry(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);empty(h);var host=host(h,p);
  cast(h,p,new EntityHitResult(host.getTarget()),0);cast(h,p,new BlockHitResult(host.position(),Direction.UP,host.blockPosition(),false),0);
  SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(host),0);h.assertTrue(!ExecutionSessions.isCasting(p),"Enemy/block/unfunded recipient rejected");
  p.setGameMode(GameType.CREATIVE);cast(h,p,new EntityHitResult(host),0,AugmentDurationDown.INSTANCE);h.assertTrue(!ExecutionSessions.isCasting(p),"Zero-duration creative cast still rejected");
  h.getLevel().setBlock(host.blockPosition().below(),Blocks.STONE.defaultBlockState(),2);cast(h,p,new EntityHitResult(host),1);
  h.assertTrue(lights(h)==1 && ExecutionSessions.isCasting(p),"Creative places a real native light without resource deduction");
  var board=h.getLevel().getScoreboard();board.removePlayerFromTeam(host.getScoreboardName(),board.getPlayersTeam(host.getScoreboardName()));ExecutionSessions.tick();h.assertTrue(!ExecutionSessions.isCasting(p),"Lost alliance immediately ends scan");
  empty(h);cast(h,p,new EntityHitResult(p),0);h.assertTrue(ExecutionSessions.isCasting(p),"Self cast can remain active with an empty scan");
  int ticks=GoetyChannelCasting.boundedTicks(0,0);h.runAfterDelay(ticks+1,()->{h.assertTrue(!ExecutionSessions.isCasting(p) && SEHelper.getSESouls(p)==0,"Native held utility is bounded and empty search remains unpaid");clean(h,p);h.succeed();});
 }
}
