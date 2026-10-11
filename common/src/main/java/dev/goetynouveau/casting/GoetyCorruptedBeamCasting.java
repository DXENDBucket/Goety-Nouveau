package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.CorruptedBeam;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** A real native beam and renderer, bounded by the same paid channel used for breaths. */
public final class GoetyCorruptedBeamCasting {
    private GoetyCorruptedBeamCasting() {}

    public static boolean apply(ServerLevel world, CastContext frame, SpellStats ars, ResourceCharge charge) {
        if (!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world, frame.caster(), host)) return false;
        var focus = EffectFocus.CORRUPTED_BEAM;
        var spell = (Spell)focus.spell();
        var staff = GoetyResources.staff(frame.caster()).copy();
        int ticks = GoetyChannelCasting.boundedTicks(
                ((com.Polarice3.Goety.api.magic.IChargingSpell)spell).shotsNumber(frame.caster(), staff),
                ars.getDurationMultiplier());
        if (ticks <= 0) return false;
        var stats = WandUtil.getStats(frame.caster(), spell);
        stats.setPotency(Math.max(0, stats.getPotency() + (int)ars.getAmpMultiplier()));
        float extraDamage = stats.getPotency();
        if (WandUtil.enchantedFocus(frame.caster())) extraDamage += WandUtil.getPotencyLevel(frame.caster()) / 2.0f;

        var beam = new CorruptedBeam(ModEntityType.CORRUPTED_BEAM.get(), world, host);
        beam.setExtraDamage(extraDamage);
        beam.setItemBase(false); // Holding a native wand is replaced by the execution session.
        long expires = world.getGameTime() + ticks;
        boolean[] open = {true};
        ((ManagedBeam)beam).goetyNouveau$bindBeam(frame.withExecutor(host), () -> open[0]
                && world.getGameTime() < expires && GlyphCatalog.CORRUPTED_BEAM.isEnabled()
                && GoetyChannelCasting.eligible(world, frame.caster(), host)
                && (charge.amount() <= 0 || charge.account().free() || charge.account().canSpend(1)));
        boolean[] spawned = {false};
        return GoetyChannelCasting.start(focus, world, frame, host, spell, stats, charge,
                new GoetyChannelCasting.Program(ticks, 1, () -> {
                    if (!spawned[0]) {
                        spawned[0] = world.addFreshEntity(beam);
                        // The first paid activation must resolve damage even if its fee empties the account.
                        if (spawned[0]) beam.tick();
                    }
                    return spawned[0] && !beam.isRemoved();
                }, () -> {}, spell.CastingSound(frame.caster()), spell.loopSound(frame.caster()),
                        () -> !beam.isRemoved(), () -> {open[0] = false; beam.discard();}));
    }
}
