package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.BurrowingSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=BurrowingSpell.class,remap=false)
public abstract class NativeBurrowingEnchantMixin {
    @Redirect(method={"SpellResult","breakBlocks"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getLevels(Lnet/minecraft/world/item/enchantment/Enchantment;Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int goetyNouveau$enchantment(net.minecraft.world.item.enchantment.Enchantment enchantment,LivingEntity actor){
        var view=GoetyNativeGeometry.current();
        return WandUtil.getLevels(enchantment,view!=null && view.spell()==(Object)this && actor==view.host()?view.context().caster():actor);
    }
}
