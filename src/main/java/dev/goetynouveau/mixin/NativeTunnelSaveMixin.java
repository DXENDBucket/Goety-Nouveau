package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.blocks.entities.SaveBlockEntity;
import com.Polarice3.Goety.common.blocks.entities.HoleBlockEntity;
import dev.goetynouveau.casting.GoetyTunnelHoles;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/** The 1.21 port's registry-aware overload bypasses HoleBlockEntity's network overrides. */
@Mixin(value=SaveBlockEntity.class,remap=false)
public abstract class NativeTunnelSaveMixin {
    @Inject(method="writeNetwork(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;",at=@At("RETURN"))
    private void goetyNouveau$save(CompoundTag tag,HolderLookup.Provider registry,CallbackInfoReturnable<CompoundTag> ci) {
        if ((Object)this instanceof HoleBlockEntity hole && GoetyTunnelHoles.marked(hole)) hole.writeNetwork(ci.getReturnValue());
    }
    @Inject(method="readNetwork(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;)V",at=@At("RETURN"))
    private void goetyNouveau$load(CompoundTag tag,HolderLookup.Provider registry,CallbackInfo ci) {
        if ((Object)this instanceof HoleBlockEntity hole && tag.getBoolean(GoetyTunnelHoles.MARKER)) hole.readNetwork(tag);
    }
}
