package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.LightProjectile;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = LightProjectile.class, remap = false)
public abstract class GoetyLightProjectileStateMixin extends net.minecraft.world.entity.projectile.Arrow implements ManagedBolt {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$DRIVEN =
            SynchedEntityData.defineId(LightProjectile.class, EntityDataSerializers.BOOLEAN);
    @Unique private boolean goetyNouveau$impacting;
    @Unique private boolean goetyNouveau$handlingHit;
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$ARS_DRIVEN =
            SynchedEntityData.defineId(LightProjectile.class, EntityDataSerializers.BOOLEAN);
    @Unique private static final EntityDataAccessor<Float> goetyNouveau$SPEED =
            SynchedEntityData.defineId(LightProjectile.class, EntityDataSerializers.FLOAT);
    @Unique private static final EntityDataAccessor<Float> goetyNouveau$NATIVE_SPEED =
            SynchedEntityData.defineId(LightProjectile.class, EntityDataSerializers.FLOAT);
    @Unique private dev.arsconflux.api.execution.ExecutionSession<?> goetyNouveau$session;
    @Unique private com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell goetyNouveau$trajectory;
    protected GoetyLightProjectileStateMixin(net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.projectile.Arrow> type, net.minecraft.world.level.Level world) { super(type,world); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(goetyNouveau$DRIVEN,false); builder.define(goetyNouveau$ARS_DRIVEN,false);
        builder.define(goetyNouveau$SPEED,1F); builder.define(goetyNouveau$NATIVE_SPEED,0F);
    }
    public boolean goetyNouveau$managed() { return ((LightProjectile)(Object)this).getEntityData().get(goetyNouveau$DRIVEN); }
    public void goetyNouveau$managed(boolean value) { ((LightProjectile)(Object)this).getEntityData().set(goetyNouveau$DRIVEN, value); }
    public boolean goetyNouveau$arsDriven() { return ((LightProjectile)(Object)this).getEntityData().get(goetyNouveau$ARS_DRIVEN); }
    public void goetyNouveau$arsDriven(boolean value) { ((LightProjectile)(Object)this).getEntityData().set(goetyNouveau$ARS_DRIVEN, value); }
    public float goetyNouveau$speedMultiplier() { return ((LightProjectile)(Object)this).getEntityData().get(goetyNouveau$SPEED); }
    public void goetyNouveau$speedMultiplier(float value) { ((LightProjectile)(Object)this).getEntityData().set(goetyNouveau$SPEED, value); }
    public float goetyNouveau$nativeSpeed() { return ((LightProjectile)(Object)this).getEntityData().get(goetyNouveau$NATIVE_SPEED); }
    public void goetyNouveau$nativeSpeed(float value) { ((LightProjectile)(Object)this).getEntityData().set(goetyNouveau$NATIVE_SPEED, value); }
    public dev.arsconflux.api.execution.ExecutionSession<?> goetyNouveau$session() { return goetyNouveau$session; }
    public void goetyNouveau$session(dev.arsconflux.api.execution.ExecutionSession<?> session) { goetyNouveau$session = session; }
    public com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell confluxTrajectory() { return goetyNouveau$trajectory; }
    public void goetyNouveau$trajectory(com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell carrier) { goetyNouveau$trajectory = carrier; }
    public boolean goetyNouveau$impacting() { return goetyNouveau$impacting; }
    public void goetyNouveau$impacting(boolean value) { goetyNouveau$impacting = value; }
    public boolean goetyNouveau$handlingHit() { return goetyNouveau$handlingHit; }
    public void goetyNouveau$handlingHit(boolean value) { goetyNouveau$handlingHit = value; }
}
