package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_remaining_summons") @PrefixGameTestTemplate(false)
public final class RemainingSummonGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CreatureStudent"));
    }
    @GameTest(template="empty",batch="remaining_ministrosity",timeoutTicks=40) public static void ministrosityVariantsQuotaAndRecall(GameTestHelper h) { RemainingSummonCases.ministrosityVariantsQuotaAndRecall(h,player(h)); }
    @GameTest(template="empty",batch="remaining_hogging",timeoutTicks=40) public static void burningHoglinFlanksExplosionAndQuota(GameTestHelper h) { RemainingSummonCases.burningHoglinFlanksExplosionAndQuota(h,player(h)); }
    @GameTest(template="empty",batch="remaining_breeze",timeoutTicks=40) public static void breezeBatchQuotaAndRecall(GameTestHelper h) { RemainingSummonCases.breezeBatchQuotaAndRecall(h,player(h)); }
    @GameTest(template="empty",batch="remaining_breath_regression",timeoutTicks=40) public static void existingBreath(GameTestHelper h) { BreathingCases.nativeDamageHostAndOwnership(h,player(h)); }
}
