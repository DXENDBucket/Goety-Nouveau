package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.BreathingSpell;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.glyph.AugmentBurning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native breath result and visuals, with Conflux lifetime and native per-tick results and per-second transactions. */
public final class GoetyBreathingCasting {
    private GoetyBreathingCasting() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame,
                                SpellStats ars, ResourceCharge charge) {
        if (!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world, frame.caster(), host)) return false;
        var spell = (BreathingSpell)focus.spell();
        var staff = GoetyResources.staff(frame.caster()).copy();
        double multiplier = Math.max(0, 1 + ars.getDurationMultiplier());
        double lifetime = spell.shotsNumber(frame.caster(), staff) * multiplier;
        if (!Double.isFinite(lifetime) || lifetime <= 0) return false;
        // Native shotsNumber is the ever-charge duration in ticks, not a count of summons.
        int ticks = (int)Math.min(Integer.MAX_VALUE, Math.ceil(lifetime));
        var stats = com.Polarice3.Goety.utils.WandUtil.getStats(frame.caster(), spell);
        stats.setPotency(Math.max(0, stats.getPotency() + (int)ars.getAmpMultiplier()));
        stats.setDuration(Math.max(0, stats.getDuration() + (int)ars.getDurationMultiplier()));
        stats.setBurning(stats.getBurning() + ars.getBuffCount(AugmentBurning.INSTANCE));
        var selected = frame.withExecutor(host);
        var view = new GoetyBreathGeometry.View(selected, host);
        Runnable pulse=()->GoetyBreathGeometry.scoped(view,()->{
            dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);return null;
        });
        Runnable visual=()->GoetyBreathGeometry.scoped(view,()->{
            spell.showWandBreath(frame.caster(),staff,stats);return null;
        });
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,pulse,visual,spell.CastingSound(frame.caster()),spell.loopSound(frame.caster())));
    }
}
