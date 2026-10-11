package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import com.Polarice3.Goety.utils.SEHelper;
import dev.goetynouveau.api.GoetyGrappleAccess;
import dev.goetynouveau.casting.GoetyGrappleCasting;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=VineHook.class,remap=false)
public abstract class NativeGrappleLeaseMixin {
    @Inject(method="shouldRetract",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$active(Player player,CallbackInfoReturnable<Boolean> ci){
        var hook=(VineHook)(Object)this;if(((GoetyGrappleAccess)hook).goetyNouveau$managed()){
            boolean retract=!GoetyGrappleCasting.valid(hook);if(retract)hook.discard();ci.setReturnValue(retract);
        }
    }
    @Inject(method="updateOwnerInfo",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$ownSlot(VineHook replacement,CallbackInfo ci){
        var hook=(VineHook)(Object)this;var player=hook.getPlayerOwner();
        if(replacement==null && ((GoetyGrappleAccess)hook).goetyNouveau$managed() && player!=null && SEHelper.getGrappling(player)!=hook)ci.cancel();
    }
}
