package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;

/** Native startSpell/WandUtil layouts, followed by native trident animation, strikes and expiry. */
public final class GoetyTridentCasting {
    private GoetyTridentCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame,Spell spell,SpellStats ars) {
        var point=frame.target() instanceof EntityHitResult e?e.getEntity().position():frame.target().getLocation();
        if(!Double.isFinite(point.lengthSqr()) || world.isOutsideBuildHeight(BlockPos.containing(point))) return false;
        for(int x:new int[]{-9,9}) for(int z:new int[]{-9,9})
            if(!world.hasChunkAt(BlockPos.containing(point).offset(x,0,z))) return false;
        var stats=com.Polarice3.Goety.utils.WandUtil.getStats(frame.caster(),spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        var view=new GoetyNativeGeometry.View(frame,frame.caster(),spell);
        return GoetyNativeGeometry.scoped(view,()->{
            spell.startSpell(world,frame.caster(),GoetyResources.staff(frame.caster()),stats);
            spell.playSound(world,frame.caster(),1,1);return true;
        });
    }
}
