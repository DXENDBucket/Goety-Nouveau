package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.utility.IlluminateSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native periodic placement in an allied-executor session; payment commits only real light changes. */
public final class GoetyIlluminateCasting {
    private GoetyIlluminateCasting() {}
    private static boolean funded(ResourceCharge charge){return charge.amount()==0 || charge.account().free() || charge.account().canSpend(charge.amount());}
    public static boolean start(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge){
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host) || !funded(charge))return false;
        var spell=(IlluminateSpell)EffectFocus.ILLUMINATE.spell();var staff=GoetyResources.staff(frame.caster()).copy();
        var stats=WandUtil.getStats(frame.caster(),spell);stats.setRadius(Math.max(0,stats.getRadius()+ars.getAoeMultiplier()));
        int ticks=GoetyChannelCasting.boundedTicks(0,ars.getDurationMultiplier());if(ticks<=0)return false;
        int interval=SpellConfig.IlluminateDuration.get(),warmup=Math.max(0,SpellConfig.IlluminateChargeUp.get());
        // Native useSpell divides by this period. A zero config must fail before entering it.
        if(interval<=0)return false;
        int first=(int)Math.min(Integer.MAX_VALUE,((long)warmup+interval-1)/interval*interval);
        boolean[] changed={false},worked={false};
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,ignored->changed[0]=true,stats);
        var driver=new ExecutionDriver<SpellStat>(){
            private long next,last;private int phase=first;
            public boolean occupiesCaster(){return true;}
            private void attempt(ExecutionSession<SpellStat> session){
                session.activate(()->GoetyNativeGeometry.scoped(view,()->{
                    changed[0]=false;spell.useSpell(world,frame.caster(),staff,phase,stats);
                    phase=phase>Integer.MAX_VALUE-interval?first:phase+interval;
                    worked[0]|=changed[0];return changed[0];
                }));
            }
            private void visual(){GoetyNativeGeometry.scoped(view,()->{spell.useParticle(world,host,staff);return null;});}
            public boolean start(ExecutionSession<SpellStat> session){
                attempt(session);last=world.getGameTime();next=last+interval;visual();
                GoetyChannelCasting.movement(session,host,ticks,ticks);return true;
            }
            public boolean tick(ExecutionSession<SpellStat> session){
                if(!GoetyChannelCasting.eligible(world,frame.caster(),host) || !funded(charge))return true;
                long now=world.getGameTime();if(now==last)return false;
                if(now>=next){attempt(session);next=now+interval;}
                visual();if(now%20==0)GoetyChannelCasting.movement(session,host,ticks,session.remainingTicks());last=now;return false;
            }
            public void close(ExecutionSession<SpellStat> session,ExecutionSession.EndReason reason){
                GoetyChannelCasting.movement(session,host,ticks,0);
                if(worked[0])GoetyFocusCooldowns.atLeast(frame.caster(),net.minecraft.core.registries.BuiltInRegistries.ITEM.get(EffectFocus.ILLUMINATE.focusId()),spell.spellCooldown(frame.caster()));
            }
        };
        var session=new ExecutionSession<>(world,frame.withExecutor(host),stats,ticks,driver,ignored->{},
                ()->GlyphCatalog.ILLUMINATE.isEnabled() && GoetyChannelCasting.eligible(world,frame.caster(),host),ExecutionScope.DIRECT);
        session.billing(action->GoetyResources.activation(charge).trigger(action),true);return ExecutionSessions.start(session);
    }
}
