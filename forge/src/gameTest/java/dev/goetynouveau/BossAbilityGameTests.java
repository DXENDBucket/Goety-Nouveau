package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_boss_abilities") @PrefixGameTestTemplate(false)
public final class BossAbilityGameTests {
    @GameTest(template="empty",timeoutTicks=60,batch="boss_cloud") public static void hellCloud(GameTestHelper h){BossAbilityCases.cloud(h,RapidFireGameTests.player(h));}
    @GameTest(template="empty",timeoutTicks=60,batch="boss_trap") public static void voidTrap(GameTestHelper h){BossAbilityCases.trap(h,RapidFireGameTests.player(h));}
    @GameTest(template="empty",timeoutTicks=60,batch="boss_hysteria") public static void hysteria(GameTestHelper h){BossAbilityCases.hysteria(h,RapidFireGameTests.player(h));}
    @GameTest(template="empty",timeoutTicks=60,batch="boss_arrow") public static void deathArrow(GameTestHelper h){BossAbilityCases.arrow(h,RapidFireGameTests.player(h));}
    @GameTest(template="empty",timeoutTicks=60,batch="boss_bomb") public static void illBomb(GameTestHelper h){BossAbilityCases.bomb(h,RapidFireGameTests.player(h));}
}
