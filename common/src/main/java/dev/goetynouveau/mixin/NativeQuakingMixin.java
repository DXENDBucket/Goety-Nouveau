package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.QuakingSpell;
import com.Polarice3.Goety.utils.*;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=QuakingSpell.class,remap=false)
public abstract class NativeQuakingMixin {
    @ModifyVariable(method="useSpell",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$executor(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$equipment(LivingEntity caster) {
        var view=GoetyNativeGeometry.current();
        return view!=null && view.spell()==(Object)this && caster==view.host()?false:WandUtil.enchantedFocus(caster);
    }
    @Redirect(method={"tremor","surroundTremor"},at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private static DamageSource goetyNouveau$damage(DamageSources sources,LivingEntity caster) {
        var view=GoetyNativeGeometry.current();
        return sources.mobAttack(view!=null && view.spell() instanceof QuakingSpell && view.context().damageOwner()!=null?view.context().damageOwner():caster);
    }
    @Redirect(method="spawnBlocks",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private DamageSource goetyNouveau$arcDamage(DamageSources sources,LivingEntity caster) {return goetyNouveau$damage(sources,caster);}
    @Redirect(method={"tremor","surroundTremor"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;areAllies(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean goetyNouveau$allies(Entity a,Entity b) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell() instanceof QuakingSpell && (a==view.host() || b==view.host())) {
            var target=a==view.host()?b:a;
            if(target==view.context().caster() || target==view.context().damageOwner() || MobUtil.areAllies(view.context().caster(),target))return true;
        }
        return MobUtil.areAllies(a,b);
    }
    @Redirect(method="spawnBlocks",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;areAllies(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean goetyNouveau$arcAllies(Entity a,Entity b) {return goetyNouveau$allies(a,b);}
    @Redirect(method="surroundTremor",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",remap=true))
    private static BlockState goetyNouveau$loadedGround(Level world,BlockPos pos) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell() instanceof QuakingSpell && (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos)))return Blocks.AIR.defaultBlockState();
        return world.getBlockState(pos);
    }
    @ModifyExpressionValue(method="surroundTremor",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;",remap=true))
    private static RenderShape goetyNouveau$groundBoundary(RenderShape shape,@Local(argsOnly=true)LivingEntity caster,@Local(ordinal=0)BlockPos pos) {
        var view=GoetyNativeGeometry.current();
        // Terminate the original downward search at an unloaded chunk or build boundary.
        if(view!=null && view.spell() instanceof QuakingSpell && (caster.level().isOutsideBuildHeight(pos) || !caster.level().hasChunkAt(pos)))return RenderShape.MODEL;
        return shape;
    }
}
