package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.entity.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class GoetyWaterJetCasting {
    private GoetyWaterJetCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var focus=EffectFocus.WATER_JET;var spell=(Spell)focus.spell();
        var staff=GoetyResources.staff(frame.caster()).copy();
        int ticks=GoetyChannelCasting.boundedTicks(((com.Polarice3.Goety.api.magic.IChargingSpell)spell).shotsNumber(frame.caster(),staff),ars.getDurationMultiplier());
        if(ticks<=0)return false;
        var stats=WandUtil.getStats(frame.caster(),spell);
        stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
        stats.setBurning(stats.getBurning()+ars.getBuffCount(dev.goetynouveau.glyph.AugmentBurning.INSTANCE));
        float flow=WandUtil.enchantedFocus(frame.caster())?.4f+WandUtil.getPotencyLevel(frame.caster())/5f*.2f:.4f;
        var display=GoetyEntities.CHANNEL_BEAM.get().create(world);
        long expires=world.getGameTime()+ticks;boolean[] open={true},spawned={false};
        display.bind(host,GoetyBeamDisplay.WATER,ticks,flow,()->open[0] && world.getGameTime()<expires
                && GlyphCatalog.WATER_JET.isEnabled() && GoetyChannelCasting.eligible(world,frame.caster(),host)
                && (charge.amount()==0 || charge.account().free() || charge.account().canSpend(1)));
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,display::observe);
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,()->{
                    if(!spawned[0])spawned[0]=world.addFreshEntity(display);
                    if(!spawned[0] || display.isRemoved())return false;
                    GoetyNativeGeometry.scoped(view,()->{
                        dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,frame.caster(),staff,stats);return null;
                    });
                    return true;
                },()->{},spell.CastingSound(frame.caster()),spell.loopSound(frame.caster()),
                        ()->!display.isRemoved(),()->{open[0]=false;display.discard();}));
    }
}
