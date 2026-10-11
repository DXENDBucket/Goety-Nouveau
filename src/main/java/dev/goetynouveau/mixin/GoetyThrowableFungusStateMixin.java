package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.ThrowableFungus;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ThrowableFungus.class, remap = false)
public abstract class GoetyThrowableFungusStateMixin implements ManagedBolt {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$DRIVEN =
            SynchedEntityData.defineId(ThrowableFungus.class, EntityDataSerializers.BOOLEAN);
    @Unique private boolean goetyNouveau$impacting;
    @Unique private boolean goetyNouveau$handlingHit;
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$ARS_DRIVEN =
            SynchedEntityData.defineId(ThrowableFungus.class, EntityDataSerializers.BOOLEAN);
    @Unique private static final EntityDataAccessor<Float> goetyNouveau$SPEED =
            SynchedEntityData.defineId(ThrowableFungus.class, EntityDataSerializers.FLOAT);
    @Unique private static final EntityDataAccessor<Float> goetyNouveau$NATIVE_SPEED =
            SynchedEntityData.defineId(ThrowableFungus.class, EntityDataSerializers.FLOAT);
    @Unique private dev.arsconflux.api.execution.ExecutionSession<?> goetyNouveau$session;
    @Unique private com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell goetyNouveau$trajectory;
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void goetyNouveau$define(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(goetyNouveau$DRIVEN, false); builder.define(goetyNouveau$ARS_DRIVEN, false);
        builder.define(goetyNouveau$SPEED, 1.0F); builder.define(goetyNouveau$NATIVE_SPEED, 0.0F);
    }
    public boolean goetyNouveau$managed() { return ((ThrowableFungus)(Object)this).getEntityData().get(goetyNouveau$DRIVEN); }
    public void goetyNouveau$managed(boolean value) { ((ThrowableFungus)(Object)this).getEntityData().set(goetyNouveau$DRIVEN, value); }
    public boolean goetyNouveau$arsDriven() { return ((ThrowableFungus)(Object)this).getEntityData().get(goetyNouveau$ARS_DRIVEN); }
    public void goetyNouveau$arsDriven(boolean value) { ((ThrowableFungus)(Object)this).getEntityData().set(goetyNouveau$ARS_DRIVEN, value); }
    public float goetyNouveau$speedMultiplier() { return ((ThrowableFungus)(Object)this).getEntityData().get(goetyNouveau$SPEED); }
    public void goetyNouveau$speedMultiplier(float value) { ((ThrowableFungus)(Object)this).getEntityData().set(goetyNouveau$SPEED, value); }
    public float goetyNouveau$nativeSpeed() { return ((ThrowableFungus)(Object)this).getEntityData().get(goetyNouveau$NATIVE_SPEED); }
    public void goetyNouveau$nativeSpeed(float value) { ((ThrowableFungus)(Object)this).getEntityData().set(goetyNouveau$NATIVE_SPEED, value); }
    public dev.arsconflux.api.execution.ExecutionSession<?> goetyNouveau$session() { return goetyNouveau$session; }
    public void goetyNouveau$session(dev.arsconflux.api.execution.ExecutionSession<?> session) { goetyNouveau$session = session; }
    public com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell confluxTrajectory() { return goetyNouveau$trajectory; }
    public void goetyNouveau$trajectory(com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell carrier) { goetyNouveau$trajectory = carrier; }
    public boolean goetyNouveau$impacting() { return goetyNouveau$impacting; }
    public void goetyNouveau$impacting(boolean value) { goetyNouveau$impacting = value; }
    public boolean goetyNouveau$handlingHit() { return goetyNouveau$handlingHit; }
    public void goetyNouveau$handlingHit(boolean value) { goetyNouveau$handlingHit = value; }
}
