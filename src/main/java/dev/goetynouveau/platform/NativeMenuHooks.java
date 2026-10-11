package dev.goetynouveau.platform;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;

/** Match Ars' personal-menu boundary without a cross-loader dependency. */
public final class NativeMenuHooks {
    private NativeMenuHooks() {}
    public static boolean realPlayer(ServerPlayer player) { return !(player instanceof FakePlayer); }
}
