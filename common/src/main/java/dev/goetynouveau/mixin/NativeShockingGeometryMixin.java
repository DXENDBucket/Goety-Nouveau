package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.storm.ShockingSpell;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=ShockingSpell.class,remap=false)
public abstract class NativeShockingGeometryMixin {
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getEyePosition()Lnet/minecraft/world/phys/Vec3;",remap=true))
    private Vec3 goetyNouveau$origin(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getEyePosition();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getX()D",remap=true))
    private double goetyNouveau$x(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getX();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getY()D",remap=true))
    private double goetyNouveau$y(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getY();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getZ()D",remap=true))
    private double goetyNouveau$z(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getZ();}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/storm/ShockingSpell;blockResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;D)Lnet/minecraft/world/phys/BlockHitResult;"))
    private BlockHitResult goetyNouveau$block(ShockingSpell spell,Level world,LivingEntity caster,double range) {return spell.blockResult(world,GoetyNativeGeometry.spatial(this,caster),range);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/storm/ShockingSpell;playSound(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;)V"))
    private void goetyNouveau$sound(ShockingSpell spell,net.minecraft.server.level.ServerLevel world,Entity caster,net.minecraft.sounds.SoundEvent sound) {
        spell.playSound(world,caster instanceof LivingEntity living?GoetyNativeGeometry.spatial(this,living):caster,sound);
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;getNearbyTarget(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;DD)Lnet/minecraft/world/entity/Entity;"))
    private Entity goetyNouveau$target(Level world,LivingEntity caster,double range,double radius) {
        var target=MobUtil.getNearbyTarget(world,GoetyNativeGeometry.spatial(this,caster),range,radius);
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && view.context().caster()==caster && target!=null
                && (target==caster || target==view.host() || target==view.context().damageOwner() || MobUtil.areAllies(caster,target)))return null;
        return target;
    }
}
