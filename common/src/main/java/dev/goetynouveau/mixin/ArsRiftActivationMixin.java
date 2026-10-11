package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The native pre-activation cancellation checks a held Goety wand. Ars already completed
 * preparation before spawning this rift; let its own first tick activate and broadcast normally. */
@Mixin(value=CastSpellTrap.class,remap=false)
public abstract class ArsRiftActivationMixin {
    @Redirect(method="tick",remap=true,at=@At(value="INVOKE",
            target="Lcom/Polarice3/Goety/common/entities/util/CastSpellTrap;isActivated()Z",remap=false))
    private boolean goetyNouveau$completedPreparation(CastSpellTrap trap) {
        return trap.isActivated() || (trap instanceof VoidRift rift && rift.warmUp<=0
                && rift.getPersistentData().getBoolean("goety_nouveau:ars_activation"));
    }
}
