package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_wind_storm") @PrefixGameTestTemplate(false)
public final class WindStormGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"WindStudent"));}
 @GameTest(template="empty",batch="wind_0",timeoutTicks=100) public static void flyingMovementAndBilling(GameTestHelper h){WindStormCases.flyingMovementAndBilling(h,player(h));}
 @GameTest(template="empty",batch="wind_1",timeoutTicks=100) public static void flyingStaffCreativeAndAlliance(GameTestHelper h){WindStormCases.flyingStaffCreativeAndAlliance(h,player(h));}
 @GameTest(template="empty",batch="wind_2",timeoutTicks=100) public static void whirlwindNativeMotionAndStaff(GameTestHelper h){WindStormCases.whirlwindNativeMotionAndStaff(h,player(h));}
 @GameTest(template="empty",batch="wind_3",timeoutTicks=100) public static void shockingNativeDamageAndOwnership(GameTestHelper h){WindStormCases.shockingNativeDamageAndOwnership(h,player(h));}
 @GameTest(template="empty",batch="wind_4",timeoutTicks=100) public static void shockingRodAndRejectedExecutors(GameTestHelper h){WindStormCases.shockingRodAndRejectedExecutors(h,player(h));}
}
