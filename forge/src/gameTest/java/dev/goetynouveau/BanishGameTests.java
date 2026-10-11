package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_banish") @PrefixGameTestTemplate(false)
public final class BanishGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"BanishSource"));}
 public static final class Gate {
  private final net.minecraft.world.entity.LivingEntity target;int calls;Gate(net.minecraft.world.entity.LivingEntity target){this.target=target;}
  @net.minecraftforge.eventbus.api.SubscribeEvent public void teleport(net.minecraftforge.event.entity.EntityTeleportEvent.EnderEntity e){if(e.getEntity()==target){calls++;e.setCanceled(true);}}
 }
 @GameTest(template="empty",batch="banish_0",timeoutTicks=150) public static void actualSelectedNativeTeleportAndLookIsolation(GameTestHelper h){BanishCases.actualSelectedNativeTeleportAndLookIsolation(h,player(h));}
 @GameTest(template="empty",batch="banish_1",timeoutTicks=150) public static void nativeRangeStaffAndFailedSearch(GameTestHelper h){BanishCases.nativeRangeStaffAndFailedSearch(h,player(h));}
 @GameTest(template="empty",batch="banish_2",timeoutTicks=150) public static void nativeSelectedRestrictionsAndTeleportEvent(GameTestHelper h){BanishCases.nativeSelectedRestrictionsAndTeleportEvent(h,player(h),t->t.addEffect(new net.minecraft.world.effect.MobEffectInstance(com.Polarice3.Goety.common.effects.GoetyEffects.ENDER_GROUND.get(),100)),(target,run)->{var gate=new Gate(target);net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(gate);try{run.run();h.assertTrue(gate.calls>0,"Actual native selected-target EnderEntity event reaches loader bus");}finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(gate);}});}
 @GameTest(template="empty",batch="banish_3",timeoutTicks=150) public static void invalidFriendlyResourcesAndCreative(GameTestHelper h){BanishCases.invalidFriendlyResourcesAndCreative(h,player(h));}
}
