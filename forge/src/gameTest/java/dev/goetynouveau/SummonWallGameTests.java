package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_summon_wall") @PrefixGameTestTemplate(false)
public final class SummonWallGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        return net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"SummonStudent"));
    }
    @GameTest(template="empty",timeoutTicks=40) public static void existingServantRegression(GameTestHelper h) { NatureServantCases.nativeServantsSharedLimitsVariantsAndRecall(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeBeastVariantsAndRecall(GameTestHelper h) { SummonWallCases.nativeBeastVariantsAndRecall(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeUndeadQuotaAndTarget(GameTestHelper h) { SummonWallCases.nativeUndeadQuotaAndTarget(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativePhantomFlightAndLifetime(GameTestHelper h) { SummonWallCases.nativePhantomFlightAndLifetime(h,player(h)); }
    @GameTest(template="empty",timeoutTicks=40) public static void nativeWallsBombsAndCooldown(GameTestHelper h) {
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        SummonWallCases.nativeWallsBombsAndCooldown(h,player);
    }
}
