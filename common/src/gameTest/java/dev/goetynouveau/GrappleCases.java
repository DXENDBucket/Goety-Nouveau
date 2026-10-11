package dev.goetynouveau;
import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import com.Polarice3.Goety.common.magic.spells.wild.GrappleSpell;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.api.GoetyGrappleAccess;
import dev.goetynouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.function.Consumer;
public final class GrappleCases {
 private static int cast(GameTestHelper h,ServerPlayer p,HitResult hit,boolean paid,AbstractAugment... augments){
  var recipe=new Spell(MethodTouch.INSTANCE,GlyphCatalog.GRAPPLE);for(var a:augments)recipe=recipe.add(a);
  ItemStack tool=p.getOffhandItem().is(Items.BOOK)?p.getOffhandItem().copy():ItemStack.EMPTY;
  var context=new SpellContext(h.getLevel(),recipe,p,LivingCaster.from(p),tool);var resolver=new SpellResolver(context);context.nextPart();
  int before=SEHelper.getSESouls(p),fee=GlyphCatalog.GRAPPLE.focus().spell().defaultSoulCost();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(paid&&!p.isCreative()?fee:0),"Actual native launch fee or free retract: fee="+fee+", before="+before+", after="+SEHelper.getSESouls(p));return fee;
 }
 private static VineHook hook(ServerPlayer p){return SEHelper.getGrappling(p) instanceof VineHook h?h:null;}
 private static void ally(GameTestHelper h,ServerPlayer p,ServerPlayer q){
  q.setPos(RainStormCases.scene(h));q.setNoGravity(true);q.setGameMode(GameType.SURVIVAL);h.getLevel().addNewPlayer(q);SEHelper.setSEActive(q,true);SEHelper.setSESouls(q,100000);
  var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("hook_"+java.util.UUID.randomUUID().toString().substring(0,8));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(q.getScoreboardName(),team);
 }
 private static void clean(GameTestHelper h,ServerPlayer p,ServerPlayer q){if(q!=null)q.discard();p.discard();RainStormCases.clear(h,p);ExecutionSessions.tick();}
 public static void actualHookAttachmentNativeForceAndFreeRetract(GameTestHelper h,ServerPlayer p,Consumer<ServerPlayer> nativeTick){
  RainStormCases.prepare(h,p);try{
   p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WILD_STAFF.get()));cast(h,p,new EntityHitResult(p),true);
   var hook=hook(p);h.assertTrue(hook!=null && hook.getOwner()==p && hook.isStaff() && ((GoetyGrappleAccess)hook).goetyNouveau$managed(),"Actual native player hook with original staff flag");
   var wall=BlockPos.containing(p.getEyePosition().add(0,0,6));for(int x=-1;x<=1;x++)for(int y=-2;y<=2;y++)h.getLevel().setBlock(wall.offset(x,y,0),Blocks.STONE.defaultBlockState(),2);
   for(int i=0;i<4 && !hook.isAttached();i++)hook.tick();h.assertTrue(hook.isAttached() && hook.getLength()>=1.5 && !hook.isRemoved(),"Original native collision attaches and synchronizes rope length without a Goety wand");
   p.setDeltaMovement(Vec3.ZERO);p.fallDistance=10;nativeTick.accept(p);
   h.assertTrue(p.getDeltaMovement().lengthSqr()>0.001 && p.fallDistance==0,"Actual native player event applies rope force and Wild-staff fall reset");
   SEHelper.setSESouls(p,0);ExecutionSessions.tick();h.assertTrue(!hook.isRemoved(),"Native grapple does not invent continuous soul drain after paid launch");cast(h,p,new EntityHitResult(p),false);ExecutionSessions.tick();h.assertTrue(hook.isRemoved() && hook(p)==null,"Free second invocation performs native retrieve and clears the original slot");h.succeed();
  }finally{clean(h,p,null);}
 }
 public static void alliedQuoteSourceRoleAndHandMetadata(GameTestHelper h,ServerPlayer p,ServerPlayer q){
  RainStormCases.prepare(h,p);try{
   ally(h,p,q);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.BOOK));cast(h,p,new EntityHitResult(p),true);var self=hook(p);
   h.assertTrue(((GoetyGrappleAccess)self).goetyNouveau$hand()==1 && self.getEntityData().getNonDefaultValues().stream().anyMatch(v->v.value() instanceof Byte b && b==1),"Cloned off-hand tool selects a real synchronized rope-hand field");
   h.assertTrue(new GrappleSpell().soulCost(p,ItemStack.EMPTY)==0,"Native caster already has a hook and would quote zero");int allyBalance=SEHelper.getSESouls(q);
   cast(h,p,new EntityHitResult(q),true);var remote=hook(q);h.assertTrue(remote!=null && remote.getOwner()==q && GoetyAreaProvenance.source(remote,null)==p && SEHelper.getSESouls(q)==allyBalance,"Target's empty slot charges original source and actual owner is recipient");
   var board=h.getLevel().getScoreboard();board.removePlayerFromTeam(q.getScoreboardName(),board.getPlayersTeam(q.getScoreboardName()));remote.tick();ExecutionSessions.tick();
   h.assertTrue(remote.isRemoved() && hook(q)==null && !self.isRemoved() && hook(p)==self,"Alliance loss ends only the remote grapple");h.succeed();
  }finally{clean(h,p,q);}
 }
 public static void nativeSlotReplacementAndLeaseIsolation(GameTestHelper h,ServerPlayer p,ServerPlayer q){
  RainStormCases.prepare(h,p);try{
   ally(h,p,q);cast(h,p,new EntityHitResult(p),true);var first=hook(p);
   cast(h,q,new EntityHitResult(p),false);h.assertTrue(hook(p)==first && !first.isRemoved(),"A different source cannot steal or retract the existing lease");
   var nativeHook=new VineHook(h.getLevel(),p,2.5f);h.getLevel().addFreshEntity(nativeHook);ExecutionSessions.tick();
   h.assertTrue(first.isRemoved() && hook(p)==nativeHook && !nativeHook.isRemoved(),"Closing old managed hook cannot clear a newer native slot");
   cast(h,p,new EntityHitResult(p),false);h.assertTrue(hook(p)==nativeHook && !nativeHook.isRemoved(),"Ars activation does not take over an existing unmarked native wand hook");
   nativeHook.tick();h.assertTrue(nativeHook.isRemoved() && hook(p)==null,"Unmarked hook retains native missing-Grapple-wand retraction condition");h.succeed();
  }finally{clean(h,p,q);}
 }
 public static void invalidRecipientsCreativeAndBoundedExpiry(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);SEHelper.setSESouls(p,0);var pos=p.blockPosition();
  cast(h,p,new BlockHitResult(p.position(),Direction.UP,pos,false),false);var mob=RainStormCases.host(h,p);cast(h,p,new EntityHitResult(mob),false);cast(h,p,new EntityHitResult(p),false);h.assertTrue(hook(p)==null,"Non-player targets and empty account cannot create a hook");
  p.setGameMode(GameType.CREATIVE);cast(h,p,new EntityHitResult(p),false,AugmentDurationDown.INSTANCE);h.assertTrue(hook(p)==null,"Zero lifespan remains invalid in creative");cast(h,p,new EntityHitResult(p),true);var hook=hook(p);h.assertTrue(hook!=null && SEHelper.getSESouls(p)==0,"Creative native hook launches for free");hook.setNoGravity(true);hook.setDeltaMovement(Vec3.ZERO);
  int ticks=GoetyChannelCasting.boundedTicks(0,0);h.runAfterDelay(ticks-2,()->{
   h.assertTrue(!hook.isRemoved() && hook(p)==hook,"Stationary managed hook survives until its finite duration");
   h.runAfterDelay(5,()->{h.assertTrue(hook.isRemoved() && hook(p)==null,"Finite expiry discards native hook and clears player reference");clean(h,p,null);h.succeed();});
  });
 }
}
