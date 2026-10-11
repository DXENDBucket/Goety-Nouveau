package dev.goetynouveau.network;

import dev.goetynouveau.GoetyNouveau;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.UUID;

/** Visual state only. Never initiates a native Goety cast or spends resources. remaining == 0 ends this token. */
public record ChantStatePayload(UUID token, int duration, int remaining, boolean channeling) implements CustomPacketPayload {
    public ChantStatePayload(UUID token, int duration, int remaining) { this(token, duration, remaining, false); }
    public static final Type<ChantStatePayload> TYPE = new Type<>(dev.arsconflux.api.projectile.CarrierRegistry.id(GoetyNouveau.MOD_ID + ":chant_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChantStatePayload> CODEC = StreamCodec.of(
            (buf, data) -> { buf.writeUUID(data.token); buf.writeVarInt(data.duration); buf.writeVarInt(data.remaining); buf.writeBoolean(data.channeling); },
            buf -> new ChantStatePayload(buf.readUUID(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean()));
    @Override public Type<ChantStatePayload> type() { return TYPE; }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(TYPE, CODEC, (payload, context) -> {
            if (FMLEnvironment.dist == Dist.CLIENT)
                context.enqueueWork(() -> dev.goetynouveau.client.ChantHudState.accept(payload));
        });
    }
}
