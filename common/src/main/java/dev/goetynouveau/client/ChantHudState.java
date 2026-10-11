package dev.goetynouveau.client;

import dev.arsconflux.api.casting.ChantProgressTimer;
import dev.goetynouveau.network.ChantStatePayload;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import java.lang.ref.WeakReference;

/** A visual bridge only. Does not change native Goety capabilities, held focuses or casting status. */
public final class ChantHudState {
    private static final ChantProgressTimer TIMER = new ChantProgressTimer();
    private static WeakReference<ClientLevel> world = new WeakReference<>(null);
    private ChantHudState() {}
    public static void accept(ChantStatePayload data) {
        var client = Minecraft.getInstance();
        if (client.level != world.get()) TIMER.clear();
        world = new WeakReference<>(client.level);
        if (client.level != null) TIMER.update(data.token(), data.duration(), data.remaining(), client.level.getGameTime());
    }
    public static boolean visible() {
        var client = Minecraft.getInstance();
        var level = world.get();
        if (level == null || level != client.level || client.player == null || !client.player.isAlive() || client.player.isSpectator()) {
            TIMER.clear(); world.clear(); return false;
        }
        return !MobUtil.isSpellCasting(client.player) && TIMER.active(level.getGameTime());
    }
    public static float progress() { return TIMER.progress(Minecraft.getInstance().level.getGameTime()); }
}
