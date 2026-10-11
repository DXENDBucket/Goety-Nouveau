package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_grapple") @PrefixGameTestTemplate(false)
public final class GrappleGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h,String name){return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),name));}
 @GameTest(template="empty",batch="hook_0",timeoutTicks=400) public static void actualHookAttachmentNativeForceAndFreeRetract(GameTestHelper h){GrappleCases.actualHookAttachmentNativeForceAndFreeRetract(h,player(h,"HookSource"),p->com.Polarice3.Goety.common.events.SoulEnergyEvents.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p)));}
 @GameTest(template="empty",batch="hook_1",timeoutTicks=400) public static void alliedQuoteSourceRoleAndHandMetadata(GameTestHelper h){GrappleCases.alliedQuoteSourceRoleAndHandMetadata(h,player(h,"HookSource"),player(h,"HookFriend"));}
 @GameTest(template="empty",batch="hook_2",timeoutTicks=400) public static void nativeSlotReplacementAndLeaseIsolation(GameTestHelper h){GrappleCases.nativeSlotReplacementAndLeaseIsolation(h,player(h,"HookSource"),player(h,"HookFriend"));}
 @GameTest(template="empty",batch="hook_3",timeoutTicks=400) public static void invalidRecipientsCreativeAndBoundedExpiry(GameTestHelper h){GrappleCases.invalidRecipientsCreativeAndBoundedExpiry(h,player(h,"HookSource"));}
}
