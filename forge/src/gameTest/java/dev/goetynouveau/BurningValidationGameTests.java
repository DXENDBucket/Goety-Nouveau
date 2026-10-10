package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_augment_ui")
@PrefixGameTestTemplate(false)
public final class BurningValidationGameTests {
    @GameTest(template="empty") public static void burningEligibility(GameTestHelper h) {
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "BurningStudent"));
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(1, 3, 1)));
        BurningValidationCases.check(h, player);
    }
}
