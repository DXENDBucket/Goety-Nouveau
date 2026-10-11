package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_tunnel_edge") @PrefixGameTestTemplate(false)
public final class TunnelEdgeGameTests {
 @GameTest(template="empty",timeoutTicks=120) public static void largeRangeAndNativeIsolation(GameTestHelper h){TunnelCases.largeRangeAndNativeIsolation(h,net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"TunnelEdge")));}
}
