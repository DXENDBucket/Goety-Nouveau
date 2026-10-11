package dev.goetynouveau.mixin;

import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyBlinkCasting;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Observe the real native random teleport result; do not replace its search or events. */
@Mixin(value=MobUtil.class,remap=false)
public abstract class NativeBlinkTeleportMixin {
    @Inject(method="teleport(Lnet/minecraft/world/entity/LivingEntity;DDD)Z",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$loadedCandidate(LivingEntity actor,double x,double y,double z,CallbackInfoReturnable<Boolean> cir){
        if(GoetyBlinkCasting.managed(actor) && !GoetyBlinkCasting.destination(actor,x,y,z))cir.setReturnValue(false);
    }
    @Inject(method="teleport(Lnet/minecraft/world/entity/LivingEntity;DDD)Z",at=@At("RETURN"))
    private static void goetyNouveau$nativeResult(LivingEntity actor,double x,double y,double z,CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValueZ())GoetyBlinkCasting.moved(actor);
    }
}
