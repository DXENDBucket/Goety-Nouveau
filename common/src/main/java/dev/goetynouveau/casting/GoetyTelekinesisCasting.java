package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.TelekinesisSpell;
import com.Polarice3.Goety.utils.WandUtil;
import com.Polarice3.Goety.common.items.ModItems;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.EntityHitResult;

/** The native focus singleton is stateful. Each Ars channel owns its own native grab instance. */
public final class GoetyTelekinesisCasting {
    private GoetyTelekinesisCasting() {}
    public static boolean victimAllowed(GoetyNativeGeometry.View view,Entity target) {
        return target!=null && target.isAlive() && !target.isRemoved() && !target.isSpectator()
                && !(target instanceof net.minecraft.world.entity.player.Player player && player.isCreative())
                && target.level()==view.host().level() && target!=view.host() && target!=view.context().caster()
                && target!=view.context().damageOwner()
                && (!(target instanceof LivingEntity living) || !GoetyBreathGeometry.allied(view.host(),living)
                    && !GoetyBreathGeometry.allied(view.context().caster(),living));
    }
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var spell=new TelekinesisSpell();var staff=GoetyResources.staff(frame.caster()).copy();
        var stats=WandUtil.getStats(frame.caster(),spell);stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        int ticks=GoetyChannelCasting.boundedTicks(spell.shotsNumber(frame.caster(),staff),ars.getDurationMultiplier());
        if(ticks<=0)return false;
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,null,stats);
        GoetyNativeGeometry.scoped(view,()->{spell.findVictim(world,frame.caster(),stats);return null;});
        java.util.function.BooleanSupplier running=()->victimAllowed(view,spell.victim)
                && GoetyNativeGeometry.scoped(view,()->spell.conditionsMet(world,frame.caster(),stats));
        if(!running.getAsBoolean()){spell.victim=null;return false;}
        boolean[] activated={false};
        java.util.function.BooleanSupplier pulse=()->{
            if(!running.getAsBoolean())return false;
            GoetyNativeGeometry.scoped(view,()->{dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);return null;});activated[0]=true;return true;
        };
        Runnable close=()->{spell.victim=null;if(activated[0])GoetyFocusCooldowns.atLeast(frame.caster(),ModItems.TELEKINESIS_FOCUS.get(),100);};
        return GoetyChannelCasting.start(EffectFocus.TELEKINESIS,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,pulse,()->{},spell.CastingSound(frame.caster()),spell.loopSound(frame.caster()),running,close));
    }
}
