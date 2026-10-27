package dev.goetynouveau.mixin;
import dev.goetynouveau.compat.NativeFocusIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=ForgeRegistry.class,remap=false)
public abstract class NativeFocusRegistrationMixin {
    @Inject(method="register(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/Object;)V",at=@At("RETURN"))
    private void goetyNouveau$focus(ResourceLocation id,Object value,CallbackInfo ci){
        if(((ForgeRegistry<?>)(Object)this).getRegistryName().equals(net.minecraft.core.registries.Registries.ITEM.location()))NativeFocusIndex.registered(id,value);
    }
}
