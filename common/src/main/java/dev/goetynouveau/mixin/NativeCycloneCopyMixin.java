package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.*;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value={Cyclone.class,FireTornado.class},remap=false)
public abstract class NativeCycloneCopyMixin {
    @Redirect(method="fakeRemove",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$copy(Level world,Entity replacement){
        var previous=(AbstractCyclone)(Object)this;GoetyAreaProvenance.copy(previous,replacement);
        if(GoetyAreaProvenance.marked(previous) && replacement instanceof AbstractCyclone cyclone)cyclone.boltSpeed=previous.boltSpeed;
        return world.addFreshEntity(replacement);
    }
}
