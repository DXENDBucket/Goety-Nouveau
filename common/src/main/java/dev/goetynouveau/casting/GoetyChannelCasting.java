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

/** Shared bounded channel lifecycle. Native callbacks, geometry and cadence belong to adapters. */
public final class GoetyChannelCasting {
    public record Program(int shots,int interval,Runnable pulse,Runnable visual,SoundEvent initial,SoundEvent loop) {}
    private GoetyChannelCasting() {}
    public static boolean eligible(ServerLevel world,LivingEntity caster,LivingEntity host) {
        return host.isAlive() && !host.isRemoved() && !host.isSpectator() && host.level()==world
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
                program.pulse().run(); emitted=1; program.visual().run();
                lastTick=world.getGameTime();nextEmission=lastTick+program.interval();nextSound=lastTick+20;
                sound(program.initial());movement(session,session.remainingTicks());
                return true; // The enclosing glyph transaction pays the first native emission.
            }
            public boolean tick(ExecutionSession<SpellStat> session) {
                if(!eligible(world,frame.caster(),host)
                        || charge.amount()>0 && !charge.account().free() && !charge.account().canSpend(1)) return true;
                long now=world.getGameTime();if(now==lastTick) return false;
                if(now>=nextEmission && emitted<program.shots()) {
                    // Native everCharge bills in groups of 20 results, not 20 world ticks.
                    // Prepay the next group at its first emission so an empty account cannot cast.
                    if(emitted%20==0) {
                        if(!session.activate(()->{program.pulse().run();return true;})) return true;
                    } else program.pulse().run();
                    ++emitted;nextEmission=now+program.interval();
                }
                if(now>=nextSound) {sound(program.loop());nextSound=now+20;}
                if(now%20==0) movement(session,session.remainingTicks());
                program.visual().run();lastTick=now;return false;
            }
            public void close(ExecutionSession<SpellStat> session,ExecutionSession.EndReason reason) {movement(session,0);}
        };
        var session=new ExecutionSession<>(world,selected,stats,ticks,driver,ignored->{},
                ()->eligible(world,frame.caster(),host) && dev.goetynouveau.GlyphCatalog.ALL.stream()
                        .filter(part->part instanceof dev.goetynouveau.glyph.GoetyEffectGlyph glyph && glyph.focus()==focus)
                        .allMatch(com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart::isEnabled),ExecutionScope.DIRECT);
        session.billing(action->GoetyResources.activation(charge).trigger(action),true);
        return ExecutionSessions.start(session);
    }
}
