package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_blink") @PrefixGameTestTemplate(false)
public final class BlinkGameTests {
 public static final class BlinkProbe extends net.minecraftforge.common.util.FakePlayer {
  BlinkProbe(net.minecraft.server.level.ServerLevel world){super(world,new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"BlinkSource"));}
  @Override public void teleportTo(double x,double y,double z){setPos(x,y,z);}
 }
 private static BlinkProbe player(GameTestHelper h){return new BlinkProbe(h.getLevel());}
 public static final class Gate {
  private final net.minecraft.world.entity.LivingEntity target;int calls;Gate(net.minecraft.world.entity.LivingEntity target){this.target=target;}
  @net.minecraftforge.eventbus.api.SubscribeEvent public void teleport(net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity e){if(e.getEntity()==target){calls++;e.setCanceled(true);}}
 }
 @GameTest(template="empty",batch="blink_0",timeoutTicks=150) public static void nativeSelfForwardAndWandIsolation(GameTestHelper h){BlinkCases.nativeSelfForwardAndWandIsolation(h,player(h));}
 @GameTest(template="empty",batch="blink_1",timeoutTicks=150) public static void alliedSourceRangeAndBodyIsolation(GameTestHelper h){BlinkCases.alliedSourceRangeAndBodyIsolation(h,player(h),p->{
  var focus=new net.minecraft.world.item.ItemStack(com.Polarice3.Goety.common.items.ModItems.BLINK_FOCUS.get());focus.enchant(com.Polarice3.Goety.common.enchantments.ModEnchantments.RANGE.get(),4);
  var wand=new net.minecraft.world.item.ItemStack(com.Polarice3.Goety.common.items.ModItems.VOID_STAFF.get());com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler.get(wand).insertItem(focus);p.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND,wand);for(int i=0;i<2;i++)wand.getItem().inventoryTick(wand,h.getLevel(),p,0,true);
 });}
 @GameTest(template="empty",batch="blink_2",timeoutTicks=150) public static void nativeEntityApproachAndSneakRandom(GameTestHelper h){BlinkCases.nativeEntityApproachAndSneakRandom(h,player(h));}
 @GameTest(template="empty",batch="blink_3",timeoutTicks=150) public static void eventRejectionResourcesAndCreative(GameTestHelper h){BlinkCases.eventRejectionResourcesAndCreative(h,player(h),(target,run)->{var gate=new Gate(target);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(gate);try{run.run();h.assertTrue(gate.calls>0,"Actual native EnderEntity event reaches loader bus");}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(gate);}},t->t.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.Polarice3.Goety.common.effects.GoetyEffects.ENDER_GROUND.get(),100)));}
}
