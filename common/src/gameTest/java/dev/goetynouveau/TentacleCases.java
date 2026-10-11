package dev.goetynouveau;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.GulfTentacle;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import dev.arsconflux.api.context.*;
import dev.goetynouveau.casting.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;

public final class TentacleCases {
 private static Mob cow(GameTestHelper h,Vec3 pos){var e=EntityType.COW.create(h.getLevel());e.setNoAi(true);e.setNoGravity(true);e.setPos(pos);h.getLevel().addFreshEntity(e);return e;}
 private static Mob host(GameTestHelper h,ServerPlayer p){var host=RainStormCases.host(h,p);host.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));host.setYRot(0);host.setYHeadRot(0);host.yBodyRot=0;return host;}
 private static void cast(GameTestHelper h,ServerPlayer p,HitResult hit,LivingEntity owner,boolean success,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.WATER_WHIP);for(var a:augments)spell=spell.add(a);
  var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));resolver.spellContext.nextPart();
  CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null).withDamageOwner(owner));
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,GlyphCatalog.WATER_WHIP.focus().spell(),List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(success&&!p.isCreative()?fee:0),"Successful native whip pays exactly once");
 }
 private static List<GulfTentacle> tentacles(GameTestHelper h){return RainStormCases.entities(h,GulfTentacle.class).stream().filter(e->!e.isRemoved()).toList();}
 public static void nativeAttachmentWarmupDamageAndExpiry(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var host=host(h,p);var owner=cow(h,RainStormCases.scene(h).add(-10,0,0));var target=cow(h,RainStormCases.scene(h).add(0,0,3));
   cast(h,p,new EntityHitResult(host),owner,true,AugmentAmplify.INSTANCE);
   var area=tentacles(h).get(0);h.assertTrue(area.getType()==ModEntityType.GULF_TENTACLE.get() && area.getTrueOwner()==host && area.getRange()==4 && area.offset==1,"Native type, synchronized attachment owner, range and arm offset");
   for(int i=0;i<7;i++)area.tick();h.assertTrue(target.getHealth()==target.getMaxHealth(),"Native animation warmup has no premature damage");
   area.tick();h.assertTrue(target.getHealth()<target.getMaxHealth() && target.getLastHurtByMob()==owner,"Actual native trace damage belongs to explicit owner");
   float hp=target.getHealth();host.setPos(host.position().add(8,0,0));host.setYRot(90);host.setYHeadRot(90);host.yBodyRot=90;area.tick();
   h.assertTrue(area.position().distanceTo(host.getEyePosition())<2 && Math.abs(area.getYRot()-host.getYRot())<.001 && target.getHealth()==hp,"Native attached motion and live facing follow actual executor");
   for(int i=0;i<20&&!area.isRemoved();i++)area.tick();h.assertTrue(area.isRemoved(),"Native twenty-tick lifetime ends the actual entity");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
 public static void nativeStaffOffspringAndPersistence(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var host=host(h,p);var owner=cow(h,RainStormCases.scene(h).add(-10,0,0));var target=cow(h,RainStormCases.scene(h).add(0,0,3));
   p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
   cast(h,p,new EntityHitResult(host),owner,true,AugmentAmplify.INSTANCE,AugmentAmplify.INSTANCE,AugmentAmplify.INSTANCE);
   var original=tentacles(h).get(0);h.assertTrue(original.isStaff && !original.getActiveEffects().isEmpty(),"Original staff and native potency buff applied");
   int paidBalance=SEHelper.getSESouls(p);for(int i=0;i<8;i++)original.tick();
   var extra=tentacles(h).stream().filter(e->e!=original).findFirst().orElseThrow();
   h.assertTrue(!extra.isStaff && extra.getTrueOwner()==host && extra.getRange()==original.getRange()
     && !extra.getActiveEffects().isEmpty() && GoetyAreaProvenance.damageOwner(extra,null)==owner
     && GoetyAreaProvenance.source(extra,null)==p,"Actual native staff extra keeps buff, geometry, source and damage ownership without recursive staff spawning");
   h.assertTrue(SEHelper.getSESouls(p)==paidBalance,"Native staff aftermath has no synthetic second launch fee");
   var saved=new net.minecraft.nbt.CompoundTag();extra.saveWithoutId(saved);var loaded=new GulfTentacle(ModEntityType.GULF_TENTACLE.get(),h.getLevel());loaded.load(saved);
   h.assertTrue(loaded.getTrueOwner()==host && GoetyAreaProvenance.damageOwner(loaded,null)==owner && GoetyAreaProvenance.source(loaded,null)==p,"Native and adapter roles survive real NBT save/load");
   target.invulnerableTime=0;extra.life=7;extra.tick();h.assertTrue(target.getLastHurtByMob()==owner && tentacles(h).size()==2,"Native extra attacks with explicit owner and does not recursively multiply");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
 public static void alliesInterruptionAndNativeIsolation(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var host=host(h,p);var owner=cow(h,RainStormCases.scene(h).add(-10,0,0));
   cast(h,p,new EntityHitResult(host),owner,true);var area=tentacles(h).get(0);p.setPos(area.position());float hp=p.getHealth(),ownerHp=owner.getHealth();
   var friend=cow(h,RainStormCases.scene(h).add(0,0,2));var score=h.getLevel().getScoreboard();score.addPlayerToTeam(friend.getScoreboardName(),score.getPlayersTeam(p.getScoreboardName()));float friendHp=friend.getHealth();
   area.damageEntities(Set.of(p,owner,friend));h.assertTrue(p.getHealth()==hp && owner.getHealth()==ownerHp && friend.getHealth()==friendHp,"Original source friends and explicit owner excluded from native damage");
   var board=h.getLevel().getScoreboard();board.removePlayerFromTeam(host.getScoreboardName(),board.getPlayersTeam(host.getScoreboardName()));area.tick();
   h.assertTrue(area.isRemoved(),"Lost executor alliance ends attached strike");
   var nativeArea=new GulfTentacle(ModEntityType.GULF_TENTACLE.get(),h.getLevel());nativeArea.setTrueOwner(host);nativeArea.setPos(host.getEyePosition());h.getLevel().addFreshEntity(nativeArea);
   nativeArea.tick();h.assertTrue(!nativeArea.isRemoved() && !GoetyAreaProvenance.marked(nativeArea),"Unmarked native wand tentacle has no addon eligibility gate");
   nativeArea.damageEntities(Set.of(owner));h.assertTrue(owner.getHealth()<ownerHp && owner.getLastHurtByMob()==host,"Unmarked native damage still belongs to its original owner");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
 public static void rejectedRecipientsPaymentAndCreative(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var ally=host(h,p);var enemy=ally.getTarget();cast(h,p,new EntityHitResult(enemy),p,false);
   cast(h,p,new BlockHitResult(RainStormCases.scene(h),net.minecraft.core.Direction.UP,net.minecraft.core.BlockPos.containing(RainStormCases.scene(h)),false),p,false);
   SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(ally),p,false);h.assertTrue(tentacles(h).isEmpty(),"Enemy, blocks and unfunded casts produce no whip");
   p.setGameMode(GameType.CREATIVE);cast(h,p,new EntityHitResult(ally),p,true);var area=tentacles(h).get(0);ally.discard();area.tick();
   h.assertTrue(area.isRemoved(),"Creative payment bypass does not bypass executor removal");
   cast(h,p,new EntityHitResult(p),p,true);area=tentacles(h).get(0);area.handleEntityEvent((byte)4);
   h.assertTrue(area.spawnAnimationState.isStarted(),"Native spawn animation event remains operational");h.succeed();
  }finally{p.discard();RainStormCases.clear(h,p);}
 }
}
