package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.PoisonQuill;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = PoisonQuill.class, remap = false)
public interface QuillPiercingAccess {
    @Invoker("resetPiercedEntities") void goetyNouveau$resetPiercing();
}
