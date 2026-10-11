package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.TemblorClapSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

/** Only the native spatial reads move to Ars' point. The source body and gear stay original. */
@Mixin(value=TemblorClapSpell.class,remap=false)
public abstract class NativeTemblorGeometryMixin {
    @Unique private Vec3 goetyNouveau$point(LivingEntity source){
        var v=GoetyNativeGeometry.current();
        if(v==null || v.spell()!=(Object)this || v.context().caster()!=source)return source.position();
        var hit=v.context().target();return hit instanceof net.minecraft.world.phys.EntityHitResult e?e.getEntity().position():hit.getLocation();
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;position()Lnet/minecraft/world/phys/Vec3;",remap=true))
    private Vec3 goetyNouveau$position(LivingEntity source){return goetyNouveau$point(source);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getX()D",remap=true))
    private double goetyNouveau$x(LivingEntity source){return goetyNouveau$point(source).x;}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getY()D",remap=true))
    private double goetyNouveau$y(LivingEntity source){return goetyNouveau$point(source).y;}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getZ()D",remap=true))
    private double goetyNouveau$z(LivingEntity source){return goetyNouveau$point(source).z;}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;",remap=true))
    private net.minecraft.core.BlockPos goetyNouveau$block(LivingEntity source){return net.minecraft.core.BlockPos.containing(goetyNouveau$point(source));}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private net.minecraft.world.damagesource.DamageSource goetyNouveau$attribution(net.minecraft.world.damagesource.DamageSources sources,LivingEntity source){
        var v=GoetyNativeGeometry.current();return sources.mobAttack(v!=null && v.spell()==(Object)this && v.context().caster()==source && v.context().damageOwner()!=null?v.context().damageOwner():source);
    }
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/geomancy/TemblorClapSpell;playSound(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void goetyNouveau$sound(TemblorClapSpell spell,net.minecraft.server.level.ServerLevel world,net.minecraft.world.entity.Entity source,net.minecraft.sounds.SoundEvent sound,float volume,float pitch){
        var v=GoetyNativeGeometry.current();
        if(v!=null && v.spell()==spell && source==v.context().caster()){
            var point=goetyNouveau$point((LivingEntity)source);world.playSound(null,point.x,point.y,point.z,sound,spell.getSoundSource(),volume,pitch);
        }else spell.playSound(world,source,sound,volume,pitch);
    }
}
