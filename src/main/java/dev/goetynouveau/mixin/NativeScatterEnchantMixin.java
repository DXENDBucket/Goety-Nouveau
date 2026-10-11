package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.ScatterSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ScatterSpell.class,remap=false)
public abstract class NativeScatterEnchantMixin {
 @Redirect(method={"useSpell","SpellResult"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getLevels(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/entity/LivingEntity;)I"))
 private int goetyNouveau$enchantment(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> enchantment,LivingEntity actor){var v=GoetyNativeGeometry.current();return WandUtil.getLevels(enchantment,v!=null && v.spell()==(Object)this && actor==v.host()?v.context().caster():actor);}
}
