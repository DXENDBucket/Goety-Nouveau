package dev.goetynouveau;
import com.Polarice3.Goety.common.magic.spells.void_spells.BlinkSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.goetynouveau.casting.GoetyResources;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.*;

public final class BlinkCases {
 private static void prepare(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);p.setYRot(0);p.setYHeadRot(0);p.yRotO=0;p.setXRot(0);p.xRotO=0;
  var center=BlockPos.containing(RainStormCases.scene(h));for(int x=-144;x<=144;x++)for(int z=-144;z<=144;z++)h.getLevel().setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
 }
 private static Cow cow(GameTestHelper h,Vec3 pos){var c=EntityType.COW.create(h.getLevel());c.setNoAi(true);c.setNoGravity(true);c.setPos(pos);h.getLevel().addFreshEntity(c);return c;}
 private static void ally(GameTestHelper h,ServerPlayer p,LivingEntity q){var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("blink_"+UUID.randomUUID().toString().substring(0,7));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(q.getScoreboardName(),team);}
 private static void cast(GameTestHelper h,ServerPlayer p,HitResult hit,boolean paid){
  var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.BLINK),p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(context,GlyphCatalog.BLINK.focus().spell()).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(paid&&!p.isCreative()?fee:0),"Only native successful blink pays source fee: before="+before+", after="+SEHelper.getSESouls(p));
 }
 private static void clean(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);}
 private static boolean same(Vec3 a,Vec3 b){return a.distanceToSqr(b)<.0001;}
 public static void nativeSelfForwardAndWandIsolation(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var start=p.position();var wall=p.blockPosition().offset(0,0,12);for(int x=-2;x<=2;x++)for(int y=0;y<5;y++)h.getLevel().setBlock(wall.offset(x,y,0),Blocks.STONE.defaultBlockState(),2);
   var nativeSpell=new BlinkSpell();nativeSpell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,WandUtil.getStats(p,nativeSpell));var reference=p.position();h.assertTrue(!same(start,reference),"Unscoped native forward branch really moves recipient");
   p.setPos(start);p.fallDistance=10;cast(h,p,new EntityHitResult(p),true);h.assertTrue(same(p.position(),reference) && p.fallDistance==0,"Ars self forward uses actual native ledge search, teleport event and fall reset");
   p.setPos(start);nativeSpell.SpellResult(h.getLevel(),p,ItemStack.EMPTY,WandUtil.getStats(p,nativeSpell));h.assertTrue(same(p.position(),reference),"Outside callback scope ordinary native wand behavior is unchanged");h.succeed();
  }finally{clean(h,p);}
 }
 public static void alliedSourceRangeAndBodyIsolation(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> equip){
  prepare(h,p);try{
   var host=cow(h,RainStormCases.scene(h));ally(h,p,host);host.setYRot(-90);host.yRotO=-90;host.setXRot(0);host.xRotO=0;equip.accept(p);
   var spell=new BlinkSpell();var stats=WandUtil.getStats(p,spell);h.assertTrue(WandUtil.enchantedFocus(p) && WandUtil.getRangeLevel(p)>0,"Actual source focus range enchant active");stats.setRange(stats.getRange()+WandUtil.getRangeLevel(p));
   var start=host.position();var source=p.position();spell.SpellResult(h.getLevel(),host,GoetyResources.staff(p),stats);var reference=host.position();h.assertTrue(!same(start,reference),"Native allied reference uses source stats/staff and host aim");
   host.setPos(start);cast(h,p,new EntityHitResult(host),true);h.assertTrue(same(host.position(),reference) && same(p.position(),source),"Host own aim/body receives native teleport, source range/staff only applied once, payer never moved");h.succeed();
  }finally{clean(h,p);}
 }
 public static void nativeEntityApproachAndSneakRandom(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var host=cow(h,RainStormCases.scene(h));ally(h,p,host);host.setYRot(0);host.yRotO=0;host.setYHeadRot(0);host.yHeadRotO=0;host.setXRot(0);host.xRotO=0;
   var enemy=cow(h,host.position().add(0,0,12));var spell=new BlinkSpell();h.assertTrue(spell.entityResult(h.getLevel(),host,32,3)!=null,"Actual native sight trace finds entity from host body: eye="+host.getEyePosition()+", view="+host.getViewVector(1)+", target="+enemy.getBoundingBox()+", alive="+enemy.isAlive()+", pick="+enemy.isPickable());
   var start=host.position();host.getRandom().setSeed(42);spell.SpellResult(h.getLevel(),host,ItemStack.EMPTY,WandUtil.getStats(host,spell));var reference=host.position();h.assertTrue(!same(start,reference),"Real native entity-approach randomTeleport succeeded");
   host.setPos(start);host.getRandom().setSeed(42);cast(h,p,new EntityHitResult(host),true);h.assertTrue(same(host.position(),reference),"Identical native toward-entity candidates and actual landing from Ars host");enemy.discard();
   host.setPos(start);p.setShiftKeyDown(true);host.getRandom().setSeed(73);boolean moved=false;for(int i=0;i<64 && !(moved=MobUtil.teleport(host,32));i++){}reference=host.position();h.assertTrue(moved,"Native random helper really succeeds on floor");
   host.setPos(start);host.getRandom().setSeed(73);cast(h,p,new EntityHitResult(host),true);h.assertTrue(same(host.position(),reference),"Source Ars sneak chooses native random branch with host RNG/position and original range");h.succeed();
  }finally{p.setShiftKeyDown(false);clean(h,p);}
 }
 public static void eventRejectionResourcesAndCreative(GameTestHelper h,ServerPlayer p,BiConsumer<LivingEntity,Runnable> cancel,Consumer<LivingEntity> enderGround){
  prepare(h,p);try{
   var target=cow(h,RainStormCases.scene(h));var start=target.position();cast(h,p,new EntityHitResult(target),false);cast(h,p,new BlockHitResult(start,Direction.UP,BlockPos.containing(start),false),false);h.assertTrue(same(target.position(),start),"Enemies and block targets cannot execute beneficial movement");ally(h,p,target);
   SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(target),false);h.assertTrue(same(target.position(),start),"Insufficient source resource cannot move target");SEHelper.setSESouls(p,100000);
   cancel.accept(target,()->{cast(h,p,new EntityHitResult(target),false);p.setShiftKeyDown(true);cast(h,p,new EntityHitResult(target),false);p.setShiftKeyDown(false);var enemy=cow(h,start.add(0,0,12));try{cast(h,p,new EntityHitResult(target),false);}finally{enemy.discard();}});h.assertTrue(same(target.position(),start),"Native events can cancel all three branches without charge");
   enderGround.accept(target);cast(h,p,new EntityHitResult(target),false);h.assertTrue(same(target.position(),start),"Native Ender Ground event blocks recipient");target.removeAllEffects();
   p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(target),true);h.assertTrue(!same(target.position(),start) && SEHelper.getSESouls(p)==0,"Creative uses actual native teleport free");h.succeed();
  }finally{p.setShiftKeyDown(false);clean(h,p);}
 }
}
