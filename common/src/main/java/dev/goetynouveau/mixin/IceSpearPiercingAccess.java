package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.IceSpear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=IceSpear.class,remap=false)
public interface IceSpearPiercingAccess {
    @Invoker("resetPiercedEntities") void goetyNouveau$resetPiercing();
}
