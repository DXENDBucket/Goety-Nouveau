package dev.goetynouveau.mixin;
import dev.goetynouveau.compat.NativeFocusResources;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ModelBakery.class)
public abstract class NativeFocusModelMixin {
    @Inject(method="loadBlockModel",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$model(ResourceLocation id,CallbackInfoReturnable<BlockModel> cir){
        var json=NativeFocusResources.model(id);if(json!=null){var model=BlockModel.fromString(json);model.name=id.toString();cir.setReturnValue(model);}
    }
}
