package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.void_spells.EndWalkSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=EndWalkSpell.class,remap=false)
public abstract class NativeEndWalkMixin {
    @ModifyVariable(method="SpellResult",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$host(LivingEntity source){return GoetyNativeGeometry.spatial(this,source);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$sourceStats(LivingEntity actor){
        var v=GoetyNativeGeometry.current();
        return v!=null && v.spell()==(Object)this && actor==v.host()?false:WandUtil.enchantedFocus(actor);
    }
}
