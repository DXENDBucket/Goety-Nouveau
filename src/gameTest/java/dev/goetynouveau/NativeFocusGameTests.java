package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_native_self") @PrefixGameTestTemplate(false)
public final class NativeFocusGameTests {
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        return player;
 }
 @GameTest(template="empty",timeoutTicks=80) public static void catalogue(GameTestHelper h){NativeFocusChecks.catalogue(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void warmupStaffAndScope(GameTestHelper h){NativeFocusChecks.warmupStaffAndScope(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void chargingBilling(GameTestHelper h){NativeFocusChecks.chargingBilling(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void emptyAccountAndInvalidTarget(GameTestHelper h){NativeFocusChecks.emptyAccountAndInvalidTarget(h,player(h));}
 @GameTest(template="empty",batch="native_ars",timeoutTicks=80) public static void arsAlternativeAndCreative(GameTestHelper h){NativeFocusChecks.arsAlternativeAndCreative(h,player(h));}
}
