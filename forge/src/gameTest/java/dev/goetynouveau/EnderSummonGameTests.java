package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_ender") @PrefixGameTestTemplate(false)
public final class EnderSummonGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CreatureStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeBatchesQuotaRecallAndResources(GameTestHelper h) { EnderSummonCases.nativeBatchesQuotaRecallAndResources(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeAttacksHidingAndExpiry(GameTestHelper h) { EnderSummonCases.nativeAttacksHidingAndExpiry(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingBlazeSummon(GameTestHelper h) { ElementalSummonCases.blazeEquipmentQuotaAndExpiry(h,player(h)); }
}
