package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_registration") @PrefixGameTestTemplate(false)
public final class NativeFocusRegistrationTests {
 @GameTest(template="empty",timeoutTicks=80)
 public static void earlyAndLateFocusesRegisterOnceWithRecipesAndModels(GameTestHelper h){NativeFocusGameTests.catalogue(h);}
}
