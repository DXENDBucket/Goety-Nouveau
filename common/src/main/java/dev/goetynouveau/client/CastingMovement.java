package dev.goetynouveau.client;

import com.Polarice3.Goety.config.SpellConfig;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

/** Mirrors native preparation movement without fake item use or native spell-casting state. */
public final class CastingMovement {
    private CastingMovement() {}
    public static void apply(LocalPlayer player, Input input) {
        if (!GoetyConfig.movementEnabled() || !ChantHudState.chanting() || player.isPassenger()) return;
        if (SpellConfig.FullStopCast.get()) {
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
        } else if (!player.isUsingItem()) {
            // Actual held-item use already gets Minecraft's 0.2 factor. Never apply that twice.
            input.leftImpulse *= 0.2f;
            input.forwardImpulse *= 0.2f;
        }
        player.setSprinting(false);
    }
}
