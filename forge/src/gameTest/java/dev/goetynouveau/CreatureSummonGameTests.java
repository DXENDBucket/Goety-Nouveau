package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_creatures") @PrefixGameTestTemplate(false)
public final class CreatureSummonGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CreatureStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40) public static void slimeVariantsSplittingAndQuota(GameTestHelper h) { CreatureSummonCases.slimeVariantsSplittingAndQuota(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void carrionAndSpriteNativeChildren(GameTestHelper h) { CreatureSummonCases.carrionAndSpriteNativeChildren(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void whisperAndFrostbornVariants(GameTestHelper h) { CreatureSummonCases.whisperAndFrostbornVariants(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void guardNativeFormationAndEquipment(GameTestHelper h) { CreatureSummonCases.guardNativeFormationAndEquipment(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingBeastRegression(GameTestHelper h) { SummonWallCases.nativeBeastVariantsAndRecall(h,player(h)); }
}
