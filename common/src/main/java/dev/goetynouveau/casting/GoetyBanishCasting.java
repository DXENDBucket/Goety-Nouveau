package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.void_spells.BanishSpell;
import com.Polarice3.Goety.utils.*;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native conditions and random-teleport callback retain immunity, events, landing and sounds. */
public final class GoetyBanishCasting {
    private GoetyBanishCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                || !target.isAlive() || target.isRemoved() || target.isSpectator() || target.level()!=world
                || target==frame.caster() || dev.arsconflux.api.targeting.Allies.areAllies(frame.caster(),target)
                || !world.isInWorldBounds(target.blockPosition()) || !world.hasChunkAt(target.blockPosition()))return false;
        if(target instanceof net.minecraft.world.entity.player.Player player && player.isCreative())return false;
        var source=frame.caster();var spell=new BanishSpell();var staff=GoetyResources.staff(source).copy();
        var stats=WandUtil.getStats(source,spell);
        long range=(long)stats.getRange()+(WandUtil.enchantedFocus(source)?WandUtil.getRangeLevel(source):0);
        if(spell.rightStaff(staff))range*=2;
        if(range<=0 || range>Integer.MAX_VALUE)return false;
        boolean[] moved={false};
        var view=new GoetyNativeGeometry.View(frame,source,spell,ignored->moved[0]=true,stats);
        GoetyNativeGeometry.scoped(view,()->{
            if(spell.conditionsMet(world,source))spell.touchResult(world,source,target,staff,stats);
            return null;
        });
        return moved[0];
    }
}
