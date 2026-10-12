package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_casting") @PrefixGameTestTemplate(false)
public final class ActiveCastingGameTests {
    @GameTest(template="empty",timeoutTicks=30)
    public static void externalCoreAllianceAuthorizesChannels(GameTestHelper h) throws Exception {
        var caster=h.spawn(net.minecraft.world.entity.EntityType.COW,2,2,2);var host=h.spawn(net.minecraft.world.entity.EntityType.COW,4,2,2);
        var relation=dev.arsconflux.api.targeting.Allies.register(dev.arsconflux.api.projectile.CarrierRegistry.id("audit_fixture:"+caster.getUUID()),(a,b)->a==caster&&b==host);
        try {h.assertTrue(dev.goetynouveau.casting.GoetyChannelCasting.eligible(h.getLevel(),caster,host),"Goety accepts the same external alliance as other providers");}
        finally {relation.close();}
        h.assertTrue(!dev.goetynouveau.casting.GoetyChannelCasting.eligible(h.getLevel(),caster,host),"Removing that relation revokes execution authorization");h.succeed();
    }
    @GameTest(template="empty",timeoutTicks=30)
    public static void mixedPreparationAndNestedEntry(GameTestHelper h) { ActiveCastingCases.mixedPreparationAndNestedEntry(h,net.minecraftforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"SharedCastStudent"))); }
    @GameTest(template="empty",timeoutTicks=30)
    public static void cancellationDeniedReleaseAndPureArs(GameTestHelper h) { ActiveCastingCases.cancellationDeniedReleaseAndPureArs(h); }
    @GameTest(template="empty",timeoutTicks=30)
    public static void nativeGoetyCooldownAndChargeBoundary(GameTestHelper h) {
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        ActiveCastingCases.nativeGoetyCooldownAndChargeBoundary(h,player);
    }
}
