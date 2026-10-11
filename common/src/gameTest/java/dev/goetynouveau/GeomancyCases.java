package dev.goetynouveau;
import com.Polarice3.Goety.common.entities.projectiles.ScatterMine;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;
public final class GeomancyCases {
 private static int cast(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,LivingEntity owner,int payments,Vec3 direction,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,glyph);for(var a:augments)spell=spell.add(a);
  var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));resolver.spellContext.nextPart();
  CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null).withDamageOwner(owner).withIncomingDirection(direction));
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(p.isCreative()?0:payments*fee),"Only successful native results pay: "+glyph.focus()+", fee="+fee+", before="+before+", after="+SEHelper.getSESouls(p));return fee;
 }
 private static BlockHitResult hit(BlockPos pos,Direction face){return new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false);}
 private static void clean(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);}
 private static Mob host(GameTestHelper h,ServerPlayer p){var m=RainStormCases.host(h,p);m.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));return m;}
 public static void nativeRecipesItemsAndFace(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var pos=BlockPos.containing(RainStormCases.scene(h));h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);
   cast(h,p,GlyphCatalog.PULVERIZE,hit(pos,Direction.UP),p,1,Vec3.ZERO);
   h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.COBBLESTONE),"Actual native pulverize recipe replaces stone");
   h.getLevel().setBlock(pos,Blocks.AMETHYST_BLOCK.defaultBlockState(),2);cast(h,p,GlyphCatalog.PULVERIZE,hit(pos,Direction.UP),p,1,Vec3.ZERO);
   h.assertTrue(h.getLevel().getBlockState(pos).isAir() && RainStormCases.entities(h,ItemEntity.class).stream().filter(e->e.getItem().is(Items.AMETHYST_SHARD)).mapToInt(e->e.getItem().getCount()).sum()==4,"Native item-result count and empty block result");
   p.setYRot(90);p.setXRot(0);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
   for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setBlock(pos.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
   h.getLevel().setBlock(pos.above(),Blocks.STONE.defaultBlockState(),2);cast(h,p,GlyphCatalog.PULVERIZE,hit(pos,Direction.UP),p,1,Vec3.ZERO);
   h.assertTrue(h.getLevel().getBlockState(pos.offset(1,0,1)).is(Blocks.COBBLESTONE) && h.getLevel().getBlockState(pos.above()).is(Blocks.STONE),"Native staff rectangle uses the actual Ars UP face, not source ray trace");
   h.assertTrue(GoetyNativeGeometry.current()==null,"Native scope restored");h.succeed();
  }finally{clean(h,p);}
 }
 public static void ordinaryMineGeometryRolesAndLifecycle(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var center=RainStormCases.scene(h);var owner=host(h,p).getTarget();owner.setPos(center.add(16,0,16));
   cast(h,p,GlyphCatalog.SCATTER,hit(BlockPos.containing(center),Direction.UP),owner,1,new Vec3(0,0,1),AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE,AugmentExtendTime.INSTANCE);
   var mines=RainStormCases.entities(h,ScatterMine.class);h.assertTrue(mines.size()==3,"Ordinary native scatter emits three actual mines");
   for(var mine:mines){h.assertTrue(mine.getOwner()==p && GoetyAreaProvenance.damageOwner(mine,null)==owner && mine.isSpell() && mine.lifeTicks==220 && mine.getExtraDamage()==1 && mine.getExtraRadius()==1,"Native mine flags, lifetime, augments and separate source/damage owner");mine.setNoGravity(true);}
   var front=mines.stream().max(java.util.Comparator.comparingDouble(Entity::getZ)).orElseThrow();h.assertTrue(front.getZ()>center.z+3 && Math.abs(front.getX()-(center.x))<1,"Native fan follows incoming direction at remote Ars point");
   var saved=new net.minecraft.nbt.CompoundTag();front.saveWithoutId(saved);var loaded=new ScatterMine(ModEntityType.SCATTER_MINE.get(),h.getLevel());loaded.load(saved);
   h.assertTrue(loaded.getOwner()==p && GoetyAreaProvenance.damageOwner(loaded,null)==owner && loaded.isSpell() && loaded.lifeTicks==220,"Native and adapter roles survive save/load");
   var friend=host(h,p);friend.setPos(front.position());owner.setPos(front.position());front.tickCount=20;front.tick();
   h.assertTrue(front.growTick==0 && friend.getHealth()==friend.getMaxHealth() && owner.getHealth()==owner.getMaxHealth(),"Source ally and explicit owner cannot trigger the armed mine");
   var victim=EntityType.COW.create(h.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(front.position());h.getLevel().addFreshEntity(victim);front.tick();
   h.assertTrue(front.growTick==1 && victim.getHealth()<victim.getMaxHealth() && victim.getLastDamageSource().getEntity()==owner,"Actual native enemy detection, blast damage and explicit attribution");
   for(int i=0;i<4;i++)front.tick();h.assertTrue(front.isRemoved(),"Native explosion growth ends after four aftermath ticks");
   var expired=mines.stream().filter(m->m!=front).findFirst().orElseThrow();expired.lifeTicks=1;expired.tick();h.assertTrue(expired.growTick==1,"Ordinary spell mines explode at native lifetime expiry");
   var nativeOnly=new ScatterMine(h.getLevel(),p,front.position().add(6,0,0));nativeOnly.setNoGravity(true);nativeOnly.setIsSpell();owner.setPos(nativeOnly.position());h.getLevel().addFreshEntity(nativeOnly);nativeOnly.tickCount=20;nativeOnly.tick();
   h.assertTrue(nativeOnly.growTick==1 && owner.getHealth()<owner.getMaxHealth(),"Unmarked native mines keep original owner-only filtering");h.succeed();
  }finally{clean(h,p);}
 }
 public static void staffCadenceGroupedPaymentAndExpiry(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);var selected=host(h,p);selected.getTarget().setPos(RainStormCases.scene(h).add(40,0,0));
  var floor=BlockPos.containing(selected.position()).below();for(int x=-10;x<=10;x++)for(int z=-10;z<=10;z++)h.getLevel().setBlock(floor.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
  p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
  var nativeSpell=GlyphCatalog.SCATTER.focus().spell();h.assertTrue(GoetyActiveCastProvider.preparationTicks(nativeSpell,p,p.getOffhandItem())==0,"Native staff window is execution rather than duplicate preparation");
  int fee=cast(h,p,GlyphCatalog.SCATTER,new EntityHitResult(selected),p,1,Vec3.ZERO),balance=SEHelper.getSESouls(p);
  h.assertTrue(ExecutionSessions.isCasting(p) && RainStormCases.entities(h,ScatterMine.class).size()==1,"Immediate first native staff mine and active channel");
  h.runAfterDelay(7,()->{
   h.assertTrue(RainStormCases.entities(h,ScatterMine.class).size()==2 && SEHelper.getSESouls(p)==balance,"Second native mine at five-tick cadence without extra grouped charge");
   h.runAfterDelay(98,()->{
    var mines=RainStormCases.entities(h,ScatterMine.class);h.assertTrue(mines.size()==21 && !ExecutionSessions.isCasting(p) && SEHelper.getSESouls(p)==balance-fee,"Native 21-emission window ends and next group pays once: "+mines.size());
    h.assertTrue(mines.stream().allMatch(m->m.getOwner()==p && !m.isSpell() && m.position().distanceTo(selected.position())<14),"Live allied spatial executor and original source owner, native staff flags");
    var expired=mines.get(0);expired.lifeTicks=1;expired.tick();h.assertTrue(expired.isRemoved() && expired.growTick==0,"Native staff mines quietly expire instead of exploding");clean(h,p);h.succeed();
   });
  });
 }
 public static void rejectionCreativeAndAllianceLoss(GameTestHelper h,ServerPlayer p){
  RainStormCases.prepare(h,p);try{
   var pos=BlockPos.containing(RainStormCases.scene(h));h.getLevel().setBlock(pos,Blocks.BEDROCK.defaultBlockState(),2);
   cast(h,p,GlyphCatalog.PULVERIZE,hit(pos,Direction.UP),p,0,Vec3.ZERO,AugmentAOE.INSTANCE);
   // Neo Ars queries block resolve capabilities before calling the effect. Test our own boundary directly.
   var absent=new BlockPos(26000000,100,23000000);h.assertTrue(!h.getLevel().hasChunkAt(absent),"Boundary probe starts in an unloaded chunk");
   var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.PULVERIZE),p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);int before=SEHelper.getSESouls(p);
   GlyphCatalog.PULVERIZE.onResolve(hit(absent,Direction.UP),h.getLevel(),p,new SpellStats.Builder().build(),context,resolver);
   h.assertTrue(!h.getLevel().hasChunkAt(absent) && SEHelper.getSESouls(p)==before,"Addon rejects absent chunks before recipe lookup without payment or loading");
   SEHelper.setSESouls(p,0);cast(h,p,GlyphCatalog.SCATTER,hit(pos,Direction.UP),p,0,Vec3.ZERO);h.assertTrue(RainStormCases.entities(h,ScatterMine.class).isEmpty(),"Empty resources produce no mines");
   p.setGameMode(GameType.CREATIVE);cast(h,p,GlyphCatalog.SCATTER,hit(pos,Direction.UP),p,1,Vec3.ZERO);h.assertTrue(RainStormCases.entities(h,ScatterMine.class).size()==3,"Creative native mines are free");RainStormCases.entities(h,ScatterMine.class).forEach(Entity::discard);
   var selected=host(h,p);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
   cast(h,p,GlyphCatalog.SCATTER,hit(pos,Direction.UP),p,0,Vec3.ZERO);cast(h,p,GlyphCatalog.SCATTER,new EntityHitResult(selected.getTarget()),p,0,Vec3.ZERO);h.assertTrue(!ExecutionSessions.isCasting(p),"Staff channel rejects nonliving locations and enemies");
   cast(h,p,GlyphCatalog.SCATTER,new EntityHitResult(selected),p,1,Vec3.ZERO);var board=h.getLevel().getScoreboard();board.removePlayerFromTeam(selected.getScoreboardName(),board.getPlayersTeam(selected.getScoreboardName()));ExecutionSessions.tick();
   h.assertTrue(!ExecutionSessions.isCasting(p),"Lost alliance stops staff deployment");h.succeed();
  }finally{clean(h,p);}
 }
}
