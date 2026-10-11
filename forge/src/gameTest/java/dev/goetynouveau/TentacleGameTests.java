package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_tentacles") @PrefixGameTestTemplate(false)
public final class TentacleGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"WhipStudent"));}
 @GameTest(template="empty",batch="whip_0",timeoutTicks=100) public static void nativeAttachmentWarmupDamageAndExpiry(GameTestHelper h){TentacleCases.nativeAttachmentWarmupDamageAndExpiry(h,player(h));}
 @GameTest(template="empty",batch="whip_1",timeoutTicks=100) public static void nativeStaffOffspringAndPersistence(GameTestHelper h){TentacleCases.nativeStaffOffspringAndPersistence(h,player(h));}
 @GameTest(template="empty",batch="whip_2",timeoutTicks=100) public static void alliesInterruptionAndNativeIsolation(GameTestHelper h){TentacleCases.alliesInterruptionAndNativeIsolation(h,player(h));}
 @GameTest(template="empty",batch="whip_3",timeoutTicks=100) public static void rejectedRecipientsPaymentAndCreative(GameTestHelper h){TentacleCases.rejectedRecipientsPaymentAndCreative(h,player(h));}
}
