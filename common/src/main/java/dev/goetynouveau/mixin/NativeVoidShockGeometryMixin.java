package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.void_spells.VoidShockSpell;
import com.Polarice3.Goety.common.entities.projectiles.VoidShock;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=VoidShockSpell.class,remap=false)
public abstract class NativeVoidShockGeometryMixin {
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;position()Lnet/minecraft/world/phys/Vec3;",remap=true))
    private Vec3 goetyNouveau$position(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).position();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getBbHeight()F",remap=true))
    private float goetyNouveau$height(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).getBbHeight();}
    @Redirect(method="useSpell",at=@At(value="NEW",target="com/Polarice3/Goety/common/entities/projectiles/VoidShock"))
    private VoidShock goetyNouveau$owner(LivingEntity caster,LivingEntity target,Level world) {
        var view=GoetyNativeGeometry.current();
        return new VoidShock(view!=null && view.spell()==(Object)this && view.context().caster()==caster && view.context().damageOwner()!=null
                ?view.context().damageOwner():caster,target,world);
    }
}
