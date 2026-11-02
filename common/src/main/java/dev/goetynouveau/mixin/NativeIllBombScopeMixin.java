package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.IllBomb;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=IllBomb.class,remap=false)
public abstract class NativeIllBombScopeMixin {
    @WrapMethod(method="explode")
    private void goetyNouveau$scope(Operation<Void> original) {
        var bomb=(IllBomb)(Object)this;
        if(GoetyAreaProvenance.marked(bomb))GoetyBossDamageScope.during(bomb,()->original.call());
        else original.call();
    }
    @WrapOperation(method="explode",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$children(Level world,Entity child,Operation<Boolean> original) {
        var parent=(Entity)(Object)this;GoetyAreaProvenance.copy(parent,child);
        if(GoetyAreaProvenance.marked(parent))child.getPersistentData().putFloat(GoetyBossDamageScope.BONUS,parent.getPersistentData().getFloat(GoetyBossDamageScope.BONUS));
        boolean added=original.call(world,child);
        if(added && GoetyAreaProvenance.marked(parent))parent.getPersistentData().putBoolean(GoetyBossDamageScope.CHILDREN,true);
        return added;
    }
}
