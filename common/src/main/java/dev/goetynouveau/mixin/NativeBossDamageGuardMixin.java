package dev.goetynouveau.mixin;

import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** No team mutation: opt-in bombs, their fireworks and areas protect both original source and damage-owner allies. */
@Mixin(LivingEntity.class)
public abstract class NativeBossDamageGuardMixin {
    @Inject(method="hurt",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$friendly(DamageSource damage,float amount,CallbackInfoReturnable<Boolean> ci) {
        Entity payload=GoetyBossDamageScope.current();
        if(payload==null && damage!=null)payload=damage.getDirectEntity();
        if(payload!=null && GoetyAreaProvenance.marked(payload) && GoetyBossAbilities.friendly(payload,(Entity)(Object)this))
            ci.setReturnValue(false);
    }
}
