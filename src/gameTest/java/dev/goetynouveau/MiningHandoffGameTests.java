package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_mining_handoff") @PrefixGameTestTemplate(false)
public final class MiningHandoffGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MiningStudent"));}
 @GameTest(template="empty",timeoutTicks=100) public static void burrowingNativeWandHandoff(GameTestHelper h){UtilityChannelCases.burrowingNativeWandHandoff(h,player(h),player(h));}
}
