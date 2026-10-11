package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.void_spells.TunnelSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;

/** The native temporary-hole lifecycle owns expansion, particles and restoration. */
public final class GoetyTunnelCasting {
    private GoetyTunnelCasting() {}
    public static boolean apply(ServerLevel world, CastContext frame, SpellStats ars) {
        if (!(frame.target() instanceof BlockHitResult hit)
                || !GoetyTunnelHoles.loaded(world,hit.getBlockPos())
                || !GoetyTunnelHoles.loaded(world,hit.getBlockPos().relative(hit.getDirection()))) return false;
        var caster=frame.caster();
        var spell=(TunnelSpell)EffectFocus.TUNNEL.spell();
        var staff=GoetyResources.staff(caster).copy();
        var stats=WandUtil.getStats(caster,spell);
        stats.setDuration(stats.getDuration()+(int)ars.getDurationMultiplier());
        long duration=(long)SpellConfig.TunnelDefaultLifespan.get()+20L*(stats.getDuration()
                +(WandUtil.enchantedFocus(caster)?NativeEffectHooks.duration(caster):0));
        if (duration<=0 || duration>Integer.MAX_VALUE
                || !spell.rightBlock(world,caster,hit.getBlockPos(),hit.getDirection(),stats)) return false;
        boolean[] created={false};
        var view=new GoetyNativeGeometry.View(frame,caster,spell,ignored->created[0]=true,stats);
        GoetyNativeGeometry.scoped(view,()->{
            spell.blockResult(world,caster,staff,hit.getBlockPos(),hit.getDirection(),stats);
            return null;
        });
        return created[0];
    }
}
