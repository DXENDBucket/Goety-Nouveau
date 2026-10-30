package dev.goetynouveau.mixin;

import dev.goetynouveau.api.StaffQualifications.Qualification;
import dev.goetynouveau.casting.QualifiedStaffAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class QualifiedStaffStackMixin implements QualifiedStaffAccess {
    @Unique private Qualification goetyNouveau$qualification;
    public Qualification goetyNouveau$qualification(){return goetyNouveau$qualification;}
    public void goetyNouveau$qualification(Qualification value){goetyNouveau$qualification=value;}

    @Inject(method="is(Lnet/minecraft/world/item/Item;)Z",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$identity(Item item,CallbackInfoReturnable<Boolean> cir) {
        // Ordinary inventory stacks pay only a null check and keep vanilla identity.
        if(goetyNouveau$qualification!=null&&goetyNouveau$qualification.matches(item))cir.setReturnValue(true);
    }

    @Inject(method="copy",at=@At("RETURN"))
    private void goetyNouveau$copy(CallbackInfoReturnable<ItemStack> cir) {
        var copy=cir.getReturnValue();
        if(goetyNouveau$qualification!=null&&!copy.isEmpty())
            ((QualifiedStaffAccess)(Object)copy).goetyNouveau$qualification(goetyNouveau$qualification);
    }
}
