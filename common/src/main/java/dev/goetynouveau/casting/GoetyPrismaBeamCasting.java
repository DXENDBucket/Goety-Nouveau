package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** A native target-dependent charge, ending in the original pair of damage calls and one payment. */
public final class GoetyPrismaBeamCasting {
    private GoetyPrismaBeamCasting() {}
    public static boolean start(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var spell=(Spell)EffectFocus.PRISMA_BEAM.spell();var staff=GoetyResources.staff(frame.caster()).copy();
        int nativeTicks=Math.max(1,spell.castDuration(frame.caster(),staff));
        int ticks=GoetyChannelCasting.boundedTicks(nativeTicks,ars.getDurationMultiplier());
        if(ticks<=0)return false;
        var stats=WandUtil.getStats(frame.caster(),spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        var display=GoetyEntities.CHANNEL_BEAM.get().create(world);
        boolean[] open={true};long began=world.getGameTime();
        display.bind(host,GoetyBeamDisplay.PRISMA,ticks,.4f,()->open[0] && world.getGameTime()<=began+ticks
                && GlyphCatalog.PRISMA_BEAM.isEnabled() && GoetyChannelCasting.eligible(world,frame.caster(),host)
                && (charge.account().free() || charge.account().canSpend(charge.amount())));
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,display::observe);
        java.util.function.BooleanSupplier target=()->GoetyNativeGeometry.scoped(view,()->spell.getTarget(frame.caster())!=null);
        if(!target.getAsBoolean())return false;
        var driver=new ExecutionDriver<com.Polarice3.Goety.common.magic.SpellStat>() {
            private long last=Long.MIN_VALUE;
            public boolean occupiesCaster(){return true;}
            public boolean start(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session) {
                if(!world.addFreshEntity(display))return false;
                use(0);last=world.getGameTime();GoetyChannelCasting.movement(session,host,ticks,ticks);return true;
            }
            private void use(int phase) {
                GoetyNativeGeometry.scoped(view,()->{spell.useSpell(world,frame.caster(),staff,phase,stats);return null;});
            }
            public boolean tick(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session) {
                if(!GoetyChannelCasting.eligible(world,frame.caster(),host) || display.isRemoved() || !target.getAsBoolean() || !charge.account().free() && !charge.account().canSpend(charge.amount()))return true;
                long now=world.getGameTime();if(now==last)return false;last=now;
                long age=now-began;
                // Extended lifetime must not drive native bubble spacing beyond its safe 0..1 charge ratio.
                use((int)Math.min(nativeTicks,age*(double)nativeTicks/Math.max(1,ticks-1)));
                if(age>=ticks-1) {
                    session.activate(()->GoetyNativeGeometry.scoped(view,()->{
                        if(spell.getTarget(frame.caster())==null)return false;
                        dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);return true;
                    }));
                    return true;
                }
                if(now%20==0)GoetyChannelCasting.movement(session,host,ticks,session.remainingTicks());
                return false;
            }
            public void close(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session,ExecutionSession.EndReason reason) {
                open[0]=false;display.discard();GoetyChannelCasting.movement(session,host,ticks,0);
            }
        };
        var session=new ExecutionSession<>(world,frame.withExecutor(host),stats,Math.max(2,ticks),driver,ignored->{},
                ()->GlyphCatalog.PRISMA_BEAM.isEnabled() && GoetyChannelCasting.eligible(world,frame.caster(),host),ExecutionScope.DIRECT);
        session.billing(action->GoetyResources.activation(charge).trigger(action),true);
        return ExecutionSessions.start(session);
    }
}
