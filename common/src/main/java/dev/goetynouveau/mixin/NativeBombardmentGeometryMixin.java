package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.nether.BombardmentSpell;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=BombardmentSpell.class,remap=false)
public abstract class NativeBombardmentGeometryMixin {
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;",remap=true))
    private Vec3 goetyNouveau$view(LivingEntity caster,float partial) {return GoetyNativeGeometry.spatial(this,caster).getViewVector(partial);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getX()D",remap=true))
    private double goetyNouveau$x(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getX();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getZ()D",remap=true))
    private double goetyNouveau$z(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getZ();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getEyeY()D",remap=true))
    private double goetyNouveau$eyes(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getEyeY();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;",remap=true))
    private Vec3 goetyNouveau$motion(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getDeltaMovement();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;forcePush(Lnet/minecraft/world/entity/Entity;DDD)V"))
    private void goetyNouveau$hover(Entity caster,double x,double y,double z) {
        var actual=caster instanceof LivingEntity living?GoetyNativeGeometry.spatial(this,living):caster;
        MobUtil.forcePush(actual,x,y,z);
    }
}
