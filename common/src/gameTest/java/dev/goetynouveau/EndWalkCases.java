package dev.goetynouveau;
import com.Polarice3.Goety.common.magic.spells.void_spells.EndWalkSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.function.Consumer;

public final class EndWalkCases {
 private static void cast(GameTestHelper h,ServerPlayer source,HitResult hit,boolean paid,AbstractAugment... a){
  var recipe=new Spell(MethodTouch.INSTANCE,GlyphCatalog.END_WALK);for(var augment:a)recipe=recipe.add(augment);
  var context=new SpellContext(h.getLevel(),recipe,source,LivingCaster.from(source),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();
  int before=SEHelper.getSESouls(source),fee=GlyphCatalog.END_WALK.focus().spell().soulCost(source,dev.goetynouveau.casting.GoetyResources.staff(source));
  fee=(int)(fee*SEHelper.soulDiscount(source));resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(source)==before-(paid&&!source.isCreative()?fee:0),"Real source payment: before="+before+", after="+SEHelper.getSESouls(source)+", native fee="+fee);
 }
 private static void ally(GameTestHelper h,ServerPlayer p,ServerPlayer q){
  q.setPos(RainStormCases.scene(h));q.setNoGravity(true);q.setGameMode(GameType.SURVIVAL);h.getLevel().addNewPlayer(q);SEHelper.setSEActive(q,true);SEHelper.setSESouls(q,12345);
  var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("walk_"+java.util.UUID.randomUUID().toString().substring(0,8));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(q.getScoreboardName(),team);
 }
 private static void returned(GameTestHelper h,ServerPlayer p,BlockPos anchor){h.assertTrue(p.position().distanceToSqr(new Vec3(anchor.getX()+.5,anchor.getY(),anchor.getZ()+.5))<.001 && !SEHelper.hasEndWalk(p),"Native return callback moves probe to original anchor and clears state: pos="+p.position()+", anchor="+anchor+", live="+SEHelper.hasEndWalk(p)+"");}
 private static void clean(GameTestHelper h,ServerPlayer p,ServerPlayer q){if(q!=null){NativeEffectHooks.removeShadowWalk(q);q.discard();}NativeEffectHooks.removeShadowWalk(p);p.discard();RainStormCases.clear(h,p);}
 public static void nativeSelfEffectAndRemoval(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var anchor=p.blockPosition();cast(h,p,new EntityHitResult(p),true,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE);
   var effect=NativeEffectHooks.shadowWalk(p);var stats=WandUtil.getStats(p,new EndWalkSpell());
   h.assertTrue(effect!=null && effect.getAmplifier()==stats.getPotency()+1 && effect.getDuration()==SpellConfig.EndWalkEffectDuration.get()+stats.getDuration()+20 && !effect.isVisible() && effect.showIcon(),"Real native effect, Ars potency/time, hidden particles and native icon");
   h.assertTrue(anchor.equals(SEHelper.getEndWalkPos(p)) && h.getLevel().dimension()==SEHelper.getEndWalkDimension(p),"Actual native anchor/dimension initialized");
   var seeker=EntityType.ZOMBIE.create(h.getLevel());seeker.setNoAi(true);seeker.setNoGravity(true);seeker.setPos(p.position().add(0,0,3));h.getLevel().addFreshEntity(seeker);seeker.setTarget(p);
   h.assertTrue(seeker.getTarget()==null,"Actual native target event prevents ordinary mob targeting during walk");
   p.setPos(p.position().add(0,0,8));h.assertTrue(NativeEffectHooks.removeShadowWalk(p),"Actual native effect removal succeeds");returned(h,p,anchor);h.succeed();
  }finally{clean(h,p,null);}
 }
 public static void alliedSourceEquipmentAndAnchorIsolation(GameTestHelper h,ServerPlayer p,ServerPlayer q,Consumer<ServerPlayer> equip){
  RainStormCases.prepare(h,p);try{
   ally(h,p,q);equip.accept(p);var nativeSpell=new EndWalkSpell();var nativeStats=WandUtil.getStats(p,nativeSpell);
   h.assertTrue(WandUtil.enchantedFocus(p) && !WandUtil.enchantedFocus(q),"Fixture has actual original-source enchanted focus, recipient has none");
   nativeSpell.SpellResult(h.getLevel(),p,dev.goetynouveau.casting.GoetyResources.staff(p),nativeStats);var baseline=NativeEffectHooks.shadowWalk(p);h.assertTrue(baseline!=null,"Unscoped original Goety callback still applies its own player state");
   int duration=baseline.getDuration(),potency=baseline.getAmplifier();var sourceAnchor=SEHelper.getEndWalkPos(p);var allyAnchor=q.blockPosition();int qBalance=SEHelper.getSESouls(q);
   cast(h,p,new EntityHitResult(q),true,AugmentExtendTime.INSTANCE,AugmentAmplify.INSTANCE);var effect=NativeEffectHooks.shadowWalk(q);
   h.assertTrue(effect!=null && effect.getDuration()==duration+20 && effect.getAmplifier()==potency+1 && SEHelper.getSESouls(q)==qBalance,"Source native enchantments preserved independently of allied executor and source alone pays");
   h.assertTrue(sourceAnchor.equals(SEHelper.getEndWalkPos(p)) && allyAnchor.equals(SEHelper.getEndWalkPos(q)),"Two native return anchors stay independent");
   q.setPos(q.position().add(0,0,9));cast(h,p,new EntityHitResult(q),false);h.assertTrue(allyAnchor.equals(SEHelper.getEndWalkPos(q)) && NativeEffectHooks.shadowWalk(q)==effect,"Repeat cannot overwrite live allied return anchor or charge again");
   NativeEffectHooks.removeShadowWalk(q);returned(h,q,allyAnchor);h.assertTrue(NativeEffectHooks.shadowWalk(p)==baseline && sourceAnchor.equals(SEHelper.getEndWalkPos(p)),"Allied return does not clean source's separate native walk");h.succeed();
  }finally{clean(h,p,q);}
 }
 public static void rejectedCreativeAndDeniedEffect(GameTestHelper h,ServerPlayer p,ServerPlayer q,Consumer<Runnable> deny){
  RainStormCases.prepare(h,p);try{
   q.setPos(RainStormCases.scene(h));q.setGameMode(GameType.SURVIVAL);h.getLevel().addNewPlayer(q);
   var board=h.getLevel().getScoreboard();var oldTeam=board.getPlayersTeam(q.getScoreboardName());if(oldTeam!=null)board.removePlayerFromTeam(q.getScoreboardName(),oldTeam);
   h.assertTrue(!dev.goetynouveau.casting.GoetyBreathGeometry.allied(p,q),"Reused player names must not inherit prior test alliances");
   cast(h,p,new EntityHitResult(q),false);var mob=RainStormCases.host(h,p);cast(h,p,new EntityHitResult(mob),false);cast(h,p,new BlockHitResult(p.position(),Direction.UP,p.blockPosition(),false),false);
   h.assertTrue(NativeEffectHooks.shadowWalk(q)==null && !SEHelper.hasEndWalk(q) && !SEHelper.hasEndWalk(p),"Enemy players, allied mobs and blocks do not receive player walk state");
   deny.accept(()->cast(h,p,new EntityHitResult(p),false));h.assertTrue(NativeEffectHooks.shadowWalk(p)==null && !SEHelper.hasEndWalk(p),"Rejected real effect leaves no orphan anchor and no payment");
   SEHelper.setSESouls(p,0);cast(h,p,new EntityHitResult(p),false);h.assertTrue(!SEHelper.hasEndWalk(p),"Empty source cannot apply real native effect");
   p.setGameMode(GameType.CREATIVE);cast(h,p,new EntityHitResult(p),true);h.assertTrue(NativeEffectHooks.shadowWalk(p)!=null && SEHelper.getSESouls(p)==0,"Creative applies complete native state for free");h.succeed();
  }finally{clean(h,p,q);}
 }
 public static void actualExpiryProjectileBreakAndStaleAnchor(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> spawnArrow){
  RainStormCases.prepare(h,p);var original=SpellConfig.EndWalkEffectDuration.get();try{
   SpellConfig.EndWalkEffectDuration.set(3);var anchor=p.blockPosition();cast(h,p,new EntityHitResult(p),true);p.setPos(p.position().add(0,0,7));
   for(int i=0;i<6 && NativeEffectHooks.shadowWalk(p)!=null;i++)p.baseTick();
   h.assertTrue(NativeEffectHooks.shadowWalk(p)==null,"Actual living baseTick expires the native effect");returned(h,p,anchor);
   cast(h,p,new EntityHitResult(p),true);anchor=p.blockPosition();p.setPos(p.position().add(0,0,5));
   spawnArrow.accept(p);
   h.assertTrue(NativeEffectHooks.shadowWalk(p)==null,"Actual native projectile-join event interrupts walk");returned(h,p,anchor);
   SEHelper.setEndWalk(p,anchor,h.getLevel().dimension());cast(h,p,new EntityHitResult(p),false);h.assertTrue(anchor.equals(SEHelper.getEndWalkPos(p)),"A pre-existing native anchor without visible effect is never overwritten");SEHelper.removeEndWalk(p);h.succeed();
  }finally{SpellConfig.EndWalkEffectDuration.set(original);clean(h,p,null);}
 }
}
