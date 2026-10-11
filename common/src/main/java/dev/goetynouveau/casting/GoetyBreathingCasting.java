package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.BreathingSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
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
                || !eligible(world, frame.caster(), host)) return false;
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
        var driver = new ExecutionDriver<SpellStat>() {
            private long nextPayment;
            private long nextSound;
            private long lastTick = Long.MIN_VALUE;
            private void movement(ExecutionSession<SpellStat> session, int remaining) {
                if (host instanceof net.minecraft.server.level.ServerPlayer player && !player.hasDisconnected()
                        && dev.goetynouveau.platform.ActiveCastingPlatform.realPlayer(host))
                    dev.goetynouveau.platform.ActiveCastingPlatform.chant(player,
                            new dev.goetynouveau.network.ChantStatePayload(session.id(), ticks, remaining, true));
            }
            private void pulse() {
                GoetyBreathGeometry.scoped(view, () -> {
                    dev.goetynouveau.platform.NativeBreathHooks.result(spell, world, frame.caster(), staff, stats);
                    return null;
                });
            }
            private void visual() {
                GoetyBreathGeometry.scoped(view, () -> {
                    spell.showWandBreath(frame.caster(), staff, stats);
                    return null;
                });
            }
            private void sound(net.minecraft.sounds.SoundEvent sound) {
                if (sound != null) spell.playSound(world, host, sound, spell.castingVolume(), spell.castingPitch());
            }
            public boolean occupiesCaster() { return true; }
            public boolean start(ExecutionSession<SpellStat> session) {
                pulse(); visual(); lastTick = world.getGameTime(); nextPayment = lastTick + 20;
                sound(spell.CastingSound(frame.caster())); nextSound = lastTick + 20;
                movement(session, session.remainingTicks());
                return true; // The enclosing glyph transaction pays this first emission.
            }
            public boolean tick(ExecutionSession<SpellStat> session) {
                if (!eligible(world, frame.caster(), host)
                        || charge.amount() > 0 && !charge.account().free() && !charge.account().canSpend(1)) return true;
                long now = world.getGameTime();
                if (now == lastTick) return false;
                if (now >= nextPayment) {
                    if (!session.activate(() -> { pulse(); return true; })) return true;
                    nextPayment = now + 20;
                } else pulse();
                if (now >= nextSound) { sound(spell.loopSound(frame.caster())); nextSound = now + 20; }
                if (now % 20 == 0) movement(session, session.remainingTicks());
                visual(); lastTick = now; return false;
            }
            public void close(ExecutionSession<SpellStat> session, ExecutionSession.EndReason reason) { movement(session, 0); }
        };
        var session = new ExecutionSession<>(world, selected, stats, ticks, driver, ignored -> {},
                () -> eligible(world, frame.caster(), host) && dev.goetynouveau.GlyphCatalog.ALL.stream()
                        .filter(p -> p instanceof dev.goetynouveau.glyph.GoetyEffectGlyph glyph && glyph.focus() == focus)
                        .allMatch(com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart::isEnabled), ExecutionScope.DIRECT);
        session.billing(action -> GoetyResources.activation(charge).trigger(action), true);
        return ExecutionSessions.start(session);
    }
    private static boolean eligible(ServerLevel world, LivingEntity caster, LivingEntity host) {
        return host.isAlive() && !host.isRemoved() && !host.isSpectator() && host.level() == world
                && world.hasChunkAt(host.blockPosition()) && GoetyBreathGeometry.allied(caster, host);
    }
}
