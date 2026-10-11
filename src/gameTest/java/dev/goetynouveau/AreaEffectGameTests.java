package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_areas") @PrefixGameTestTemplate(false)
public final class AreaEffectGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "AreaStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void instantDamageArmorOwnerAndBilling(GameTestHelper h) { AreaEffectCases.instant(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void pillarFormationDamageStaffAndExpiry(GameTestHelper h) { AreaEffectCases.pillars(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void riftPullDamageClosingStaffAndExpiry(GameTestHelper h) { AreaEffectCases.rift(h,player(h)); }
}
