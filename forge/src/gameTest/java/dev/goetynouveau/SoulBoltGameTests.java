package dev.goetynouveau;

import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.ModList;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

@GameTestHolder("goety_nouveau") @PrefixGameTestTemplate(false)
public final class SoulBoltGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        h.assertTrue(!ModList.get().isLoaded("irons_nouveau") && !ModList.get().isLoaded("irons_spellbooks"), "Goety integration runs independently of Iron");
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "GoetyStudent"));
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player).orElseThrow(IllegalStateException::new);
        mana.setMaxMana(10000); mana.setMana(10000);
        return player;
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeImpactAndContinuation(GameTestHelper h) { SoulBoltCases.nativeImpactAndContinuation(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeLaunchMotionAndRotation(GameTestHelper h) { SoulBoltCases.nativeLaunchMotionAndRotation(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void storageBudgetAndCreative(GameTestHelper h) { SoulBoltCases.storageBudgetAndCreative(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void arsManaAlternativeAndFailedSpawn(GameTestHelper h) {
        var player = player(h);
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player).orElseThrow(IllegalStateException::new);
        SoulBoltCases.arsManaAlternativeAndFailedSpawn(h, player, mana::setMana, () -> (int)mana.getCurrentMana());
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void repeatedCarrierPaysAtEachImpact(GameTestHelper h) { SoulBoltCases.repeatedCarrierPaysAtEachImpact(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeLossEvent(GameTestHelper h) {
        var player = player(h); var calls = new java.util.concurrent.atomic.AtomicInteger();
        java.util.function.Consumer<com.Polarice3.Goety.common.events.spell.ChangeSoulEnergyEvent.Loss> listener = event -> {
            if (event.getEntity() == player) { calls.incrementAndGet(); event.setSoulChange(15); }
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(listener);
        try { SoulBoltCases.nativeLossEvent(h, player, calls::get); }
        finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(listener); }
    }
}
