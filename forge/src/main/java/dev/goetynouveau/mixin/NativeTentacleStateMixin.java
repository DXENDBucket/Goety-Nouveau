package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.neutral.GulfTentacle;
import dev.goetynouveau.casting.ArsTriggeredEntity;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only provenance is synchronized; orientation is read from the existing native owner. */
@Mixin(value=GulfTentacle.class,remap=false)
public abstract class NativeTentacleStateMixin implements ArsTriggeredEntity {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$ARS=SynchedEntityData.defineId(GulfTentacle.class,EntityDataSerializers.BOOLEAN);
    @Inject(method="defineSynchedData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$define(CallbackInfo ci){((GulfTentacle)(Object)this).getEntityData().define(goetyNouveau$ARS,false);}
    public boolean goetyNouveau$arsTriggered(){return ((GulfTentacle)(Object)this).getEntityData().get(goetyNouveau$ARS);}
    public void goetyNouveau$arsTriggered(boolean value){((GulfTentacle)(Object)this).getEntityData().set(goetyNouveau$ARS,value);}
    @Inject(method="addAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$save(CompoundTag data,CallbackInfo ci){data.putBoolean("GoetyNouveauTriggered",goetyNouveau$arsTriggered());}
    @Inject(method="readAdditionalSaveData",remap=true,at=@At("TAIL"))
    private void goetyNouveau$load(CompoundTag data,CallbackInfo ci){goetyNouveau$arsTriggered(data.getBoolean("GoetyNouveauTriggered"));}
}
