package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.blocks.entities.HoleBlockEntity;
import com.Polarice3.Goety.common.magic.spells.void_spells.TunnelSpell;
import dev.goetynouveau.casting.GoetyTunnelHoles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=HoleBlockEntity.class,remap=false)
public abstract class NativeTunnelHoleMixin {
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/void_spells/TunnelSpell;createHole(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;IZI)Z"))
    private boolean goetyNouveau$child(Level world,BlockPos pos,Direction face,int count,boolean staff,int lifespan) {
        return GoetyTunnelHoles.marked((HoleBlockEntity)(Object)this)
                ?GoetyTunnelHoles.child(world,pos,face,count,staff,lifespan)
                :TunnelSpell.createHole(world,pos,face,count,staff,lifespan);
    }
    @Inject(method="writeNetwork(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/nbt/CompoundTag;",at=@At("RETURN"))
    private void goetyNouveau$save(CompoundTag tag,CallbackInfoReturnable<CompoundTag> ci) {
        if (GoetyTunnelHoles.marked((HoleBlockEntity)(Object)this)) ci.getReturnValue().putBoolean(GoetyTunnelHoles.MARKER,true);
    }
    @Inject(method="readNetwork(Lnet/minecraft/nbt/CompoundTag;)V",at=@At("RETURN"))
    private void goetyNouveau$load(CompoundTag tag,CallbackInfo ci) {
        if (tag.getBoolean(GoetyTunnelHoles.MARKER)) {
            var hole=(HoleBlockEntity)(Object)this;
            hole.getPersistentData().putBoolean(GoetyTunnelHoles.MARKER,true);
            int direction=tag.getInt("Direction");
            hole.direction=direction>=0 && direction<Direction.values().length?Direction.values()[direction]:null;
        }
    }
}
