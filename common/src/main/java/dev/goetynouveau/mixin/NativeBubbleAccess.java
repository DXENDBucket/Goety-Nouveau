package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.BouncyBubble;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=BouncyBubble.class,remap=false)
public interface NativeBubbleAccess {
    @Accessor("exploding") boolean goetyNouveau$exploding();
    @Accessor("exploding") void goetyNouveau$exploding(boolean value);
}
