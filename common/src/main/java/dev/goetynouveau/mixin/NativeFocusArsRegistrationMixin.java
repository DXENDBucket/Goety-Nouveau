package dev.goetynouveau.mixin;

import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.goetynouveau.compat.NativeFocusIndex;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(value=ItemsRegistry.class,remap=false)
public abstract class NativeFocusArsRegistrationMixin {
    @ModifyExpressionValue(method="onItemRegistry",at=@At(value="INVOKE",target="Lcom/hollingsworth/arsnouveau/api/registry/GlyphRegistry;getGlyphItemMap()Ljava/util/Map;"))
    private static Map<ResourceLocation,Supplier<Glyph>> goetyNouveau$alreadyRegistered(Map<ResourceLocation,Supplier<Glyph>> items){
        return NativeFocusIndex.itemsForArsRegistration(items);
    }
}
