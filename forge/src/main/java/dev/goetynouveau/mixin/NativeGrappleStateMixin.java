package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import dev.goetynouveau.api.GoetyGrappleAccess;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=VineHook.class,remap=false)
public abstract class NativeGrappleStateMixin implements GoetyGrappleAccess {
 @Unique private static final EntityDataAccessor<Byte> goetyNouveau$HAND=SynchedEntityData.defineId(VineHook.class,EntityDataSerializers.BYTE);
 @Inject(method="defineSynchedData",at=@At("TAIL"),remap=true)
 private void goetyNouveau$define(CallbackInfo ci){((VineHook)(Object)this).getEntityData().define(goetyNouveau$HAND,(byte)-1);}
 public int goetyNouveau$hand(){return ((VineHook)(Object)this).getEntityData().get(goetyNouveau$HAND);}
 public void goetyNouveau$hand(int hand){((VineHook)(Object)this).getEntityData().set(goetyNouveau$HAND,(byte)hand);}
}
