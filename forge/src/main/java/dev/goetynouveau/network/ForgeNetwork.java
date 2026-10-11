package dev.goetynouveau.network;
import dev.arsconflux.api.projectile.CarrierRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
public final class ForgeNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CarrierRegistry.id("goety_nouveau:casting"), () -> "1", "1"::equals, "1"::equals);
    private ForgeNetwork() {}
    public static void register() {
        CHANNEL.messageBuilder(ChantStatePayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder((data, buf) -> { buf.writeUUID(data.token()); buf.writeVarInt(data.duration()); buf.writeVarInt(data.remaining()); })
                .decoder(buf -> new ChantStatePayload(buf.readUUID(), buf.readVarInt(), buf.readVarInt()))
                .consumerMainThread((data, context) -> {
                    if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT)
                        dev.goetynouveau.client.ChantHudState.accept(data);
                    context.get().setPacketHandled(true);
                }).add();
    }
    public static void sendToPlayer(ServerPlayer player, ChantStatePayload data) { CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), data); }
}
