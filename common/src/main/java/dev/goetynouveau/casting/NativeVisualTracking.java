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
        // This native packet binds the clone to the receiving player. Never broadcast it
        // to other observers; native owner UUID/client ID metadata serves those clients.
        if (entity instanceof com.Polarice3.Goety.common.entities.ally.Doppelganger clone
                && clone.getPersistentData().getBoolean(GoetyPersonalEffects.CLONE_MARKER)
                && observer.getUUID().equals(clone.getOwnerId()))
            com.Polarice3.Goety.common.network.ModNetwork.sendTo(observer,
                    new com.Polarice3.Goety.common.network.server.SSetPlayerOwnerPacket(clone));
        for (byte event:events(entity)) observer.connection.send(new ClientboundEntityEventPacket(entity,event));
    }
}
