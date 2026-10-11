package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.storm.ThunderstormSpell;
import com.Polarice3.Goety.common.entities.util.MagicLightningTrap;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=ThunderstormSpell.class,remap=false)
public abstract class NativeThunderstormGeometryMixin {
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;blockPosition()Lnet/minecraft/core/BlockPos;",remap=true))
    private BlockPos goetyNouveau$center(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster).blockPosition();}
    @Redirect(method="useSpell",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$owner(ServerLevel world,Entity entity) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && entity instanceof MagicLightningTrap trap)
            trap.setOwner(view.context().damageOwner()!=null?view.context().damageOwner():view.context().caster());
        return world.addFreshEntity(entity);
    }
}
