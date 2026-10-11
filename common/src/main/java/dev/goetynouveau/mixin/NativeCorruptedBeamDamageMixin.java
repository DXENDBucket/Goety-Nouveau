package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.CorruptedBeam;
import dev.goetynouveau.casting.ManagedBeam;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = CorruptedBeam.class, remap = false)
public abstract class NativeCorruptedBeamDamageMixin {
    @Redirect(method = "damageEntities", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;indirectMagic(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;", remap = true))
    private DamageSource goetyNouveau$owner(DamageSources sources, Entity direct, Entity owner) {
        var frame = ((ManagedBeam)this).goetyNouveau$beamContext();
        if (frame == null) return sources.indirectMagic(direct, owner);
        var attributed = frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
        return sources.indirectMagic(attributed, attributed);
    }
}
