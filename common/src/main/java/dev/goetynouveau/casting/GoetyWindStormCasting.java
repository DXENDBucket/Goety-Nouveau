package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/** Native wind movement/area callbacks and native lightning packets, scoped to an allied executor. */
public final class GoetyWindStormCasting {
    private GoetyWindStormCasting() {}
    public static int flyingPotency(int baseline, boolean enchanted, int enchantment, boolean additive) {
        return enchanted ? (additive ? baseline + enchantment : enchantment) : baseline;
    }
    public static boolean apply(EffectFocus focus,ServerLevel world,CastContext frame,SpellStats ars,ResourceCharge charge) {
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var spell=(Spell)focus.spell();var caster=frame.caster();var staff=GoetyResources.staff(caster).copy();
        int ticks=GoetyChannelCasting.boundedTicks(((com.Polarice3.Goety.api.magic.IChargingSpell)spell).shotsNumber(caster,staff),ars.getDurationMultiplier());
        if(ticks<=0)return false;
        var stats=WandUtil.getStats(caster,spell);
        if(focus==EffectFocus.FLYING) {
            // Old Goety replaced potency; current Goety adds focus potency to native attributes.
            // NativeWindRecipientMixin suppresses that gear branch after we prepare it once.
            int base=flyingPotency(stats.getPotency(),WandUtil.enchantedFocus(caster),
                    WandUtil.getPotencyLevel(caster),dev.goetynouveau.platform.NativeStatHooks.ADDITIVE_FLYING);
            stats.setPotency(Math.max(0,base+(int)ars.getAmpMultiplier()));
        } else if(focus==EffectFocus.WHIRLWIND) {
            // Native lift uses only focus potency, not ordinary SpellStat potency.
            int base=WandUtil.enchantedFocus(caster)?WandUtil.getPotencyLevel(caster):0;
            stats.setPotency(Math.max(0,base+(int)ars.getAmpMultiplier()));
            stats.setRadius(stats.getRadius()+ars.getAoeMultiplier()+(WandUtil.enchantedFocus(caster)?NativeEffectHooks.radius(caster):0));
            double radius=stats.getRadius()+(spell.rightStaff(staff)?1:0);
            if(!Double.isFinite(radius) || radius<=0)return false;
        } else if(focus==EffectFocus.SHOCKING) {
            stats.setPotency(Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()));
            stats.setBurning(stats.getBurning()+ars.getBuffCount(dev.goetynouveau.glyph.AugmentBurning.INSTANCE));
        } else return false;
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,null,stats);
        Runnable pulse=()->GoetyNativeGeometry.scoped(view,()->{
            dev.goetynouveau.platform.NativeBreathHooks.result(spell,world,caster,staff,stats);return null;
        });
        return GoetyChannelCasting.start(focus,world,frame,host,spell,stats,charge,
                new GoetyChannelCasting.Program(ticks,1,pulse,()->{},spell.CastingSound(caster),spell.loopSound(caster)));
    }
}
