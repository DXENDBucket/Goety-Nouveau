package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_variants") @PrefixGameTestTemplate(false)
public final class VariantBehaviorGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        var player=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"BookCaster"));
        var mana=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player).orElseThrow(IllegalStateException::new);
        mana.setMaxMana(10000); mana.setMana(10000); return player;
    }
    @GameTest(template="empty",timeoutTicks=40) public static void bookOnlySneaking(GameTestHelper h) { VariantBehaviorCases.bookOnlySneaking(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void offhandFrostStaffPierces(GameTestHelper h) { VariantBehaviorCases.offhandFrostStaffPierces(h,player(h)); }
}
