package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_utility") @PrefixGameTestTemplate(false)
public final class UtilitySpellGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"UtilityStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40) public static void sensingRotationAndTrembling(GameTestHelper h) { UtilitySpellCases.sensingRotationAndTrembling(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeChargeStackAndChain(GameTestHelper h) { UtilitySpellCases.nativeChargeStackAndChain(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void royalSpikesAndLeapleaf(GameTestHelper h) { UtilitySpellCases.royalSpikesAndLeapleaf(h,player(h)); }
}
