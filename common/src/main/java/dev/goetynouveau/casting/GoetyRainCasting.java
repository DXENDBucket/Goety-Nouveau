package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.IChargingSpell;
import com.Polarice3.Goety.common.magic.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.glyph.AugmentBurning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native rain results retain arrows, snow clouds, meteor variants and their native impact logic. */
public final class GoetyRainCasting {
    private GoetyRainCasting() {}
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host)) return false;
        var spell=(Spell)focus.spell();var charging=(IChargingSpell)spell;
        var staff=GoetyResources.staff(frame.caster()).copy();
        double count=charging.shotsNumber(frame.caster(),staff)*Math.max(0,1+ars.getDurationMultiplier());
        if(!Double.isFinite(count) || count<=0) return false;
        int shots=(int)Math.min(Integer.MAX_VALUE,Math.ceil(count));
        int interval=Math.max(1,charging.Cooldown());
        var stats=com.Polarice3.Goety.utils.WandUtil.getStats(frame.caster(),spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        stats.setRadius(Math.max(0,stats.getRadius()+(float)ars.getAoeMultiplier()));
        stats.setBurning(stats.getBurning()+ars.getBuffCount(AugmentBurning.INSTANCE));
        var view=new GoetyRainGeometry.View(frame.withExecutor(host),host,spell);
        Runnable pulse=()->GoetyRainGeometry.scoped(view,()->{
            dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);return null;
        });
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(shots,interval,pulse,()->{},null,null));
    }
}
