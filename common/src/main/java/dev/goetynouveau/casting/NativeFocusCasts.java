package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.*;
import com.Polarice3.Goety.common.events.spell.GoetyEventFactory;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import dev.goetynouveau.platform.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundSource;
import java.util.*;

/** Opaque native lifecycle, always at the real player. It never claims native projectile impacts. */
public final class NativeFocusCasts {
    private record Key(UUID caster,net.minecraft.resources.ResourceLocation focus){}
    private static final Set<Key> RUNNING=new HashSet<>();
    private NativeFocusCasts(){}
    public static ResourceCharge quote(NativeFocusGlyph glyph,SpellContext context,List<AbstractAugment> augments){
        var caster=CastContext.of(context,null,null).caster();if(caster==null)return null;
        var state=new NativeFocusScope.State(glyph,caster);
        return NativeFocusScope.scoped(state,()->GoetyResources.quote(context,state.spell,augments));
    }
    public static boolean start(NativeFocusGlyph glyph,CastContext frame,SpellStats ars){
        if(!(frame.caster() instanceof ServerPlayer player)||!ActiveCastingPlatform.realPlayer(player)||!glyph.isEnabled()
                ||!(player.level() instanceof ServerLevel world)||frame.ars()==null||dev.arsconflux.api.glyph.ArsSpellAccess.level(frame.ars())!=world)return false;
        var key=new Key(player.getUUID(),glyph.focusId());if(RUNNING.contains(key))return false;
        if(GoetyFocusCooldowns.enabled(player)&&com.Polarice3.Goety.utils.SEHelper.isOnCooldown(player,glyph.source()))return false;
        var state=new NativeFocusScope.State(glyph,player);if(state.wand.isEmpty())return false;
        return NativeFocusScope.geometry(state,frame,()->{
            state.stats=WandUtil.getStats(player,state.spell);
            state.stats.increasePotency(Math.max(-state.stats.getPotency(),(int)ars.getAmpMultiplier()));
            if(!state.spell.conditionsMet(world,player))return false;
            state.spell=GoetyEventFactory.onStartSpell(player,state.wand,state.spell);if(state.spell==null)return false;
            var charge=GoetyResources.quote(frame.ars(),state.spell,ars.getAugments());
            var charging=state.spell instanceof IChargingSpell c?c:null;
            int nativeWarmup=Math.max(0,charging==null?state.spell.castDuration(player,state.wand):charging.castUp(player,state.wand));
            int warmup=GoetyConfig.chantingEnabled()&&dev.arsconflux.api.casting.ActiveCastPolicy.of(frame.ars()).chanting()
                    ?dev.arsconflux.api.casting.CastingTiming.chantTicks(player,nativeWarmup):0,offset=nativeWarmup-warmup;
            int lifetime=charging==null?warmup+2:Math.min(72000,warmup+GoetyConfig.nativeChannelTicks());
            NativeFocusPlatform.state(state.wand,lifetime+offset,0);
            var driver=new ExecutionDriver<SpellStat>(){
                int elapsed,emitted,nextResult=warmup;
                boolean started,stopped;
                private void hud(ExecutionSession<SpellStat> session,int remaining){
                    if(player.hasDisconnected())return;
                    int preparation=remaining>0?Math.max(0,warmup-elapsed):0;
                    ActiveCastingPlatform.chant(player,new dev.goetynouveau.network.ChantStatePayload(session.id(),warmup,preparation,false));
                    ActiveCastingPlatform.chant(player,new dev.goetynouveau.network.ChantStatePayload(session.id(),lifetime,preparation>0?0:remaining,true));
                }
                private boolean callback(java.util.function.BooleanSupplier action){return NativeFocusScope.geometry(state,frame,()->action.getAsBoolean());}
                private void sound(net.minecraft.sounds.SoundEvent sound){if(sound!=null)world.playSound(null,player.blockPosition(),sound,SoundSource.PLAYERS,state.spell.castingVolume(),state.spell.castingPitch());}
                public boolean occupiesCaster(){return true;}
                public boolean start(ExecutionSession<SpellStat> session){
                    RUNNING.add(key);started=true;
                    callback(()->{state.spell.startSpell(world,player,state.wand,NativeFocusScope.stats(state));sound(state.spell.CastingSound(player,state.wand));return true;});
                    hud(session,lifetime);return true;
                }
                private boolean result(){
                    var chosen=GoetyEventFactory.onCastSpell(player,state.spell);if(chosen==null)return false;
                    NativeFocusPlatform.damage(chosen,()->chosen.SpellResult(world,player,state.wand,NativeFocusScope.stats(state)));
                    ++emitted;state.shots=emitted;NativeFocusPlatform.state(state.wand,lifetime+offset,emitted);
                    if(charging==null)cooldown();
                    return true;
                }
                public boolean tick(ExecutionSession<SpellStat> session){return callback(()->{
                    ++elapsed;
                    if(!state.spell.conditionsMet(world,player))return true;
                    if(elapsed==warmup)hud(session,session.remainingTicks());
                    var selected=GoetyEventFactory.onCastingSpell(player,state.wand,state.spell,elapsed+offset);
                    if(selected==null)return true;
                    NativeFocusPlatform.damage(selected,()->selected.useSpell(world,player,state.wand,elapsed+offset,NativeFocusScope.stats(state)));
                    selected.useParticle(world,player,state.wand);
                    if(elapsed%20==0){sound(selected.loopSound(player));hud(session,session.remainingTicks());}
                    if(elapsed>=nextResult){
                        boolean pay=emitted>0&&(charging==null||!charging.everCharge()||emitted%20==0);
                        boolean success=pay?GoetyResources.activation(charge).trigger(this::result):result();
                        if(!success)return true;
                        if(charging==null)return true;
                        int shots=charging.shotsNumber(player,state.wand);if(shots>0&&emitted>=shots)return true;
                        nextResult=elapsed+Math.max(1,charging.Cooldown(player,state.wand,emitted));
                    }
                    return false;
                });}
                private void cooldown(){
                    if(!GoetyFocusCooldowns.enabled(player))return;
                    int ticks=Math.max(0,state.spell.spellCooldown(player));
                    if(charging!=null&&charging.shotsNumber(player,state.wand)>0)ticks=(int)(ticks*Math.min(1D,(double)emitted/charging.shotsNumber(player,state.wand)));
                    if(!state.spell.hasCustomCooldown(player,state.wand,state.focus,ticks))GoetyFocusCooldowns.atLeast(player,glyph.source().getItem(),ticks);
                }
                public void close(ExecutionSession<SpellStat> session,ExecutionSession.EndReason reason){
                    try{if(started&&!stopped){stopped=true;callback(()->{
                        var stoppedSpell=GoetyEventFactory.onStopSpell(player,state.wand,state.spell,elapsed+offset,Math.max(0,lifetime-elapsed));
                        if(stoppedSpell!=null)stoppedSpell.stopSpell(world,player,state.wand,state.focus,elapsed+offset,NativeFocusScope.stats(state));
                        if(charging!=null&&emitted>0)cooldown();return true;
                    });}}finally{RUNNING.remove(key);hud(session,0);}
                }
            };
            var session=new ExecutionSession<>(world,frame.withExecutor(player).withAccount(charge.account()),state.stats,lifetime,driver,ignored->{},glyph::isEnabled,ExecutionScope.DIRECT);
            session.billing(action->GoetyResources.activation(charge).trigger(action),false);
            return ExecutionSessions.start(session);
        });
    }
}
