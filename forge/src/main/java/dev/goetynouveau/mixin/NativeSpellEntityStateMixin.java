package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.SpellEntity;
import dev.goetynouveau.casting.ArsTriggeredEntity;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Metadata arrives with native spawn data; client preactivation ticks must see provenance too. */
@Mixin(value=SpellEntity.class,remap=false)
public abstract class NativeSpellEntityStateMixin implements ArsTriggeredEntity {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$ARS=SynchedEntityData.defineId(SpellEntity.class,EntityDataSerializers.BOOLEAN);
    @Inject(method="defineSynchedData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$define(CallbackInfo ci) {((SpellEntity)(Object)this).getEntityData().define(goetyNouveau$ARS,false);}
    public boolean goetyNouveau$arsTriggered() {return ((SpellEntity)(Object)this).getEntityData().get(goetyNouveau$ARS);}
    public void goetyNouveau$arsTriggered(boolean value) {((SpellEntity)(Object)this).getEntityData().set(goetyNouveau$ARS,value);}
    @Inject(method="addAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$save(CompoundTag data,CallbackInfo ci) {if(goetyNouveau$arsTriggered()) data.putBoolean("GoetyNouveauTriggered",true);}
    @Inject(method="readAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$load(CompoundTag data,CallbackInfo ci) {goetyNouveau$arsTriggered(data.getBoolean("GoetyNouveauTriggered"));}
}
