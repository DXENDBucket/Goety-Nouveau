package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_endwalk") @PrefixGameTestTemplate(false)
public final class EndWalkGameTests {
 // FakePlayer's connection intentionally discards teleport packets. Observe the real native callback here.
 public static final class WalkProbe extends net.minecraftforge.common.util.FakePlayer {
  boolean rejectEffects;
  WalkProbe(net.minecraft.server.level.ServerLevel world,com.mojang.authlib.GameProfile profile){super(world,profile);}
  @Override public void teleportTo(double x,double y,double z){setPos(x,y,z);}
  @Override public boolean canBeAffected(net.minecraft.world.effect.MobEffectInstance effect){return !rejectEffects && super.canBeAffected(effect);}
 }
 private static WalkProbe player(GameTestHelper h,String name){return new WalkProbe(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name));}
 @GameTest(template="empty",batch="walk_0",timeoutTicks=100) public static void nativeSelfEffectAndRemoval(GameTestHelper h){EndWalkCases.nativeSelfEffectAndRemoval(h,player(h,"WalkSource"));}
 @GameTest(template="empty",batch="walk_1",timeoutTicks=100) public static void alliedSourceEquipmentAndAnchorIsolation(GameTestHelper h){EndWalkCases.alliedSourceEquipmentAndAnchorIsolation(h,player(h,"WalkSource"),player(h,"WalkAlly"),p->{
  var focus=new net.minecraft.world.item.ItemStack(com.Polarice3.Goety.common.items.ModItems.END_WALK_FOCUS.get());focus.enchant(com.Polarice3.Goety.common.enchantments.ModEnchantments.DURATION.get(),2);focus.enchant(com.Polarice3.Goety.common.enchantments.ModEnchantments.POTENCY.get(),1);
  var wand=new net.minecraft.world.item.ItemStack(com.Polarice3.Goety.common.items.ModItems.VOID_STAFF.get());com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler.get(wand).insertItem(focus);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,wand);for(int i=0;i<2;i++)wand.getItem().inventoryTick(wand,h.getLevel(),p,0,true);
 });}
 @GameTest(template="empty",batch="walk_2",timeoutTicks=100) public static void rejectedCreativeAndDeniedEffect(GameTestHelper h){var p=player(h,"WalkSource");EndWalkCases.rejectedCreativeAndDeniedEffect(h,p,player(h,"WalkAlly"),run->{p.rejectEffects=true;try{run.run();}finally{p.rejectEffects=false;}});}

 @GameTest(template="empty",batch="walk_3",timeoutTicks=100) public static void actualExpiryProjectileBreakAndStaleAnchor(GameTestHelper h){EndWalkCases.actualExpiryProjectileBreakAndStaleAnchor(h,player(h,"WalkSource"),p->h.getLevel().addFreshEntity(new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),p)));}
}
