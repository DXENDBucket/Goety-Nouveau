package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.*;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=BlockFinder.class,remap=false)
public abstract class NativeSelectedBlockFaceMixin {
 @Redirect(method="multiBlockBreak",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;rayTrace(Lnet/minecraft/world/entity/Entity;DZ)Lnet/minecraft/world/phys/BlockHitResult;"))
 private static BlockHitResult goetyNouveau$face(Entity caster,double range,boolean fluids){var v=GoetyNativeGeometry.current();return v!=null && v.spell() instanceof com.Polarice3.Goety.common.magic.spells.geomancy.PulverizeSpell && caster==v.context().caster() && v.context().target() instanceof BlockHitResult hit?hit:MobUtil.rayTrace(caster,range,fluids);}
}
