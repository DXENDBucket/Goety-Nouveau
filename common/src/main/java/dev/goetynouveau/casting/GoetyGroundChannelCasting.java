package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Original native callbacks in a bounded allied-executor session. */
public final class GoetyGroundChannelCasting {
    private GoetyGroundChannelCasting() {}
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        if(focus==EffectFocus.WARDING && !(host instanceof Player))return false;
        var spell=(Spell)focus.spell();var caster=frame.caster();var staff=GoetyResources.staff(caster).copy();
        var stats=WandUtil.getStats(caster,spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        stats.setRadius(Math.max(0,stats.getRadius()+ars.getAoeMultiplier()));
        int first,ticks;
        if(focus==EffectFocus.VOID_SHOCK) {
            first=10;
            ticks=GoetyChannelCasting.boundedTicks(Math.max(0,spell.castDuration(caster,staff)-first),ars.getDurationMultiplier());
        } else if(focus==EffectFocus.QUAKING) {
            // Quaking's native main attack exists only during useSpell, after its warmup.
            if(WandUtil.enchantedFocus(caster)) {
                stats.setPotency(stats.getPotency()+WandUtil.getPotencyLevel(caster));
                stats.setRange(stats.getRange()+WandUtil.getRangeLevel(caster));
                stats.setRadius(stats.getRadius()+NativeEffectHooks.radius(caster));
            }
            first=spell.defaultCastDuration()+1;
            // Native sneaking uses expanding rings; ordinary casts use forward range.
            ticks=GoetyResources.sneaking(caster)?(int)stats.getRadius()+(spell.rightStaff(staff)?1:0):stats.getRange();
            ticks=Math.max(0,ticks);
        } else if(focus==EffectFocus.WARDING) {
            if(WandUtil.enchantedFocus(caster))stats.setPotency(stats.getPotency()+WandUtil.getPotencyLevel(caster));
            first=0;
            ticks=GoetyChannelCasting.boundedTicks(((com.Polarice3.Goety.api.magic.IChargingSpell)spell).shotsNumber(caster,staff),ars.getDurationMultiplier());
        } else return false;
        if(ticks<=0)return false;
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell);int[] phase={first};
        Runnable pulse=()->GoetyNativeGeometry.scoped(view,()->{
            if(focus==EffectFocus.WARDING)spell.SpellResult(world,caster,staff,stats);
            else dev.goetynouveau.platform.NativeBreathHooks.run(spell,()->spell.useSpell(world,caster,staff,phase[0]++,stats));
            return null;
        });
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,pulse,()->{},spell.CastingSound(caster),spell.loopSound(caster)));
    }
}
