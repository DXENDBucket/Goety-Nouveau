package dev.goetynouveau.mixin;

import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Only explicitly marked bridge areas expire with their source; native boss areas are untouched. */
@Mixin(targets={"com.Polarice3.Goety.common.entities.projectiles.AbstractSpellCloud","com.Polarice3.Goety.common.entities.util.MagicLightningTrap"},remap=false)
public abstract class NativeBossAreaMixin {
    @Inject(method="tick",remap=true,at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$lease(CallbackInfo ci) {
        var entity=(Entity)(Object)this;
        if(!entity.level().isClientSide && GoetyAreaProvenance.marked(entity) && !GoetyBossAbilities.live(entity)) {
            entity.discard();ci.cancel();
        }
    }
}
