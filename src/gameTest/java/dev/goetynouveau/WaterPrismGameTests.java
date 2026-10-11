package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_water_prism") @PrefixGameTestTemplate(false)
public final class WaterPrismGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"PrismStudent"));}
 @GameTest(template="empty",batch="water",timeoutTicks=100) public static void waterNativeDamageVisualAndVariants(GameTestHelper h){WaterPrismCases.waterNativeDamageVisualAndVariants(h,player(h));}
 @GameTest(template="empty",batch="water_environment",timeoutTicks=100) public static void waterEnvironmentalAndEmptyAccount(GameTestHelper h){WaterPrismCases.waterEnvironmentalAndEmptyAccount(h,player(h));}
 @GameTest(template="empty",batch="prism",timeoutTicks=400) public static void prismNativeChargeAndFinalPayment(GameTestHelper h){WaterPrismCases.prismNativeChargeAndFinalPayment(h,player(h));}
 @GameTest(template="empty",batch="prism_stop",timeoutTicks=100) public static void prismInvalidAndInterruptedTargets(GameTestHelper h){WaterPrismCases.prismInvalidAndInterruptedTargets(h,player(h));}
 @GameTest(template="empty",batch="corruption_regression",timeoutTicks=100) public static void corruptionRegression(GameTestHelper h){BeamChannelCases.paymentAllegianceAndCleanup(h,player(h));}
}
