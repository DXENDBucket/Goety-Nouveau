package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_native_casting") @PrefixGameTestTemplate(false)
public final class NativePreparationGameTests {
    @GameTest(template="empty",timeoutTicks=220)
    public static void nativeGoetyPreparationRelease(GameTestHelper h) {
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        ActiveCastingCases.nativeGoetyPreparationRelease(h,player,recipe -> new com.hollingsworth.arsnouveau.api.spell.SpellCaster(player.getMainHandItem()).castSpell(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,null,recipe));
    }
}
