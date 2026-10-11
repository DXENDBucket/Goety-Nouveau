package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.SpellStat;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.sounds.SoundEvent;
import java.util.function.BooleanSupplier;

/** Shared bounded channel lifecycle. Native callbacks, geometry and cadence belong to adapters. */
public final class GoetyChannelCasting {
    public record Program(int shots,int interval,BooleanSupplier pulse,Runnable visual,
                          SoundEvent initial,SoundEvent loop,BooleanSupplier running,Runnable close) {
        public Program(int shots,int interval,Runnable pulse,Runnable visual,SoundEvent initial,SoundEvent loop) {
            this(shots,interval,()->{pulse.run();return true;},visual,initial,loop,()->true,()->{});
        }
    }
    private GoetyChannelCasting() {}
    /** An infinite native held cast needs a finite duration when activated by one Ars hit. */
    public static int boundedTicks(int nativeTicks,double durationModifier) {
        double base=nativeTicks>0?nativeTicks:dev.goetynouveau.platform.GoetyConfig.unboundedChannelTicks();
        double ticks=base*Math.max(0,1+durationModifier);
        return !Double.isFinite(ticks) || ticks<=0?0:(int)Math.min(Integer.MAX_VALUE,Math.ceil(ticks));
    }
    public static boolean eligible(ServerLevel world,LivingEntity caster,LivingEntity host) {
        return caster!=null && caster.isAlive() && !caster.isRemoved() && !caster.isSpectator() && caster.level()==world
                && host.isAlive() && !host.isRemoved() && !host.isSpectator() && host.level()==world
                && world.hasChunkAt(host.blockPosition()) && GoetyBreathGeometry.allied(caster,host);
    }
    public static boolean start(EffectFocus focus,ServerLevel world,CastContext frame,LivingEntity host,
                                Spell spell,SpellStat stats,ResourceCharge charge,Program program) {
        if(program.shots()<=0 || program.interval()<=0 || !eligible(world,frame.caster(),host)) return false;
        long lifetime=1L+(long)(program.shots()-1)*program.interval();
        int ticks=(int)Math.min(Integer.MAX_VALUE,lifetime);
        var selected=frame.withExecutor(host);
        var driver=new ExecutionDriver<SpellStat>() {
            private int emitted;
            private long nextEmission,nextSound,lastTick=Long.MIN_VALUE;
            private void movement(ExecutionSession<SpellStat> session,int remaining) {
                if(host instanceof net.minecraft.server.level.ServerPlayer player && !player.hasDisconnected()
                        && dev.goetynouveau.platform.ActiveCastingPlatform.realPlayer(host))
                    dev.goetynouveau.platform.ActiveCastingPlatform.chant(player,
                            new dev.goetynouveau.network.ChantStatePayload(session.id(),ticks,remaining,true));
            }
            private void sound(SoundEvent sound) {
                if(sound!=null) spell.playSound(world,host,sound,spell.castingVolume(),spell.castingPitch());
            }
            public boolean occupiesCaster() { return true; }
            public boolean start(ExecutionSession<SpellStat> session) {
                if(!program.pulse().getAsBoolean()) return false;
                emitted=1; program.visual().run();
                lastTick=world.getGameTime();nextEmission=lastTick+program.interval();nextSound=lastTick+20;
                sound(program.initial());movement(session,session.remainingTicks());
                return true; // The enclosing glyph transaction pays the first native emission.
            }
            public boolean tick(ExecutionSession<SpellStat> session) {
                if(!eligible(world,frame.caster(),host) || !program.running().getAsBoolean()
                        || charge.amount()>0 && !charge.account().free() && !charge.account().canSpend(1)) return true;
                long now=world.getGameTime();if(now==lastTick) return false;
                if(now>=nextEmission && emitted<program.shots()) {
                    // Native everCharge bills in groups of 20 results, not 20 world ticks.
                    // Prepay the next group at its first emission so an empty account cannot cast.
                    if(emitted%20==0) {
                        if(!session.activate(program.pulse())) return true;
                    } else if(!program.pulse().getAsBoolean()) return true;
                    ++emitted;nextEmission=now+program.interval();
                }
                if(now>=nextSound) {sound(program.loop());nextSound=now+20;}
                if(now%20==0) movement(session,session.remainingTicks());
                program.visual().run();lastTick=now;return false;
            }
            public void close(ExecutionSession<SpellStat> session,ExecutionSession.EndReason reason) {
                try {program.close().run();} finally {movement(session,0);}
            }
        };
        var session=new ExecutionSession<>(world,selected,stats,ticks,driver,ignored->{},
                ()->eligible(world,frame.caster(),host) && dev.goetynouveau.GlyphCatalog.ALL.stream()
                        .filter(part->part instanceof dev.goetynouveau.glyph.GoetyEffectGlyph glyph && glyph.focus()==focus)
                        .allMatch(com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart::isEnabled),ExecutionScope.DIRECT);
        session.billing(action->GoetyResources.activation(charge).trigger(action),true);
        return ExecutionSessions.start(session);
    }
}
