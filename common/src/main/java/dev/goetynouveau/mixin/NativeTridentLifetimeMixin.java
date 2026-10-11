package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.util.CastSpellTrap;
import com.Polarice3.Goety.common.entities.util.TridentStorm;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.ArsTriggeredEntity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** The paid Ars area effect outlives right-click; native wand traps keep their casting requirement. */
@Mixin(value=CastSpellTrap.class,remap=false)
public abstract class NativeTridentLifetimeMixin {
    @Redirect(method="tick",remap=true,at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;isSpellCasting(Lnet/minecraft/world/entity/LivingEntity;)Z",remap=false))
    private boolean goetyNouveau$active(LivingEntity owner) {
        if((Object)this instanceof TridentStorm && ((ArsTriggeredEntity)this).goetyNouveau$arsTriggered()) return owner.isAlive();
        return MobUtil.isSpellCasting(owner);
    }
}
