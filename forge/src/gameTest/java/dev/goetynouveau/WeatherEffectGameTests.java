package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_weather") @PrefixGameTestTemplate(false)
public final class WeatherEffectGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "WeatherStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void thunderboltDamageRodAndChain(GameTestHelper h) { WeatherEffectCases.thunderbolt(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void cloudWarmupDamageTrackingAndExpiry(GameTestHelper h) { WeatherEffectCases.clouds(h,player(h)); }
}
