package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_breathing") @PrefixGameTestTemplate(false)
public final class BreathingGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CreatureStudent"));
    }
    @GameTest(template="empty",batch="native_breath_host",timeoutTicks=250) public static void nativeDamageHostAndOwnership(GameTestHelper h) { BreathingCases.nativeDamageHostAndOwnership(h,player(h)); }
    @GameTest(template="empty",batch="native_breath_upkeep",timeoutTicks=250) public static void upkeepExpiryAndAllegiance(GameTestHelper h) { BreathingCases.upkeepExpiryAndAllegiance(h,player(h)); }
}
