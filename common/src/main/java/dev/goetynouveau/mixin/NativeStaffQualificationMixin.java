package dev.goetynouveau.mixin;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.magic.Spell;
import dev.goetynouveau.api.StaffQualifications;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=Spell.class,remap=false)
public abstract class NativeStaffQualificationMixin {
    @Inject(method="rightStaff",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$matching(ItemStack staff,CallbackInfoReturnable<Boolean> cir) {
        var qualification=StaffQualifications.read(staff);
        if(qualification!=null&&qualification.matches(((Spell)(Object)this).getSpellType()))cir.setReturnValue(true);
    }
    @Inject(method="typeStaff",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$school(ItemStack staff,SpellType type,CallbackInfoReturnable<Boolean> cir) {
        var qualification=StaffQualifications.read(staff);
        if(qualification!=null&&qualification.matches(type))cir.setReturnValue(true);
    }
}
