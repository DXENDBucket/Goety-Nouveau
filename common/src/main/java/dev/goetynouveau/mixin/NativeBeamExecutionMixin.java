package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.projectiles.AbstractBeam;
import com.Polarice3.Goety.utils.MobUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.casting.ArsTriggeredEntity;
import dev.goetynouveau.casting.ManagedBeam;
import dev.goetynouveau.platform.NativeBreathHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Spatial owner remains native; only Ars beams get session checks and caster alliance filtering. */
@Mixin(value = AbstractBeam.class, remap = false)
public abstract class NativeBeamExecutionMixin implements ManagedBeam {
    @Unique private CastContext goetyNouveau$context;
    @Unique private BooleanSupplier goetyNouveau$active;

    public void goetyNouveau$bindBeam(CastContext context, BooleanSupplier active) {
        goetyNouveau$context = context;
        goetyNouveau$active = active;
        ((ArsTriggeredEntity)this).goetyNouveau$arsTriggered(true);
    }
    public CastContext goetyNouveau$beamContext() { return goetyNouveau$context; }

    @Inject(method = "tick", remap = true, at = @At("HEAD"), cancellable = true)
    private void goetyNouveau$life(CallbackInfo ci) {
        if (!((ArsTriggeredEntity)this).goetyNouveau$arsTriggered()) return;
        var beam = (AbstractBeam)(Object)this;
        if (beam.level().isClientSide) {
            // Native getOwner() only resolves player UUIDs on the client. Metadata also supports mobs.
            beam.getOptionalOwner();
        } else if (goetyNouveau$active == null || !goetyNouveau$active.getAsBoolean()) {
            beam.discard();
            ci.cancel();
        }
    }

    @Inject(method = "canHitEntity", at = @At("RETURN"), cancellable = true)
    private void goetyNouveau$allies(LivingEntity host, CallbackInfoReturnable<Predicate<LivingEntity>> cir) {
        if (goetyNouveau$context == null) return;
        var caster = goetyNouveau$context.caster();
        var owner = goetyNouveau$context.damageOwner();
        cir.setReturnValue(cir.getReturnValue().and(target -> target != caster && target != owner
                && !MobUtil.areAllies(caster, target)));
    }

    @WrapOperation(method = "tick", remap = true,
            at = @At(value = "INVOKE", target = "Lcom/Polarice3/Goety/common/entities/projectiles/AbstractBeam;damageEntities(Ljava/util/Set;)V", remap = false))
    private void goetyNouveau$damage(AbstractBeam beam, Set<LivingEntity> targets, Operation<Void> original) {
        if (goetyNouveau$context == null) original.call(beam, targets);
        else NativeBreathHooks.run((com.Polarice3.Goety.common.magic.Spell)EffectFocus.CORRUPTED_BEAM.spell(),
                () -> original.call(beam, targets));
    }
}
