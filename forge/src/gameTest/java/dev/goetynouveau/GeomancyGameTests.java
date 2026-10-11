package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_geomancy") @PrefixGameTestTemplate(false)
public final class GeomancyGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"EarthStudent"));}
 @GameTest(template="empty",batch="earth_0",timeoutTicks=250) public static void nativeRecipesItemsAndFace(GameTestHelper h){GeomancyCases.nativeRecipesItemsAndFace(h,player(h));}
 @GameTest(template="empty",batch="earth_1",timeoutTicks=250) public static void ordinaryMineGeometryRolesAndLifecycle(GameTestHelper h){GeomancyCases.ordinaryMineGeometryRolesAndLifecycle(h,player(h));}
 @GameTest(template="empty",batch="earth_2",timeoutTicks=250) public static void staffCadenceGroupedPaymentAndExpiry(GameTestHelper h){GeomancyCases.staffCadenceGroupedPaymentAndExpiry(h,player(h));}
 @GameTest(template="empty",batch="earth_3",timeoutTicks=250) public static void rejectionCreativeAndAllianceLoss(GameTestHelper h){GeomancyCases.rejectionCreativeAndAllianceLoss(h,player(h));}
}
