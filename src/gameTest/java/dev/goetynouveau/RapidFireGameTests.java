package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_rapid_fire") @PrefixGameTestTemplate(false)
public final class RapidFireGameTests {
    static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        return player;
    }
    @GameTest(template="empty",batch="rapid_0",timeoutTicks=60) public static void immediateRepeatedRealShotsAndPayment(GameTestHelper h) {RapidFireCases.immediateRepeatedRealShotsAndPayment(h,player(h));}
    @GameTest(template="empty",batch="rapid_1",timeoutTicks=200) public static void mixedRecipeAndOtherChargingSpells(GameTestHelper h) {RapidFireCases.mixedRecipeAndOtherChargingSpells(h,player(h));}
}
