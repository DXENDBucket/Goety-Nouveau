package dev.goetynouveau.mixin;

import com.Polarice3.Goety.client.events.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native Goety input handling keeps priority; the bridge contributes only its own preparation. */
@Mixin(value = ClientEvents.class, remap = false)
public abstract class NativeChantMovementMixin {
    @Inject(method = "updateInputEvent", at = @At("RETURN"))
    private static void goetyNouveau$movement(net.neoforged.neoforge.client.event.MovementInputUpdateEvent event, CallbackInfo ci) {
        if (event.getEntity() instanceof net.minecraft.client.player.LocalPlayer player)
            dev.goetynouveau.client.CastingMovement.apply(player, event.getInput());
    }
}
