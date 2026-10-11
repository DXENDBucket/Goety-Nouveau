package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_personal") @PrefixGameTestTemplate(false)
public final class PersonalSpellGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"PersonalStudent"));
    }
    private static net.minecraft.server.level.ServerPlayer vulnerablePlayer(GameTestHelper h) {
        var profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CurseStudent");
        var dummy=player(h);
        var result=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,net.minecraft.server.level.ClientInformation.createDefault());
        result.connection=dummy.connection;
        // Native fake players are permanently immune. Use a real ServerPlayer and let
        // its initial 60-tick spawn protection elapse through actual server player ticks.
        for(int i=0;i<61;i++) result.tick();
        return result;
    }
    @GameTest(template="empty",timeoutTicks=40) public static void killingFeedbackTargetAndResources(GameTestHelper h) { PersonalSpellCases.killingFeedbackTargetAndResources(h,vulnerablePlayer(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void illusionRecastMetadataAndLifecycle(GameTestHelper h) { PersonalSpellCases.illusionRecastMetadataAndLifecycle(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void illusionNativeEquipmentBranches(GameTestHelper h) { PersonalSpellCases.illusionNativeEquipmentBranches(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void boltingAimDamageAndCleanup(GameTestHelper h) { PersonalSpellCases.boltingAimDamageAndCleanup(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void existingFistTracking(GameTestHelper h) { AreaEffectCases.fistTracking(h,player(h)); }
}
