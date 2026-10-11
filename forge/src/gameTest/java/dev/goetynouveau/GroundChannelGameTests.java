package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_ground_channels") @PrefixGameTestTemplate(false)
public final class GroundChannelGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"GroundStudent"));}
 @GameTest(template="empty",batch="void_emission",timeoutTicks=150) public static void voidNativeEmissionAndOwnership(GameTestHelper h){GroundChannelCases.voidNativeEmissionAndOwnership(h,player(h));}
 @GameTest(template="empty",batch="void_staff",timeoutTicks=100) public static void voidStaffAndAllegiance(GameTestHelper h){GroundChannelCases.voidStaffAndAllegiance(h,player(h));}
 @GameTest(template="empty",batch="quake",timeoutTicks=100) public static void quakeNativeForwardAndStaff(GameTestHelper h){GroundChannelCases.quakeNativeForwardAndStaff(h,player(h));}
 @GameTest(template="empty",batch="quake_rings",timeoutTicks=100) public static void quakeRingsAndVoidBoundary(GameTestHelper h){GroundChannelCases.quakeRingsAndVoidBoundary(h,player(h));}
 @GameTest(template="empty",batch="ward",timeoutTicks=100) public static void wardingNativePlayerData(GameTestHelper h){GroundChannelCases.wardingNativePlayerData(h,player(h),player(h));}
 @GameTest(template="empty",batch="rejected",timeoutTicks=100) public static void rejectedExecutors(GameTestHelper h){GroundChannelCases.rejectedExecutors(h,player(h));}
}
