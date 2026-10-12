package dev.goetynouveau.client;

import com.Polarice3.Goety.config.SpellConfig;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

/** Mirrors native preparation movement without fake item use or native spell-casting state. */
public final class CastingMovement {
    private CastingMovement() {}
    public static void apply(LocalPlayer player, Input input, Object event) {
        if (!GoetyConfig.movementEnabled() || !(ChantHudState.chanting() || ChantHudState.channeling()) || player.isPassenger()) return;
        if (SpellConfig.FullStopCast.get()) {
            input.leftImpulse = 0;
            input.forwardImpulse = 0;
            input.jumping = false;
        } else if (!player.isUsingItem()) {
            // Actual held-item use already gets Minecraft's 0.2 factor. Never apply that twice.
            float ratio=dev.arsconflux.api.casting.MovementLimits.multiplier(event, .2f);
            input.leftImpulse *= ratio;
            input.forwardImpulse *= ratio;
        }
        player.setSprinting(false);
    }
}
