package dev.goetynouveau;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.common.blocks.entities.HoleBlockEntity;
import com.Polarice3.Goety.common.magic.spells.void_spells.TunnelSpell;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import dev.arsconflux.api.context.*;
import dev.goetynouveau.casting.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.phys.*;
import java.util.function.*;
public final class TunnelCases {
 private static BlockPos center(GameTestHelper h){return BlockPos.containing(RainStormCases.scene(h));}
 private static BlockHitResult hit(BlockPos pos,Direction face){return new BlockHitResult(Vec3.atCenterOf(pos),face,pos,false);}
 private static void clean(GameTestHelper h,ServerPlayer p){
  var c=center(h);for(var pos:BlockPos.betweenClosed(c.offset(-12,-16,-12),c.offset(12,16,20)))if(h.getLevel().getBlockEntity(pos) instanceof HoleBlockEntity hole)h.getLevel().setBlock(pos,hole.oldBlock==null?Blocks.AIR.defaultBlockState():hole.oldBlock,2);
  p.discard();RainStormCases.clear(h,p);
 }
 private static void prepare(GameTestHelper h,ServerPlayer p){RainStormCases.prepare(h,p);for(var pos:BlockPos.betweenClosed(center(h).offset(-4,-5,-4),center(h).offset(4,5,8)))h.getLevel().setBlock(pos,Blocks.AIR.defaultBlockState(),2);}
 private static void wall(GameTestHelper h,int half,int depth){for(int x=-half;x<=half;x++)for(int y=-half;y<=half;y++)for(int z=0;z<depth;z++)h.getLevel().setBlock(center(h).offset(x,y,z),Blocks.STONE.defaultBlockState(),2);}
 private static int cast(GameTestHelper h,ServerPlayer p,HitResult hit,int payments,AbstractAugment... augments){
  var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.TUNNEL);for(var a:augments)spell=spell.add(a);
  var context=new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);context.nextPart();
  int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(context,GlyphCatalog.TUNNEL.focus().spell(),java.util.List.of(augments)).amount();resolver.onResolveEffect(h.getLevel(),hit);
  h.assertTrue(SEHelper.getSESouls(p)==before-(p.isCreative()?0:payments*fee),"Native first-hole result determines payment: before="+before+", after="+SEHelper.getSESouls(p));return fee;
 }
 private static void configuredCast(GameTestHelper h,ServerPlayer p,BlockPos pos,Direction face,int range,int life,AbstractAugment... augments){
  int oldRange=SpellConfig.TunnelDefaultDistance.get(),oldLife=SpellConfig.TunnelDefaultLifespan.get();try{SpellConfig.TunnelDefaultDistance.set(range);SpellConfig.TunnelDefaultLifespan.set(life);cast(h,p,hit(pos,face),1,augments);}finally{SpellConfig.TunnelDefaultDistance.set(oldRange);SpellConfig.TunnelDefaultLifespan.set(oldLife);}
 }
 public static void realRemoteExpansionAndRestoration(GameTestHelper h,ServerPlayer p){
  prepare(h,p);wall(h,2,4);var pos=center(h);configuredCast(h,p,pos,Direction.NORTH,3,40);
  var root=(HoleBlockEntity)h.getLevel().getBlockEntity(pos);h.assertTrue(root!=null && root.direction==Direction.NORTH && root.count==4 && root.radius==3 && root.oldBlock.is(Blocks.STONE) && GoetyTunnelHoles.marked(root),"Real native initial hole and original block at remote Ars hit");
  h.runAfterDelay(8,()->{
   for(int z=0;z<3;z++)for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)h.assertTrue(h.getLevel().getBlockEntity(pos.offset(x,y,z)) instanceof HoleBlockEntity hole && GoetyTunnelHoles.marked(hole),"Actual native 3x3 expansion propagates marker across each depth");
   h.assertTrue(h.getLevel().getBlockState(pos.offset(0,0,3)).is(Blocks.STONE) && h.getLevel().getBlockState(pos.offset(2,0,0)).is(Blocks.STONE),"Original native depth/width limits retained");
   h.runAfterDelay(40,()->{for(int z=0;z<4;z++)for(int x=-2;x<=2;x++)for(int y=-2;y<=2;y++)h.assertTrue(h.getLevel().getBlockState(pos.offset(x,y,z)).is(Blocks.STONE),"Every original solid restored without mining drops");clean(h,p);h.succeed();});
  });
 }
 public static void staffWidthDurationAndProtectedBlocks(GameTestHelper h,ServerPlayer p){
  prepare(h,p);wall(h,3,2);var pos=center(h);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOID_STAFF.get()));
  h.assertTrue(GlyphCatalog.TUNNEL.focus().spell() instanceof TunnelSpell s && s.rightStaff(p.getOffhandItem()),"Native void staff branch");
  h.getLevel().setBlock(pos.offset(2,0,0),Blocks.CHEST.defaultBlockState(),2);h.getLevel().setBlock(pos.offset(-2,0,0),Blocks.BEDROCK.defaultBlockState(),2);
  var original=Blocks.STONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING,Direction.EAST);h.getLevel().setBlock(pos,original,2);configuredCast(h,p,pos,Direction.NORTH,1,40,AugmentExtendTime.INSTANCE);
  var root=(HoleBlockEntity)h.getLevel().getBlockEntity(pos);h.assertTrue(root.radius==5 && root.lifespan==60 && root.oldBlock.equals(original),"Native 5-wide staff footprint and additive twenty-tick time augment");
  h.runAfterDelay(5,()->{
   h.assertTrue(h.getLevel().getBlockState(pos.offset(2,2,0)).is(ModBlocks.HOLE.get()) && h.getLevel().getBlockState(pos.offset(3,0,0)).is(Blocks.STONE),"Actual native staff ring, not arbitrary synthetic AOE");
   h.assertTrue(h.getLevel().getBlockState(pos.offset(2,0,0)).is(Blocks.CHEST) && h.getLevel().getBlockState(pos.offset(-2,0,0)).is(Blocks.BEDROCK),"Native block-entity and unbreakable rejection retained during expansion");
   h.runAfterDelay(60,()->{h.assertTrue(h.getLevel().getBlockState(pos).equals(original) && h.getLevel().getBlockState(pos.offset(2,2,0)).is(Blocks.STONE),"Native original state including stair facing restores");clean(h,p);h.succeed();});
  });
 }
 public static void persistentDownwardFaceAndBoundary(GameTestHelper h,ServerPlayer p,Function<HoleBlockEntity,CompoundTag> save,BiConsumer<HoleBlockEntity,CompoundTag> load,Function<HoleBlockEntity,CompoundTag> network){
  prepare(h,p);try{
   var pos=center(h);h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);h.getLevel().setBlock(pos.above(),Blocks.STONE.defaultBlockState(),2);configuredCast(h,p,pos,Direction.DOWN,2,60);
   var root=(HoleBlockEntity)h.getLevel().getBlockEntity(pos);root.life=7;var tag=save.apply(root);var packet=network.apply(root);
   h.assertTrue(tag.getInt("Life")==7 && tag.getInt("Count")==3 && packet.getInt("Lifespan")==60 && packet.getBoolean(GoetyTunnelHoles.MARKER),"Real native save/update-tag entrypoints include hole data and marker");
   var restored=new HoleBlockEntity(pos,ModBlocks.HOLE.get().defaultBlockState());restored.setLevel(h.getLevel());load.accept(restored,tag);
   h.assertTrue(restored.direction==Direction.DOWN && restored.life==7 && restored.lifespan==60 && restored.count==3 && restored.oldBlock.is(Blocks.STONE) && GoetyTunnelHoles.marked(restored),"Complete native save/load preserves DOWN ordinal zero, remaining life and original block");
   h.getLevel().setBlockEntity(restored);restored.life=0;restored.tick();h.assertTrue(h.getLevel().getBlockEntity(pos.above()) instanceof HoleBlockEntity child && child.direction==Direction.DOWN && GoetyTunnelHoles.marked(child),"Reloaded downward face expands upward into the correct native depth");
   var absent=new BlockPos(26000000,100,21000000);h.assertTrue(!h.getLevel().hasChunkAt(absent),"Probe chunk starts unloaded");
   var view=new GoetyNativeGeometry.View(CastContext.detached(p,hit(pos,Direction.DOWN),null),p,(TunnelSpell)GlyphCatalog.TUNNEL.focus().spell());
   h.assertTrue(!GoetyNativeGeometry.scoped(view,()->TunnelSpell.createHole(h.getLevel(),absent,Direction.UP,2,false,20)) && !h.getLevel().hasChunkAt(absent),"Initial native createHole is guarded before its block lookup");
   h.assertTrue(!GoetyTunnelHoles.child(h.getLevel(),absent,Direction.UP,2,false,20) && !h.getLevel().hasChunkAt(absent),"Asynchronous native child boundary cannot load chunks");
   h.assertTrue(!GoetyTunnelHoles.child(h.getLevel(),new BlockPos(pos.getX(),h.getLevel().getMaxBuildHeight(),pos.getZ()),Direction.UP,2,false,20),"Native child build boundary rejected");
   var nativePos=pos.offset(7,0,0);h.getLevel().setBlock(nativePos,Blocks.STONE.defaultBlockState(),2);h.assertTrue(TunnelSpell.createHole(h.getLevel(),nativePos,Direction.NORTH,2,false,20),"Ordinary native tunnel still creates holes outside adapter scope");
   h.assertTrue(!GoetyTunnelHoles.marked((HoleBlockEntity)h.getLevel().getBlockEntity(nativePos)) && GoetyNativeGeometry.current()==null,"Unmarked native hole and restored synchronous scope");h.succeed();
  }finally{clean(h,p);}
 }
 public static void largeRangeAndNativeIsolation(GameTestHelper h,ServerPlayer p){
  prepare(h,p);BlockPos nativePos=null;try{
   var pos=center(h);h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);var spell=(TunnelSpell)GlyphCatalog.TUNNEL.focus().spell();var stats=WandUtil.getStats(p,spell);stats.setRange(130);
   var frame=CastContext.detached(p,hit(pos,Direction.NORTH),null);var view=new GoetyNativeGeometry.View(frame,p,spell,null,stats);
   GoetyNativeGeometry.scoped(view,()->{spell.blockResult(h.getLevel(),p,ItemStack.EMPTY,pos,Direction.NORTH,stats);return null;});
   h.assertTrue(h.getLevel().getBlockEntity(pos) instanceof HoleBlockEntity hole && hole.count==131 && GoetyTunnelHoles.marked(hole),"Addon retains int native depth instead of byte overflow at range 130");
   p.setYRot(0);p.setXRot(0);nativePos=BlockPos.containing(p.getEyePosition().add(0,0,4));
   for(int z=0;z<4;z++)h.getLevel().setBlock(BlockPos.containing(p.getEyePosition().add(0,0,z)),Blocks.AIR.defaultBlockState(),2);
   h.getLevel().setBlock(nativePos,Blocks.STONE.defaultBlockState(),2);spell.blockResult(h.getLevel(),p,ItemStack.EMPTY,nativePos,Direction.NORTH,stats);
   h.assertTrue(h.getLevel().getBlockEntity(nativePos) instanceof HoleBlockEntity hole && hole.count==(byte)131 && !GoetyTunnelHoles.marked(hole),"Original native callback outside the scope is untouched, including its narrow count");h.succeed();
  }finally{if(nativePos!=null && h.getLevel().getBlockEntity(nativePos) instanceof HoleBlockEntity hole)h.getLevel().setBlock(nativePos,hole.oldBlock,2);clean(h,p);}
 }
 public static void invalidRecipientsResourcesAndLifetime(GameTestHelper h,ServerPlayer p){
  prepare(h,p);try{
   var pos=center(h);h.getLevel().setBlock(pos,Blocks.BEDROCK.defaultBlockState(),2);cast(h,p,hit(pos,Direction.NORTH),0);
   h.getLevel().setBlock(pos,Blocks.CHEST.defaultBlockState(),2);cast(h,p,hit(pos,Direction.NORTH),0);
   h.getLevel().setBlock(pos,Blocks.WATER.defaultBlockState(),2);cast(h,p,hit(pos,Direction.NORTH),0);
   var mob=RainStormCases.host(h,p);cast(h,p,new EntityHitResult(mob),0);h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);
   var context=new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.TUNNEL),p,LivingCaster.from(p),ItemStack.EMPTY);var resolver=new SpellResolver(context);int balance=SEHelper.getSESouls(p),old=SpellConfig.TunnelDefaultLifespan.get();
   try{SpellConfig.TunnelDefaultLifespan.set(40);GlyphCatalog.TUNNEL.onResolve(hit(pos,Direction.NORTH),h.getLevel(),p,new SpellStats.Builder().setDurationModifier(-2).build(),context,resolver);}finally{SpellConfig.TunnelDefaultLifespan.set(old);}
   h.assertTrue(SEHelper.getSESouls(p)==balance && h.getLevel().getBlockState(pos).is(Blocks.STONE),"Nonpositive lifetime is rejected before creation/payment");
   SEHelper.setSESouls(p,0);cast(h,p,hit(pos,Direction.NORTH),0);h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.STONE),"Unfunded cast makes no holes");p.setGameMode(GameType.CREATIVE);cast(h,p,hit(pos,Direction.NORTH),1);h.assertTrue(h.getLevel().getBlockState(pos).is(ModBlocks.HOLE.get()) && SEHelper.getSESouls(p)==0,"Creative uses real native tunnel for free");h.succeed();
  }finally{clean(h,p);}
 }
}
