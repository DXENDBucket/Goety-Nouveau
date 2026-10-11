package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_soul_light") @PrefixGameTestTemplate(false)
public final class SoulLightGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"SoulLightTester"));}
 @GameTest(template="empty",batch="soul_light_0",timeoutTicks=100) public static void actualNativeFlightAndParticles(GameTestHelper h){SoulLightCases.actualNativeFlightAndParticles(h,player(h));}
 @GameTest(template="empty",batch="soul_light_1",timeoutTicks=100) public static void fullNativeEntityAndBlockImpacts(GameTestHelper h){SoulLightCases.fullNativeEntityAndBlockImpacts(h,player(h));}
 @GameTest(template="empty",batch="soul_light_2",timeoutTicks=100) public static void repeatingCarrierPaymentAndParticleContract(GameTestHelper h){SoulLightCases.repeatingCarrierPaymentAndParticleContract(h,player(h));}
 @GameTest(template="empty",batch="soul_light_3",timeoutTicks=100) public static void creativeResourceValidationAndAcceleration(GameTestHelper h){SoulLightCases.creativeResourceValidationAndAcceleration(h,player(h));}
}
