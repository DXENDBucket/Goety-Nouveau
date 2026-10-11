package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.abyss.PrismaBeamSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=PrismaBeamSpell.class,remap=false)
public abstract class NativePrismaGeometryMixin {
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getX()D",remap=true))
    private double goetyNouveau$x(LivingEntity entity) {return GoetyNativeGeometry.spatial(this,entity).getX();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getZ()D",remap=true))
    private double goetyNouveau$z(LivingEntity entity) {return GoetyNativeGeometry.spatial(this,entity).getZ();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getEyeY()D",remap=true))
    private double goetyNouveau$eyes(LivingEntity entity) {return GoetyNativeGeometry.spatial(this,entity).getEyeY();}
    @org.spongepowered.asm.mixin.Unique
    private LivingEntity goetyNouveau$attributed(LivingEntity caster) {
        var view=GoetyNativeGeometry.current();
        return view!=null && view.spell()==(Object)this && view.context().caster()==caster && view.context().damageOwner()!=null?view.context().damageOwner():caster;
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;indirectMagic(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private DamageSource goetyNouveau$magic(DamageSources sources,Entity direct,Entity caster) {
        var owner=caster instanceof LivingEntity living?goetyNouveau$attributed(living):caster;
        return sources.indirectMagic(direct==caster?owner:direct,owner);
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private DamageSource goetyNouveau$attack(DamageSources sources,LivingEntity caster) {return sources.mobAttack(goetyNouveau$attributed(caster));}
}
