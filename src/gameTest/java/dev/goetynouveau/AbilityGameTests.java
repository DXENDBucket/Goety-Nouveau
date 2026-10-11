package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_abilities") @PrefixGameTestTemplate(false)
public final class AbilityGameTests {
    @GameTest(template="empty",timeoutTicks=60) public static void nativeBrewsAndEffects(GameTestHelper h) {AbilityCases.nativeBrewsAndEffects(h,RapidFireGameTests.player(h));}
    @GameTest(template="empty",timeoutTicks=60,batch="ability_projectiles") public static void nativeProjectilesAndRepeatedPayment(GameTestHelper h) {AbilityCases.nativeProjectilesAndRepeatedPayment(h,RapidFireGameTests.player(h));}
}
