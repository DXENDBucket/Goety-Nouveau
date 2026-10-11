package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_elemental") @PrefixGameTestTemplate(false)
public final class ElementalSummonGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CreatureStudent"));
    }
    private static net.minecraft.server.level.ServerPlayer waterPlayer(GameTestHelper h) {
        var profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"WaterStudent");
        var dummy=player(h);
        var result=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile);
        result.connection=dummy.connection;
        // Fake-player tick does not refresh immersion. Use an actual ServerPlayer;
        // the shared water fixture runs its doTick to sample eye/body fluid state.
        for(int i=0;i<61;i++) result.tick();
        return result;
    }
    @GameTest(template="empty",timeoutTicks=40) public static void blazeEquipmentQuotaAndExpiry(GameTestHelper h) { ElementalSummonCases.blazeEquipmentQuotaAndExpiry(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void ghastEquipmentPowerAndRecall(GameTestHelper h) { ElementalSummonCases.ghastEquipmentPowerAndRecall(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void guardianWaterQuotaAndNativeDown(GameTestHelper h) { ElementalSummonCases.guardianWaterQuotaAndNativeDown(h,waterPlayer(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingWaterVariant(GameTestHelper h) { CreatureSummonCases.whisperAndFrostbornVariants(h,player(h)); }
}
