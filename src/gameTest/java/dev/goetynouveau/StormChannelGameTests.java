package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_storm_channels") @PrefixGameTestTemplate(false)
public final class StormChannelGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"StormStudent"));
    }
    @GameTest(template="empty",batch="bombardment",timeoutTicks=180) public static void bombardmentHostMotionAndCadence(GameTestHelper h) {StormChannelCases.bombardmentHostMotionAndCadence(h,player(h));}
    @GameTest(template="empty",batch="thunderstorm",timeoutTicks=160) public static void nativeThunderstormVariantsAndOwner(GameTestHelper h) {StormChannelCases.nativeThunderstormVariantsAndOwner(h,player(h));}
    @GameTest(template="empty",batch="trident",timeoutTicks=200) public static void tridentLocationWarmupAndNativeStrike(GameTestHelper h) {StormChannelCases.tridentLocationWarmupAndNativeStrike(h,player(h));}
    @GameTest(template="empty",batch="rain_regression",timeoutTicks=180) public static void rainRegression(GameTestHelper h) {RainStormCases.meteorCadenceAndEmptyAccount(h,player(h));}
}
