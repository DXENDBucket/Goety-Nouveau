package dev.goetynouveau;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.spells.void_spells.BanishSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.goetynouveau.casting.GoetyResources;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.*;

public final class BanishCases {
 private static void prepare(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);p.setYRot(0);p.setYHeadRot(0);p.setXRot(0);
  var center=BlockPos.containing(RainStormCases.scene(h));for(int x=-72;x<=72;x++)for(int z=-72;z<=72;z++)h.getLevel().setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
 }
 private static Cow cow(GameTestHelper h){var c=EntityType.COW.create(h.getLevel());c.setNoAi(true);c.setNoGravity(true);c.setPos(RainStormCases.scene(h));h.getLevel().addFreshEntity(c);return c;}
 private static void cast(GameTestHelper h,ServerPlayer p,HitResult hit,boolean paid){
  var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.BANISH),p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(context,GlyphCatalog.BANISH.focus().spell()).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(paid&&!p.isCreative()?fee:0),"Only real successful teleport commits source native fee: before="+before+", after="+SEHelper.getSESouls(p));
 }
 private static void clean(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);}
 public static void actualSelectedNativeTeleportAndLookIsolation(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var target=cow(h);var decoy=cow(h);decoy.setPos(p.position().add(0,0,2));decoy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Math.max(1000,SpellConfig.BanishMaxHealth.get()+1));decoy.setHealth(decoy.getMaxHealth());
   h.assertTrue(!new BanishSpell().conditionsMet(h.getLevel(),p),"Unscoped native sight trace really sees an immune-by-health decoy");
   var sourcePos=p.position();var targetPos=target.position();var decoyPos=decoy.position();target.getRandom().setSeed(42);cast(h,p,new EntityHitResult(target),true);
   h.assertTrue(target.position().distanceToSqr(targetPos)>.01 && p.position().distanceToSqr(sourcePos)<.001 && decoy.position().distanceToSqr(decoyPos)<.001,"Actual native randomTeleport moves only distant Ars-selected enemy, not caster or sight decoy");
   h.assertTrue(target.getHealth()==target.getMaxHealth() && h.getLevel().noCollision(target) && !target.isInWater(),"Native landing collision/fluid validation and no direct damage remain native");h.succeed();
  }finally{clean(h,p);}
 }
 public static final class SearchProbe extends Cow {
  public final List<Vec3> attempts=new ArrayList<>();
  public SearchProbe(ServerLevel world){super(EntityType.COW,world);setNoAi(true);setNoGravity(true);}
  @Override public boolean randomTeleport(double x,double y,double z,boolean particles){attempts.add(new Vec3(x,y,z));return false;}
 }
 public static void nativeRangeStaffAndFailedSearch(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var target=new SearchProbe(h.getLevel());target.setPos(RainStormCases.scene(h));h.getLevel().addFreshEntity(target);var spell=new BanishSpell();
   for(boolean staff:new boolean[]{false,true}){
    p.setItemInHand(InteractionHand.OFF_HAND,staff?new ItemStack(ModItems.VOID_STAFF.get()):ItemStack.EMPTY);var held=GoetyResources.staff(p);var stats=WandUtil.getStats(p,spell);
    target.getRandom().setSeed(73);spell.touchResult(h.getLevel(),p,target,held,stats);var reference=List.copyOf(target.attempts);h.assertTrue(!reference.isEmpty(),"Native search generated actual candidate attempts");target.attempts.clear();
    target.getRandom().setSeed(73);cast(h,p,new EntityHitResult(target),false);h.assertTrue(target.attempts.equals(reference),"Scoped callback retains native random candidate count and coordinates with Void staff="+staff);
    int range=stats.getRange()*(staff?2:1);h.assertTrue(reference.size()<=range && reference.stream().allMatch(pos->Math.abs(pos.x-target.getX())<=range/2.0 && Math.abs(pos.z-target.getZ())<=range/2.0),"Native default and staff range envelope retained");target.attempts.clear();
   }h.succeed();
  }finally{clean(h,p);}
 }
 public static void nativeSelectedRestrictionsAndTeleportEvent(GameTestHelper h,ServerPlayer p,Consumer<LivingEntity> enderGround,BiConsumer<LivingEntity,Runnable> cancel){
  prepare(h,p);var oldList=SpellConfig.BanishBlackList.get();try{
   var target=cow(h);var start=target.position();target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(SpellConfig.BanishMaxHealth.get()+1);target.setHealth(target.getMaxHealth());cast(h,p,new EntityHitResult(target),false);
   h.assertTrue(target.position().distanceToSqr(start)<.001,"Selected target max health limit, not some unrelated sight target");target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10);target.setHealth(10);
   SpellConfig.BanishBlackList.set(List.of("minecraft:cow"));cast(h,p,new EntityHitResult(target),false);SpellConfig.BanishBlackList.set(oldList);h.assertTrue(target.position().distanceToSqr(start)<.001,"Actual native type config blacklist retained");
   var pig=EntityType.PIG.create(h.getLevel());pig.setNoAi(true);pig.setNoGravity(true);pig.setPos(start.add(3,0,0));h.getLevel().addFreshEntity(pig);h.assertTrue(pig.getType().is(ModTags.EntityTypes.BANISH_IMMUNE),"Fixture resolves actual native immune tag");cast(h,p,new EntityHitResult(pig),false);
   cancel.accept(target,()->cast(h,p,new EntityHitResult(target),false));h.assertTrue(target.position().distanceToSqr(start)<.001,"Actual selected target's native EnderEntity event can cancel with no charge");
   enderGround.accept(target);cast(h,p,new EntityHitResult(target),false);h.assertTrue(target.position().distanceToSqr(start)<.001,"Native Ender Ground potion handler rejects actual recipient teleport");h.succeed();
  }finally{SpellConfig.BanishBlackList.set(oldList);clean(h,p);}
 }
 public static void invalidFriendlyResourcesAndCreative(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var target=cow(h);var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("banish_"+UUID.randomUUID().toString().substring(0,7));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(target.getScoreboardName(),team);
   cast(h,p,new EntityHitResult(target),false);cast(h,p,new EntityHitResult(p),false);cast(h,p,new BlockHitResult(p.position(),Direction.UP,p.blockPosition(),false),false);h.assertTrue(target.position().distanceToSqr(RainStormCases.scene(h))<.001,"Control effect does not uproot self/allied actors or accept block targets");
   board.removePlayerFromTeam(target.getScoreboardName(),team);SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(target),false);h.assertTrue(target.position().distanceToSqr(RainStormCases.scene(h))<.001,"Unfunded source never starts teleport callback");
   p.setGameMode(GameType.CREATIVE);target.getRandom().setSeed(42);cast(h,p,new EntityHitResult(target),true);h.assertTrue(target.position().distanceToSqr(RainStormCases.scene(h))>.01 && SEHelper.getSESouls(p)==0,"Creative preserves actual native teleport with free resource account");h.succeed();
  }finally{clean(h,p);}
 }
}
