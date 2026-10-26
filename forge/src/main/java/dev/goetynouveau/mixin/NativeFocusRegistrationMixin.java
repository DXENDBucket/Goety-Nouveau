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
    private void goetyNouveau$focus(ResourceLocation id,Object value,CallbackInfo ci){NativeFocusIndex.registered(id,value);}
}
