package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.AbstractBeam;
import dev.goetynouveau.casting.ArsTriggeredEntity;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Client interpolation uses the real native host; Ars beams do not send held-wand aim packets. */
@Mixin(value = AbstractBeam.class, remap = false)
public abstract class NativeBeamStateMixin implements ArsTriggeredEntity {
    @Unique private static final EntityDataAccessor<Boolean> goetyNouveau$ARS = SynchedEntityData.defineId(AbstractBeam.class, EntityDataSerializers.BOOLEAN);
    @Inject(method = "defineSynchedData", remap = true, at = @At("TAIL"))
    private void goetyNouveau$define(CallbackInfo ci) {((AbstractBeam)(Object)this).getEntityData().define(goetyNouveau$ARS, false);}
    public boolean goetyNouveau$arsTriggered() {return ((AbstractBeam)(Object)this).getEntityData().get(goetyNouveau$ARS);}
    public void goetyNouveau$arsTriggered(boolean value) {((AbstractBeam)(Object)this).getEntityData().set(goetyNouveau$ARS, value);}
    @Inject(method = "addAdditionalSaveData", remap = true, at = @At("TAIL"))
    private void goetyNouveau$save(CompoundTag data, CallbackInfo ci) {if (goetyNouveau$arsTriggered()) data.putBoolean("GoetyNouveauTriggered", true);}
    @Inject(method = "readAdditionalSaveData", remap = true, at = @At("TAIL"))
    private void goetyNouveau$load(CompoundTag data, CallbackInfo ci) {goetyNouveau$arsTriggered(data.getBoolean("GoetyNouveauTriggered"));}
    @Redirect(method = "tick", remap = true, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/network/simple/SimpleChannel;sendToServer(Ljava/lang/Object;)V", remap = false))
    private void goetyNouveau$packet(net.minecraftforge.network.simple.SimpleChannel channel, Object packet) {
        if (!goetyNouveau$arsTriggered()) channel.sendToServer(packet);
    }
}
