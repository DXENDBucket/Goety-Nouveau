package dev.goetynouveau.tests;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_mobs") @PrefixGameTestTemplate(false)
public final class MobCastingGameTests {
 @GameTest(template="empty",timeoutTicks=80) public static void nativeAccountsBuffAndCooldown(GameTestHelper h){MobCastingCases.run(h);}
}
