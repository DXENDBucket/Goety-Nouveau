package dev.goetynouveau.mixin;

import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyExecutorFacing;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Native forward/side offsets retain their math but read the scoped executor's head yaw. */
@Mixin(value=MobUtil.class,remap=false)
public abstract class NativeHorizontalFacingMixin {
    @Redirect(method={"getHorizontalLookAngle","getHorizontalLeftLookAngle","getHorizontalRightLookAngle"},
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;getYRot()F",remap=true))
    private static float goetyNouveau$yaw(Entity source) {return GoetyExecutorFacing.yaw(source);}
}
