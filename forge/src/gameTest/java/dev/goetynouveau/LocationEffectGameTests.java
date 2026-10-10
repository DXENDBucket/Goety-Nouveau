package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_locations") @PrefixGameTestTemplate(false)
public final class LocationEffectGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "LocationStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void nativePlacementDamageExpiryAndStaff(GameTestHelper h) { LocationEffectCases.nativePlacementDamageExpiryAndStaff(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void leechHealingAndNativeLaunch(GameTestHelper h) { LocationEffectCases.leechHealingAndNativeLaunch(h,player(h)); }
}
