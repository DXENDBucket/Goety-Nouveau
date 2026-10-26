package dev.goetynouveau.mixin;
import com.Polarice3.Goety.utils.WandUtil;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import dev.goetynouveau.casting.NativeFocusScope;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=WandUtil.class,remap=false)
public abstract class NativeFocusWandMixin {
    @Inject(method="findWand",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$wand(LivingEntity caster,CallbackInfoReturnable<ItemStack> cir){var s=NativeFocusScope.current(caster);if(s!=null)cir.setReturnValue(s.wand);}
    @Inject(method="findFocus",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$focus(LivingEntity caster,CallbackInfoReturnable<ItemStack> cir){var s=NativeFocusScope.current(caster);if(s!=null)cir.setReturnValue(s.focus);}
    @Inject(method="getShots",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$shots(LivingEntity caster,CallbackInfoReturnable<Integer> cir){var s=NativeFocusScope.current(caster);if(s!=null)cir.setReturnValue(s.shots);}
    @Inject(method="getStats",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$stats(LivingEntity caster,ISpell spell,CallbackInfoReturnable<SpellStat> cir){var s=NativeFocusScope.current(caster);if(s!=null&&s.spell==spell&&s.stats!=null)cir.setReturnValue(new SpellStat(s.stats.getPotency(),s.stats.getDuration(),s.stats.getRange(),s.stats.getRadius(),s.stats.getBurning(),s.stats.getVelocity()));}
}
