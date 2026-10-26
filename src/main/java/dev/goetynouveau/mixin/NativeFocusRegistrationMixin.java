package dev.goetynouveau.mixin;
import dev.goetynouveau.compat.NativeFocusIndex;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(MappedRegistry.class)
public abstract class NativeFocusRegistrationMixin {
    @Inject(method="register",at=@At("RETURN"))
    private void goetyNouveau$focus(ResourceKey<?> key,Object value,RegistrationInfo info,CallbackInfoReturnable<Holder.Reference<?>> cir){NativeFocusIndex.registered(key.location(),value);}
}
