package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.SpellHurtingProjectile;
import dev.goetynouveau.casting.ManagedBolt;
import net.minecraft.network.syncher.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SpellHurtingProjectile.class, remap = false)
public abstract class GoetyBoltStateMixin implements ManagedBolt {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$DRIVEN =
            SynchedEntityData.defineId(SpellHurtingProjectile.class, EntityDataSerializers.BOOLEAN);
    @Unique private boolean goetyNouveau$impacting;
    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void goetyNouveau$define(SynchedEntityData.Builder builder, CallbackInfo ci) { builder.define(goetyNouveau$DRIVEN, false); }
    public boolean goetyNouveau$managed() { return ((SpellHurtingProjectile)(Object)this).getEntityData().get(goetyNouveau$DRIVEN); }
    public void goetyNouveau$managed(boolean value) { ((SpellHurtingProjectile)(Object)this).getEntityData().set(goetyNouveau$DRIVEN, value); }
    public boolean goetyNouveau$impacting() { return goetyNouveau$impacting; }
    public void goetyNouveau$impacting(boolean value) { goetyNouveau$impacting = value; }
}
