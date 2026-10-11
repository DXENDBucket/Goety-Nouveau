package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.void_spells.BanishSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=BanishSpell.class,remap=false)
public abstract class NativeBanishMixin {
    @Redirect(method="conditionsMet(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)Z",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/common/magic/spells/void_spells/BanishSpell;entityResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;ID)Lnet/minecraft/world/phys/EntityHitResult;"))
    private EntityHitResult goetyNouveau$selected(BanishSpell spell,Level world,LivingEntity source,int reach,double padding){
        var v=GoetyNativeGeometry.current();
        return v!=null && v.spell()==spell && source==v.context().caster() && v.context().target() instanceof EntityHitResult hit
                ?hit:spell.entityResult(world,source,reach,padding);
    }
    @Redirect(method="touchResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;randomTeleport(DDDZ)Z",remap=true))
    private boolean goetyNouveau$actualTeleport(LivingEntity target,double x,double y,double z,boolean particles){
        var v=GoetyNativeGeometry.current();boolean scoped=v!=null && v.spell()==(Object)this;
        if(scoped){var pos=BlockPos.containing(x,y,z);if(!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)
                || !target.level().isInWorldBounds(pos) || !target.level().hasChunkAt(pos))return false;}
        boolean moved=target.randomTeleport(x,y,z,particles);
        if(scoped && moved)GoetyNativeGeometry.observe(this,v.context().caster(),new EntityHitResult(target));
        return moved;
    }
}
