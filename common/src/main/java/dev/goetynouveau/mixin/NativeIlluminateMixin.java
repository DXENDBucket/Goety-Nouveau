package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.utility.IlluminateSpell;
import com.Polarice3.Goety.common.blocks.ModBlocks;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=IlluminateSpell.class,remap=false)
public abstract class NativeIlluminateMixin {
    @Unique private boolean goetyNouveau$scoped(){var v=GoetyNativeGeometry.current();return v!=null && v.spell()==(Object)this;}
    @Unique private boolean goetyNouveau$loaded(ServerLevel world,BlockPos pos){
        var host=GoetyNativeGeometry.current().host();
        if(!world.isInWorldBounds(pos) || !world.isInWorldBounds(pos.below()))return false;
        int minX=Math.min(host.blockPosition().getX(),pos.getX()-1)>>4,maxX=Math.max(host.blockPosition().getX(),pos.getX()+1)>>4;
        int minZ=Math.min(host.blockPosition().getZ(),pos.getZ()-1)>>4,maxZ=Math.max(host.blockPosition().getZ(),pos.getZ()+1)>>4;
        for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++)if(!world.hasChunkAt(new BlockPos(x<<4,pos.getY(),z<<4)))return false;
        return true;
    }
    @ModifyVariable(method="useSpell",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$recipient(LivingEntity caster){return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$gear(LivingEntity host){var view=GoetyNativeGeometry.current();return WandUtil.enchantedFocus(goetyNouveau$scoped() && host==view.host()?view.context().caster():host);}
    @Inject(method="tryToPlaceAtPos",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$bounds(ServerLevel world,LivingEntity caster,BlockPos pos,CallbackInfoReturnable<Boolean> cir){if(goetyNouveau$scoped() && !goetyNouveau$loaded(world,pos))cir.setReturnValue(false);}
    @Inject(method="tryToPlaceGlowLightAround",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$support(BlockPos pos,LivingEntity caster,ServerLevel world,CallbackInfoReturnable<Boolean> cir){
        if(!goetyNouveau$scoped())return;
        if(!goetyNouveau$loaded(world,pos) || !ModBlocks.GLOW_LIGHT_BLOCK.get().defaultBlockState().canSurvive(world,pos)){cir.setReturnValue(false);return;}
        // The native placement eventually checks these same supports; reject impossible placements before its eight visibility rays.
        for(var side:new Direction[]{Direction.DOWN,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST}){
            var support=world.getBlockState(pos.relative(side));if(!support.isAir() && world.isUnobstructed(support,pos,CollisionContext.empty()))return;
        }
        cir.setReturnValue(false);
    }
    @Inject(method="replaceSoulLight",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$replacementBounds(BlockPos pos,ServerLevel world,CallbackInfoReturnable<Boolean> cir){if(goetyNouveau$scoped() && !goetyNouveau$loaded(world,pos))cir.setReturnValue(false);}
    @Inject(method={"tryToPlaceGlowLight","replaceSoulLight"},at=@At("RETURN"))
    private void goetyNouveau$observe(BlockPos pos,ServerLevel world,CallbackInfoReturnable<Boolean> cir){
        if(goetyNouveau$scoped() && cir.getReturnValueZ()){
            var view=GoetyNativeGeometry.current();GoetyNativeGeometry.observe(this,view.context().caster(),new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));
        }
    }
}
