package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.BouncyBubble;
import dev.goetynouveau.casting.ManagedBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Repeated carriers apply explosions only during a successfully paid native hit. */
@Mixin(value=BouncyBubble.class,remap=false)
public abstract class ManagedBubbleLifetimeMixin {
    @Inject(method="explode",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$unpaidExpiry(CallbackInfo ci) {
        if ((Object)this instanceof ManagedBolt state && state.goetyNouveau$managed()
                && state.goetyNouveau$arsDriven() && !state.goetyNouveau$handlingHit()) ci.cancel();
    }
}
