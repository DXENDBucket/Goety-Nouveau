package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_utility_channels") @PrefixGameTestTemplate(false)
public final class UtilityChannelGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"UtilityStudent"));}
 @GameTest(template="empty",batch="utility_0",timeoutTicks=100) public static void telekinesisSessionIsolation(GameTestHelper h){UtilityChannelCases.telekinesisSessionIsolation(h,player(h),player(h));}
 @GameTest(template="empty",batch="utility_1",timeoutTicks=100) public static void telekinesisNativeCollisionAndRange(GameTestHelper h){UtilityChannelCases.telekinesisNativeCollisionAndRange(h,player(h));}
 @GameTest(template="empty",batch="utility_2",timeoutTicks=100) public static void telekinesisInvalidAndHurt(GameTestHelper h){UtilityChannelCases.telekinesisInvalidAndHurt(h,player(h));}
 @GameTest(template="empty",batch="utility_3",timeoutTicks=100) public static void burrowingProgressDropsAndCleanup(GameTestHelper h){UtilityChannelCases.burrowingProgressDropsAndCleanup(h,player(h),player(h));}
 @GameTest(template="empty",batch="utility_4",timeoutTicks=100) public static void burrowingLeasePermissionsAndResources(GameTestHelper h){UtilityChannelCases.burrowingLeasePermissionsAndResources(h,player(h),player(h),player(h));}
 @GameTest(template="empty",batch="utility_5",timeoutTicks=100) public static void burrowingNativeStaffAndBurning(GameTestHelper h){UtilityChannelCases.burrowingNativeStaffAndBurning(h,player(h),player(h));}
}
