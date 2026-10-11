package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_rain_storm") @PrefixGameTestTemplate(false)
public final class RainStormGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"RainStudent"));
    }
    @GameTest(template="empty",batch="rain_geometry",timeoutTicks=200) public static void nativeRainGeometryAndVariants(GameTestHelper h) {RainStormCases.nativeRainGeometryAndVariants(h,player(h));}
    @GameTest(template="empty",batch="rain_upkeep",timeoutTicks=150) public static void upkeepAndAllegiance(GameTestHelper h) {RainStormCases.upkeepAndAllegiance(h,player(h));}
    @GameTest(template="empty",batch="meteor_cadence",timeoutTicks=150) public static void meteorCadenceAndEmptyAccount(GameTestHelper h) {RainStormCases.meteorCadenceAndEmptyAccount(h,player(h));}
    @GameTest(template="empty",batch="moving_cloud",timeoutTicks=200) public static void nativeCloudFlightAndFreeze(GameTestHelper h) {RainStormCases.nativeCloudFlightAndFreeze(h,player(h));}
    @GameTest(template="empty",batch="breath_regression",timeoutTicks=250) public static void breathRegression(GameTestHelper h) {BreathingCases.upkeepExpiryAndAllegiance(h,player(h));}
}
