package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.void_spells.EndWalkSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Native effect and native return-anchor events own the complete walk lifecycle. */
public final class GoetyEndWalkCasting {
    private GoetyEndWalkCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host)
                || NativeEffectHooks.shadowWalk(host)!=null || SEHelper.hasEndWalk(host))return false;
        var source=frame.caster();var spell=new EndWalkSpell();var staff=GoetyResources.staff(source).copy();
        var stats=WandUtil.getStats(source,spell);
        long duration=stats.getDuration(),potency=stats.getPotency();
        // Native Duration enchantment replaces the default stat; Potency adds to it.
        if(WandUtil.enchantedFocus(source)) {
            duration=100L*NativeEffectHooks.duration(source);
            potency+=WandUtil.getPotencyLevel(source);
        }
        double extra=ars.getDurationMultiplier()*20;
        if(!Double.isFinite(extra))return false;
        double total=SpellConfig.EndWalkEffectDuration.get()+duration+extra;
        if(total<=0 || total>Integer.MAX_VALUE || total<Integer.MIN_VALUE)return false;
        stats.setDuration((int)total-SpellConfig.EndWalkEffectDuration.get());
        stats.setPotency((int)Math.max(0,Math.min(Integer.MAX_VALUE,potency+(int)ars.getAmpMultiplier())));
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,null,stats);
        GoetyNativeGeometry.scoped(view,()->{spell.SpellResult(world,source,staff,stats);return null;});
        if(NativeEffectHooks.shadowWalk(host)==null) {
            // Native callback writes the anchor even if another mod rejects the effect.
            SEHelper.removeEndWalk(host);return false;
        }
        return SEHelper.hasEndWalk(host);
    }
}
