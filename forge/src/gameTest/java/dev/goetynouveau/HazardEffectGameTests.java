package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_hazards") @PrefixGameTestTemplate(false)
public final class HazardEffectGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "HazardStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40)
    public static void volcanoEruptionAndExpiry(GameTestHelper h) { HazardEffectCases.volcano(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void minesArmExplodeAndLeaveAcid(GameTestHelper h) { HazardEffectCases.mines(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40)
    public static void teethHoverFallAndLeavePikes(GameTestHelper h) { HazardEffectCases.teeth(h,player(h)); }
}
