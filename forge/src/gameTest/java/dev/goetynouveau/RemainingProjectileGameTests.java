package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_projectiles") @PrefixGameTestTemplate(false)
public final class RemainingProjectileGameTests {
    @GameTest(template="empty",timeoutTicks=40) public static void repeatedPiercingArrows(GameTestHelper h) { RemainingProjectileCases.repeatedPiercingArrows(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void poisonDartNativeFlightAndPiercing(GameTestHelper h) { ProjectileBatchCases.quillNativePhysicsAndPiercing(h,player(h)); }
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        var player = net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ProjectileStudent"));
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player).orElseThrow(IllegalStateException::new);
        mana.setMaxMana(10000); mana.setMana(10000);
        return player;
    }
    @GameTest(template="empty",timeoutTicks=80) public static void invulnerableContactsDoNotContinueButBlocksDo(GameTestHelper h) { RemainingProjectileCases.invulnerableContactsDoNotContinueButBlocksDo(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=80) public static void nativeLaunchesAndFullHits(GameTestHelper h) { RemainingProjectileCases.nativeLaunchesAndFullHits(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=80) public static void repeatingCarriersAndDeniedBilling(GameTestHelper h) { RemainingProjectileCases.repeatingCarriersAndDeniedBilling(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=80) public static void salvosPiercingSwordAndCreative(GameTestHelper h) { RemainingProjectileCases.salvosPiercingSwordAndCreative(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=80) public static void nativeCollisionHooks(GameTestHelper h) { RemainingProjectileCases.nativeCollisionHooks(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void unaugmentedCushionWorks(GameTestHelper h) { RemainingProjectileCases.unaugmentedCushionWorks(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingBoltFullHits(GameTestHelper h) { SoulBoltCases.nativeImpactAndContinuation(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingCarrierVisualTicks(GameTestHelper h) { ProjectileBatchCases.specialCarriersKeepNativeVisualTicks(h,player(h)); }
}
