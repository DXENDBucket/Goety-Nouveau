package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_casting") @PrefixGameTestTemplate(false)
public final class ActiveCastingGameTests {
    @GameTest(template="empty",timeoutTicks=30)
    public static void mixedPreparationAndNestedEntry(GameTestHelper h) { ActiveCastingCases.mixedPreparationAndNestedEntry(h,net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"SharedCastStudent"))); }
    @GameTest(template="empty",timeoutTicks=30)
    public static void cancellationDeniedReleaseAndPureArs(GameTestHelper h) { ActiveCastingCases.cancellationDeniedReleaseAndPureArs(h); }
    @GameTest(template="empty",timeoutTicks=30)
    public static void nativeGoetyCooldownAndChargeBoundary(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        ActiveCastingCases.nativeGoetyCooldownAndChargeBoundary(h,player);
    }
}
