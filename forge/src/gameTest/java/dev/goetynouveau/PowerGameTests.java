package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_power") @PrefixGameTestTemplate(false)
public final class PowerGameTests {
 @GameTest(template="empty",timeoutTicks=100) public static void realIronAndGoetySharedPower(GameTestHelper h) {
  var player=net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"ConfluencePower"));
  PowerCases.sharedNativeDamage(h,player,value->player.getAttribute(dev.arsconflux.platform.PowerAttributes.CONFLUENCE.get()).setBaseValue(value));
 }
}
