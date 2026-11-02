package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.DeathArrow;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import dev.goetynouveau.platform.GoetyConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Native gear/environment qualifications stay in Goety; bound only bridge arrows' percentage supplement. */
@Mixin(value=DeathArrow.class,remap=false)
public abstract class NativeDeathArrowCapMixin {
    @ModifyArg(method="doPostHurtEffects",remap=true,at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;heal(F)V",remap=true),index=0)
    private float goetyNouveau$cap(float amount) {
        var arrow=(DeathArrow)(Object)this;
        if(amount<0 && GoetyAreaProvenance.marked(arrow) && GoetyConfig.capDeathArrowPercentage())
            return -Math.min(-amount,GoetyConfig.deathArrowPercentageCap());
        return amount;
    }
}
