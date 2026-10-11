package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.util.VoidRift;
import dev.goetynouveau.casting.RiftVisualState;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native activated/closing booleans are not synced. Store only the bridge's visual phase
 * in spawn/tracking metadata; native renderers and client animation still do the rendering. */
@Mixin(value=VoidRift.class,remap=false)
public abstract class ArsRiftDataMixin implements RiftVisualState {
    @Unique private static final EntityDataAccessor<Integer> goetyNouveau$PHASE =
            SynchedEntityData.defineId(VoidRift.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void goetyNouveau$define(SynchedEntityData.Builder builder,CallbackInfo ci) {
        builder.define(goetyNouveau$PHASE,RiftVisualState.NATIVE);
    }
    public int goetyNouveau$visualPhase() { return ((VoidRift)(Object)this).getEntityData().get(goetyNouveau$PHASE); }
    public void goetyNouveau$visualPhase(int phase) {
        if (phase<RiftVisualState.NATIVE || phase>RiftVisualState.CLOSING) throw new IllegalArgumentException("Invalid rift visual phase");
        ((VoidRift)(Object)this).getEntityData().set(goetyNouveau$PHASE,phase);
    }
    @Inject(method="onSyncedDataUpdated",remap=true,at=@At("TAIL"))
    private void goetyNouveau$restoreVisuals(EntityDataAccessor<?> accessor,CallbackInfo ci) {
        if (!goetyNouveau$PHASE.equals(accessor)) return;
        int phase=goetyNouveau$visualPhase(); if (phase==RiftVisualState.NATIVE) return;
        var rift=(VoidRift)(Object)this;
        rift.setActivated(true);
        rift.isClosing=phase==RiftVisualState.CLOSING;
        rift.setAnimation(rift.isClosing?2:3);
    }
}
