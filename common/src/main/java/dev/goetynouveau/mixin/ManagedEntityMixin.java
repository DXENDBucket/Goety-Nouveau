package dev.goetynouveau.mixin;

import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ManagedEntityMixin {
    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$deferImpactRemoval(Entity.RemovalReason reason, CallbackInfo ci) {
        if ((Object)this instanceof ManagedBolt state && state.goetyNouveau$managed()
                && state.goetyNouveau$impacting() && reason == Entity.RemovalReason.DISCARDED) ci.cancel();
    }
    @Inject(method = "shouldBeSaved", at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$transient(CallbackInfoReturnable<Boolean> ci) {
        if ((Object)this instanceof ManagedBolt state && state.goetyNouveau$managed()) ci.setReturnValue(false);
    }
}
