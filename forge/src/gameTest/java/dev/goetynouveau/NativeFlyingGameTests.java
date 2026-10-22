package dev.goetynouveau;

import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;

/** Separate namespace permits rerunning only the version-sensitive native callback check. */
@GameTestHolder("goety_nouveau_native_flying") @PrefixGameTestTemplate(false)
public final class NativeFlyingGameTests {
    @GameTest(template="empty",timeoutTicks=80)
    public static void flyingMatchesLoadedNativeRule(GameTestHelper h) {
        NativeGrowthCases.flyingMatchesLoadedNativeRule(h,NativeGrowthGameTests.player(h),NativeGrowthGameTests::attributes,
                (p,e)->NativeGrowthGameTests.focus(h,p,e));
    }
}
