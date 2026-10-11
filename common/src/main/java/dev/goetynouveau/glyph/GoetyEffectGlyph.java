package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.context.CastContexts;
import dev.arsconflux.api.glyph.TriggerPaidGlyph;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.casting.GoetyEffectCasting;
import dev.goetynouveau.casting.GoetyResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Effects execute at an Ars hit; their native resource charge is committed only on success. */
public final class GoetyEffectGlyph extends AbstractEffect implements TriggerPaidGlyph {
    private final EffectFocus focus;
    public GoetyEffectGlyph(EffectFocus focus) {
        super(CarrierRegistry.id("goety_nouveau:glyph_" + focus.glyph), focus.name);
        this.focus = focus;
        compatibleAugments.addAll(augments(focus));
        spellSchools.add(SpellSchools.CONJURATION);
        SpellSchools.CONJURATION.addSpellPart(this);
    }
    public EffectFocus focus() { return focus; }
    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
    @Override protected Set<AbstractAugment> getCompatibleAugments() {
        // Ars invokes this virtual method from its constructor, before focus is assigned.
        return Set.of();
    }
    private static Set<AbstractAugment> augments(EffectFocus focus) {
        var result = new HashSet<AbstractAugment>();
        if (focus.potency) { result.add(AugmentAmplify.INSTANCE); result.add(AugmentDampen.INSTANCE); }
        if (focus.duration) { result.add(AugmentExtendTime.INSTANCE); result.add(AugmentDurationDown.INSTANCE); }
        if (focus.radius) result.add(AugmentAOE.INSTANCE);
        if (focus == EffectFocus.BOLTING) { result.add(AugmentAccelerate.INSTANCE); result.add(AugmentDecelerate.INSTANCE); }
        if (GoetyAugmentValidation.supportsBurning(focus)) result.add(AugmentBurning.INSTANCE);
        for (var augment : AugmentGoety.ALL) if (augment.supports(focus)) result.add(augment);
        return result;
    }
    @Override public ResourceCharge firstCharge(SpellContext context, List<AbstractAugment> augments) {
        return GoetyResources.quote(context, focus.spell(), augments);
    }
    @Override public void onResolve(HitResult hit, Level world, LivingEntity ignoredCaster, SpellStats stats,
                                     SpellContext context, SpellResolver resolver) {
        if (!(world instanceof ServerLevel server) || !isEnabled()) return;
        if (!GoetyAugmentValidation.validEffect(focus, stats.getAugments())) return;
        var frame = CastContext.of(context, hit, null);
        var caster = frame.caster();
        if (caster == null || !caster.isAlive() || caster.level() != server) return;
        var spell = focus.spell();
        // Soul Heal's native mob condition checks the caster's health. An Ars target can be
        // a different injured ally, so validate the actual recipient in the adapter instead.
        if (!focus.breathing() && !focus.rain() && focus != EffectFocus.KILLING && focus != EffectFocus.SOUL_HEAL && focus != EffectFocus.IRON_HIDE && focus != EffectFocus.BULWARK
                && !(spell instanceof com.Polarice3.Goety.common.magic.SummonSpell && GoetyResources.sneaking(caster))
                && !spell.conditionsMet(server, caster)) return;
        var charge = GoetyResources.quote(context, spell, stats.getAugments());
        // Preserve the chain's explicit source account. This focus's native payment belongs
        // to this activation; publishing it as the inherited account would redirect other
        // providers' later effects into Goety's soul pool.
        CastContexts.bind(context, frame);
        var selected = frame.withAccount(charge.account());
        CastContexts.scoped(selected, () -> GoetyResources.activation(charge).trigger(
                () -> GoetyEffectCasting.apply(focus, server, selected, stats, charge)));
    }
}
