package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_tunnel") @PrefixGameTestTemplate(false)
public final class TunnelGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"TunnelStudent"));}
 @GameTest(template="empty",batch="tunnel_0",timeoutTicks=250) public static void realRemoteExpansionAndRestoration(GameTestHelper h){TunnelCases.realRemoteExpansionAndRestoration(h,player(h));}
 @GameTest(template="empty",batch="tunnel_1",timeoutTicks=250) public static void staffWidthDurationAndProtectedBlocks(GameTestHelper h){TunnelCases.staffWidthDurationAndProtectedBlocks(h,player(h));}
 @GameTest(template="empty",batch="tunnel_2",timeoutTicks=250) public static void persistentDownwardFaceAndBoundary(GameTestHelper h){TunnelCases.persistentDownwardFaceAndBoundary(h,player(h),hole->hole.saveWithFullMetadata(),(hole,tag)->hole.load(tag),hole->hole.getUpdateTag());}
 @GameTest(template="empty",batch="tunnel_3",timeoutTicks=250) public static void invalidRecipientsResourcesAndLifetime(GameTestHelper h){TunnelCases.invalidRecipientsResourcesAndLifetime(h,player(h));}
}
