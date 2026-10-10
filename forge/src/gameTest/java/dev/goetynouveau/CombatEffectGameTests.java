package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_combat") @PrefixGameTestTemplate(false)
public final class CombatEffectGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CombatStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void bulwarkNativeShieldsAndBilling(GameTestHelper h) { CombatEffectCases.bulwarkNativeShieldsAndBilling(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void dischargeNativeAreaChainAndBilling(GameTestHelper h) { CombatEffectCases.dischargeNativeAreaChainAndBilling(h,player(h)); }
}
