package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import dev.goetynouveau.casting.RiftVisualState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the native size/damage semantics, but cull the visible billboard rather than a
 * zero-sized physical box. Tracking metadata also survives missed one-shot activation events. */
@Mixin(value=VoidRift.class,remap=false)
public abstract class ArsRiftVisualMixin extends CastSpellTrap {
    protected ArsRiftVisualMixin(EntityType<?> type, Level world) { super(type,world); }
    @Unique private RiftVisualState goetyNouveau$visuals() { return (RiftVisualState)(Object)this; }
    @Unique private AABB goetyNouveau$billboardBounds() {
        var rift=(VoidRift)(Object)this;
        double size=(4+rift.getSize())*(rift.isActivated()?1.5:1);
        return new AABB(position(),position()).inflate(size);
    }
    @Override public AABB getBoundingBoxForCulling() {
        return goetyNouveau$visuals().goetyNouveau$visualPhase()>0 ? goetyNouveau$billboardBounds() : super.getBoundingBoxForCulling();
    }
    @Override public boolean shouldRenderAtSqrDistance(double distance) {
        if (goetyNouveau$visuals().goetyNouveau$visualPhase()==0) return super.shouldRenderAtSqrDistance(distance);
        double range=goetyNouveau$billboardBounds().getSize()*64*Entity.getViewScale();
        return distance<range*range;
    }
    @Inject(method="tick",remap=true,at=@At("TAIL"))
    private void goetyNouveau$publishClosing(CallbackInfo ci) {
        if (!level().isClientSide && goetyNouveau$visuals().goetyNouveau$visualPhase()>0
                && ((VoidRift)(Object)this).isClosing)
            goetyNouveau$visuals().goetyNouveau$visualPhase(RiftVisualState.CLOSING);
    }
    @Inject(method="addAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$save(CompoundTag tag,CallbackInfo ci) {
        int phase=goetyNouveau$visuals().goetyNouveau$visualPhase();
        if (phase>0) tag.putInt("GoetyNouveauRiftPhase",phase);
    }
    @Inject(method="readAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$load(CompoundTag tag,CallbackInfo ci) {
        if (tag.contains("GoetyNouveauRiftPhase")) goetyNouveau$visuals().goetyNouveau$visualPhase(tag.getInt("GoetyNouveauRiftPhase"));
    }
}
