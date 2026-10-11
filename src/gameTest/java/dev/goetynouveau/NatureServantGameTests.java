package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_nature") @PrefixGameTestTemplate(false)
public final class NatureServantGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "NatureStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void windSelectedPointStaffAndAllies(GameTestHelper h) { NatureServantCases.windSelectedPointStaffAndAllies(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void nativeVinesAndTurrets(GameTestHelper h) { NatureServantCases.nativeVinesAndTurrets(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void nativeServantsSharedLimitsVariantsAndRecall(GameTestHelper h) { NatureServantCases.nativeServantsSharedLimitsVariantsAndRecall(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void lightNativeFlightHitsAndRepeating(GameTestHelper h) { NatureServantCases.lightNativeFlightHitsAndRepeating(h,player(h)); }
}
