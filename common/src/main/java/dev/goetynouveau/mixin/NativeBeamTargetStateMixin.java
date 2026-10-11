package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.MiscCapHelper;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A dedicated visual entity replaces native player held-wand target capabilities only in this scope. */
@Mixin(value=MiscCapHelper.class,remap=false)
public abstract class NativeBeamTargetStateMixin {
    @Inject(method="setClientTarget",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$target(LivingEntity caster,Entity target,CallbackInfo ci) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.context().caster()==caster && view.observer()!=null) ci.cancel();
    }
}
