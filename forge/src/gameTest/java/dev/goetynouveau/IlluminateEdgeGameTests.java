package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_illuminate_edge") @PrefixGameTestTemplate(false)
public final class IlluminateEdgeGameTests {
 @GameTest(template="empty",timeoutTicks=100) public static void zeroNativePeriodIsRejected(GameTestHelper h){IlluminateCases.zeroNativePeriodIsRejected(h,net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"LightEdge")));}
}
