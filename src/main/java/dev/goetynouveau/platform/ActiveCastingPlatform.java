package dev.goetynouveau.platform;
import dev.goetynouveau.network.ChantStatePayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
public final class ActiveCastingPlatform {
    private ActiveCastingPlatform() {}
    public static boolean realPlayer(LivingEntity caster) { return caster instanceof ServerPlayer && !(caster instanceof net.neoforged.neoforge.common.util.FakePlayer); }
    public static void chant(ServerPlayer player, ChantStatePayload packet) { net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, packet); }
}
