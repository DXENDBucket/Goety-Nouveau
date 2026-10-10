package dev.goetynouveau;

import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.fml.ModList;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

@GameTestHolder("goety_nouveau") @PrefixGameTestTemplate(false)
public final class SoulBoltGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        h.assertTrue(!ModList.get().isLoaded("irons_nouveau") && !ModList.get().isLoaded("irons_spellbooks"), "Goety integration runs independently of Iron");
        var player = FakePlayerFactory.get(h.getLevel(), new GameProfile(UUID.randomUUID(), "GoetyStudent"));
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
        mana.setMaxMana(10000); mana.setMana(10000);
        return player;
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void specialCarriersKeepNativeVisualTicks(GameTestHelper h) { ProjectileBatchCases.specialCarriersKeepNativeVisualTicks(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void fireballNativeMotionAndSalvo(GameTestHelper h) { ProjectileBatchCases.fireballNativeMotionAndSalvo(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void quillNativePhysicsAndPiercing(GameTestHelper h) { ProjectileBatchCases.quillNativePhysicsAndPiercing(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void areaEffectsAndBurningSummons(GameTestHelper h) { ProjectileBatchCases.areaEffectsAndBurningSummons(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void repeatedNewPayloadBilling(GameTestHelper h) { ProjectileBatchCases.repeatedNewPayloadBilling(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeBuffsAndWeakening(GameTestHelper h) { SimpleSpellCases.nativeBuffsAndWeakening(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeLightningAndUpdraft(GameTestHelper h) { SimpleSpellCases.nativeLightningAndUpdraft(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeMagicBoltFlightAndFullImpacts(GameTestHelper h) { SimpleSpellCases.nativeMagicBoltFlightAndFullImpacts(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void providerGlyphGroupsKeepCategories(GameTestHelper h) {
        PresentationCases.check(h, com.hollingsworth.arsnouveau.setup.registry.CreativeTabRegistry.COMPARE_SPELL_TYPE_NAME);
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeSkullSummoning(GameTestHelper h) { SummonCases.nativeSkullsKeepTargetOwnerExpiryAndOneBatchCharge(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeContinuationKeepsSourceAccount(GameTestHelper h) { EffectCases.nativeContinuationKeepsSourceAccount(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeIceTracksSelectedTarget(GameTestHelper h) { EffectCases.nativeIceTracksSelectedTarget(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeVinesAndBlockTarget(GameTestHelper h) { EffectCases.nativeVinesAndBlockTarget(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void novaAndHealingRespectAllies(GameTestHelper h) { EffectCases.novaAndHealingRespectAllies(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeImpactAndContinuation(GameTestHelper h) { SoulBoltCases.nativeImpactAndContinuation(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeLaunchMotionAndRotation(GameTestHelper h) { SoulBoltCases.nativeLaunchMotionAndRotation(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void storageBudgetAndCreative(GameTestHelper h) { SoulBoltCases.storageBudgetAndCreative(h, player(h)); }
    @GameTest(template="empty", timeoutTicks=40)
    public static void arsManaAlternativeAndFailedSpawn(GameTestHelper h) {
        var player = player(h);
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
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
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(listener);
        try { SoulBoltCases.nativeLossEvent(h, player, calls::get); }
        finally { net.neoforged.neoforge.common.NeoForge.EVENT_BUS.unregister(listener); }
    }
}
