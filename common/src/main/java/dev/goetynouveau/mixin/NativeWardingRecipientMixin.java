package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.WardingSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=WardingSpell.class,remap=false)
public abstract class NativeWardingRecipientMixin {
    @ModifyVariable(method="SpellResult",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$recipient(LivingEntity caster) {return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$equipment(LivingEntity caster) {
        var view=GoetyNativeGeometry.current();
        return view!=null && view.spell()==(Object)this && caster==view.host()?false:WandUtil.enchantedFocus(caster);
    }
}
