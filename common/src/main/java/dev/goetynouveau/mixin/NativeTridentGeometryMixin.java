package dev.goetynouveau.mixin;

import com.Polarice3.Goety.utils.WandUtil;
import com.Polarice3.Goety.common.entities.util.TridentStorm;
import com.Polarice3.Goety.common.magic.spells.abyss.TridentStormSpell;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=WandUtil.class,remap=false)
public abstract class NativeTridentGeometryMixin {
    @Redirect(method={"summonTridentMinor","summonTridentSurround","summonTridentSquare","summonTridentWideCircle","summonTridentCross"},
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;position()Lnet/minecraft/world/phys/Vec3;",remap=true))
    private static Vec3 goetyNouveau$point(LivingEntity caster) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell() instanceof TridentStormSpell && caster==view.context().caster()) {
            var hit=view.context().target();return hit instanceof EntityHitResult e?e.getEntity().position():hit.getLocation();
        }
        return caster.position();
    }
    @Redirect(method="summonTridentStorm",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private static boolean goetyNouveau$owner(Level world,Entity entity) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell() instanceof TridentStormSpell && entity instanceof TridentStorm trident)
            trident.setOwner(view.context().damageOwner()!=null?view.context().damageOwner():view.context().caster());
        if(view!=null && view.spell() instanceof TridentStormSpell && entity instanceof TridentStorm)
            ((dev.goetynouveau.casting.ArsTriggeredEntity)entity).goetyNouveau$arsTriggered(true);
        return world.addFreshEntity(entity);
    }
}
