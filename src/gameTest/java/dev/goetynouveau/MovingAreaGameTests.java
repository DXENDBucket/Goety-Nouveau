package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_moving_areas") @PrefixGameTestTemplate(false)
public final class MovingAreaGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"AreaStudent"));}
 @GameTest(template="empty",batch="area_0",timeoutTicks=100) public static void cycloneTargetCloneAndFriends(GameTestHelper h){MovingAreaCases.cycloneTargetCloneAndFriends(h,player(h));}
 @GameTest(template="empty",batch="area_1",timeoutTicks=100) public static void nativeFireVariantAndSourceGear(GameTestHelper h){MovingAreaCases.nativeFireVariantAndSourceGear(h,player(h));}
 @GameTest(template="empty",batch="area_2",timeoutTicks=100) public static void tidalNativeTravelDamageAndStaff(GameTestHelper h){MovingAreaCases.tidalNativeTravelDamageAndStaff(h,player(h));}
 @GameTest(template="empty",batch="area_3",timeoutTicks=100) public static void rejectedCastsAndCreative(GameTestHelper h){MovingAreaCases.rejectedCastsAndCreative(h,player(h));}
}
