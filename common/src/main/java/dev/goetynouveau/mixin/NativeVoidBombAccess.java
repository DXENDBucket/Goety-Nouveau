package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.VoidShockBomb;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=VoidShockBomb.class,remap=false)
public interface NativeVoidBombAccess {
    @Accessor("growTick") void goetyNouveau$growTick(int ticks);
}
