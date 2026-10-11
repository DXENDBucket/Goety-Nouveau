package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import dev.goetynouveau.mixin.FistVisualAccess;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.List;

/** Restore transient native EarthFist animations for newly tracking observers only. */
public final class NativeVisualTracking {
    private NativeVisualTracking() {}
    public static List<Byte> events(Entity entity) {
        if (!(entity instanceof EarthFist fist) || fist.isRemoved() || !fist.isSentSpikeEvent()
                || !fist.getPersistentData().getBoolean("goety_nouveau:ars_fist")) return List.of();
        return ((FistVisualAccess)fist).goetyNouveau$remainingLife()<=0
                ? List.of((byte)4,(byte)5) : List.of((byte)4);
    }
    public static void startTracking(ServerPlayer observer,Entity entity) {
        for (byte event:events(entity)) observer.connection.send(new ClientboundEntityEventPacket(entity,event));
    }
}
