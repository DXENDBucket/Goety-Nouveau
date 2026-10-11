package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.TidalSurge;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=TidalSurge.class,remap=false)
public abstract class NativeTidalAreaMixin {
    @Redirect(method="attackEntities",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;areAllies(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean goetyNouveau$friends(Entity candidate,Entity owner){return MobUtil.areAllies(candidate,owner)||GoetyAreaProvenance.friendly((Entity)(Object)this,candidate);}
}
