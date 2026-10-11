package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.BurrowingSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=BurrowingSpell.class,remap=false)
public abstract class NativeBurrowingRecipientMixin {
    @Unique private LivingEntity goetyNouveau$gear(LivingEntity actor){var view=GoetyNativeGeometry.current();return view!=null && view.spell()==(Object)this && actor==view.host()?view.context().caster():actor;}
    @ModifyVariable(method="SpellResult",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$recipient(LivingEntity caster){return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$enchanted(LivingEntity caster){return WandUtil.enchantedFocus(goetyNouveau$gear(caster));}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getPotencyLevel(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int goetyNouveau$potency(LivingEntity caster){return WandUtil.getPotencyLevel(goetyNouveau$gear(caster));}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getRangeLevel(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int goetyNouveau$range(LivingEntity caster){return WandUtil.getRangeLevel(goetyNouveau$gear(caster));}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;isCrouching()Z",remap=true))
    private boolean goetyNouveau$sneak(Player player){var original=goetyNouveau$gear(player);return original!=player?GoetyResources.sneaking(original):player.isCrouching();}
}
