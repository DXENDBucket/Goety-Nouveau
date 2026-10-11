package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.void_spells.TunnelSpell;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=TunnelSpell.class,remap=false)
public abstract class NativeTunnelSpellMixin {
    @Unique private static boolean goetyNouveau$active() {
        var v=GoetyNativeGeometry.current();
        return v!=null && v.spell() instanceof TunnelSpell;
    }
    @Redirect(method="blockResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;rayTrace(Lnet/minecraft/world/entity/Entity;DZ)Lnet/minecraft/world/phys/BlockHitResult;"))
    private BlockHitResult goetyNouveau$hit(Entity caster,double distance,boolean fluids) {
        var v=GoetyNativeGeometry.current();
        return goetyNouveau$active() && v.spell()==(Object)this && caster==v.context().caster()
                && v.context().target() instanceof BlockHitResult hit?hit:MobUtil.rayTrace(caster,distance,fluids);
    }
    @Redirect(method="blockResult",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",remap=true))
    private BlockState goetyNouveau$scan(ServerLevel world,BlockPos pos) {
        return goetyNouveau$active() && !GoetyTunnelHoles.loaded(world,pos)?Blocks.BEDROCK.defaultBlockState():world.getBlockState(pos);
    }
    @ModifyArg(method="blockResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/void_spells/TunnelSpell;createHole(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;IZI)Z"),index=3)
    private int goetyNouveau$count(int nativeCount) {
        var v=GoetyNativeGeometry.current();
        if (!goetyNouveau$active() || v.spell()!=(Object)this || v.stats()==null) return nativeCount;
        // The native hole count is int, but blockResult accidentally narrows it to a byte.
        long count=1L+v.stats().getRange()+(WandUtil.enchantedFocus(v.context().caster())?WandUtil.getRangeLevel(v.context().caster()):0);
        return (int)Math.min(Integer.MAX_VALUE,Math.max(1,count));
    }
    @Inject(method="createHole",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$loaded(Level world,BlockPos pos,Direction face,int count,boolean staff,int lifespan,CallbackInfoReturnable<Boolean> ci) {
        if (goetyNouveau$active() && !GoetyTunnelHoles.loaded(world,pos)) ci.setReturnValue(false);
    }
    @Inject(method="createHole",at=@At("RETURN"))
    private static void goetyNouveau$created(Level world,BlockPos pos,Direction face,int count,boolean staff,int lifespan,CallbackInfoReturnable<Boolean> ci) {
        if (goetyNouveau$active() && ci.getReturnValueZ()) {
            GoetyTunnelHoles.mark(world,pos);
            var v=GoetyNativeGeometry.current();
            GoetyNativeGeometry.observe(v.spell(),v.context().caster(),new BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),face==null?Direction.UP:face,pos,false));
        }
    }
}
