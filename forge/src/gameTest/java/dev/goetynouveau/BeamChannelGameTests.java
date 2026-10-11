package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_beams") @PrefixGameTestTemplate(false)
public final class BeamChannelGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(), new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "BeamStudent"));
    }
    @GameTest(template="empty",batch="native_beam",timeoutTicks=100) public static void nativeGeometryDamageAndAlliance(GameTestHelper h) {BeamChannelCases.nativeGeometryDamageAndAlliance(h,player(h));}
    @GameTest(template="empty",batch="beam_upkeep",timeoutTicks=100) public static void paymentAllegianceAndCleanup(GameTestHelper h) {BeamChannelCases.paymentAllegianceAndCleanup(h,player(h));}
    @GameTest(template="empty",batch="beam_lifetime",timeoutTicks=600) public static void finiteCreativeAndOrphanCleanup(GameTestHelper h) {BeamChannelCases.finiteCreativeAndOrphanCleanup(h,player(h));}
    @GameTest(template="empty",batch="rain_regression",timeoutTicks=180) public static void rainRegression(GameTestHelper h) {RainStormCases.meteorCadenceAndEmptyAccount(h,player(h));}
}
