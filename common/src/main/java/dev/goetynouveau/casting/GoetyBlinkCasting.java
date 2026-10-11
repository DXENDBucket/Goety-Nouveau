package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.void_spells.BlinkSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.LivingEntity;

/** The native three-way blink executes from an allied body, with original-source gear. */
public final class GoetyBlinkCasting {
    private GoetyBlinkCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host)
                || !world.isInWorldBounds(host.blockPosition()))return false;
        var source=frame.caster();var spell=new BlinkSpell();var staff=GoetyResources.staff(source).copy();
        var stats=WandUtil.getStats(source,spell);
        long range=(long)stats.getRange()+(WandUtil.enchantedFocus(source)?WandUtil.getRangeLevel(source):0);
        long effective=spell.rightStaff(staff)?range*2:range;
        // Native forward range is 32+range; random range is 64+2*range. Guard integer arithmetic.
        if(range<=0 || effective>(Integer.MAX_VALUE-64L)/2)return false;
        stats.setRange((int)range);
        boolean[] moved={false};
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,ignored->moved[0]=true,stats);
        GoetyNativeGeometry.scoped(view,()->{spell.SpellResult(world,source,staff,stats);return null;});
        return moved[0];
    }
    /** Only this addon's synchronous native Blink callback observes teleports. */
    public static boolean managed(LivingEntity actor) {
        var view=GoetyNativeGeometry.current();return view!=null && view.spell() instanceof BlinkSpell && view.host()==actor;
    }
    public static boolean destination(LivingEntity actor,double x,double y,double z) {
        if(!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))return false;
        var pos=net.minecraft.core.BlockPos.containing(x,y,z);
        return actor.level().isInWorldBounds(pos) && actor.level().hasChunkAt(pos);
    }
    public static void moved(LivingEntity actor) {
        if(managed(actor)){var view=GoetyNativeGeometry.current();GoetyNativeGeometry.observe(view.spell(),view.context().caster(),new EntityHitResult(actor));}
    }
}
