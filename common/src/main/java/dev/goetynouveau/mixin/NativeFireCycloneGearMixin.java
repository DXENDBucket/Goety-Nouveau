package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.FireTornado;
import com.Polarice3.Goety.utils.CuriosFinder;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=FireTornado.class,remap=false)
public abstract class NativeFireCycloneGearMixin {
    @Redirect(method="hurtMobs",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/CuriosFinder;hasUnholySet(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$originalGear(LivingEntity owner){return CuriosFinder.hasUnholySet(GoetyAreaProvenance.source((Entity)(Object)this,owner));}
}
