package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_illuminate") @PrefixGameTestTemplate(false)
public final class IlluminateGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"LightStudent"));}
 @GameTest(template="empty",batch="light_0",timeoutTicks=300) public static void actualNativePlacementAndSourceGeometry(GameTestHelper h){IlluminateCases.actualNativePlacementAndSourceGeometry(h,player(h));}
 @GameTest(template="empty",batch="light_1",timeoutTicks=300) public static void emptySearchLiveMovementCadenceAndFunds(GameTestHelper h){IlluminateCases.emptySearchLiveMovementCadenceAndFunds(h,player(h));}
 @GameTest(template="empty",batch="light_2",timeoutTicks=300) public static void nativeReplacementBoundsAndPreparation(GameTestHelper h){IlluminateCases.nativeReplacementBoundsAndPreparation(h,player(h));}
 @GameTest(template="empty",batch="light_3",timeoutTicks=300) public static void rejectedExecutorsCreativeAndBoundedExpiry(GameTestHelper h){IlluminateCases.rejectedExecutorsCreativeAndBoundedExpiry(h,player(h));}
}
