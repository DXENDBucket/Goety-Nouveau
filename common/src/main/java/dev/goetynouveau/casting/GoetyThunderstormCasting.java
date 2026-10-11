package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native useSpell owns the delayed lightning traps, staff variants and startup particles. */
public final class GoetyThunderstormCasting {
    private GoetyThunderstormCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host)) return false;
        var focus=EffectFocus.THUNDERSTORM;var spell=(Spell)focus.spell();
        var staff=GoetyResources.staff(frame.caster()).copy();
        int base=spell.castDuration(frame.caster(),staff);
        // The Ars preparation phase precedes the effect; native emission starts at tick 20.
        double duration=Math.max(0,base-20)*Math.max(0,1+ars.getDurationMultiplier());
        if(!Double.isFinite(duration) || duration<=0) return false;
        int ticks=(int)Math.min(Integer.MAX_VALUE,Math.ceil(duration));
        var stats=com.Polarice3.Goety.utils.WandUtil.getStats(frame.caster(),spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        stats.setRadius(Math.max(0,stats.getRadius()+(float)ars.getAoeMultiplier()));
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell);int[] phase={20};
        Runnable pulse=()->GoetyNativeGeometry.scoped(view,()->{
            spell.useSpell(world,frame.caster(),staff,phase[0]++,stats);return null;
        });
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,pulse,()->GoetyNativeGeometry.scoped(view,()->{
                    spell.useParticle(world,frame.caster(),staff);return null;
                }),spell.CastingSound(frame.caster()),null));
    }
}
