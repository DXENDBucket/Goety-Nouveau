package dev.goetynouveau.platform;

import dev.goetynouveau.GoetyNouveau;
import dev.goetynouveau.casting.NativeVisualTracking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=GoetyNouveau.MOD_ID)
public final class VisualTrackingEvents {
    private VisualTrackingEvents() {}
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer observer) NativeVisualTracking.startTracking(observer,event.getTarget());
    }
}
