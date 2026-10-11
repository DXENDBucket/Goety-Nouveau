package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.GulfTentacle;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.EntityHitResult;

/** Adapt the native one-second attached strike, retaining its native packets, animation and hit callbacks. */
public final class GoetyWaterWhipCasting {
    private GoetyWaterWhipCasting() {}
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars){
        if(!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var caster=frame.caster();var spell=(Spell)EffectFocus.WATER_WHIP.spell();var stats=WandUtil.getStats(caster,spell);
        int potency=Math.max(0,stats.getPotency()+(int)ars.getAmpMultiplier()),range=stats.getRange();
        if(WandUtil.enchantedFocus(caster)){potency+=WandUtil.getPotencyLevel(caster);range+=WandUtil.getRangeLevel(caster);}
        // The Ars book is the initiating main-hand tool; actual executor arm supplies the native offset.
        var hand=caster.isUsingItem()?caster.getUsedItemHand():InteractionHand.MAIN_HAND;int side=host.getMainArm()==HumanoidArm.RIGHT?1:-1;
        if(hand==InteractionHand.OFF_HAND)side=-side;
        float yaw=host.yBodyRot*((float)Math.PI/180);double sin=Math.sin(yaw),cos=Math.cos(yaw),offset=side*.5;
        var origin=host.getEyePosition().add(-cos*offset-sin,(host.isCrouching()?-.1875:0)-.45,-sin*offset+cos);
        var tentacle=new GulfTentacle(ModEntityType.GULF_TENTACLE.get(),world);
        tentacle.setPos(origin);tentacle.setOffset(side);tentacle.setRot(host.getYRot(),host.getXRot());
        // Native synchronized owner controls attachment and client aim, not this adapter's damage attribution.
        tentacle.setTrueOwner(host);tentacle.setRange(tentacle.getRange()+range);
        tentacle.setStaff(spell.rightStaff(GoetyResources.staff(caster)));
        if(potency>0)tentacle.addEffect(NativeEffectHooks.tentacleBuff(Math.min(10,potency-1)));
        GoetyAreaProvenance.mark(tentacle,frame);
        if(!world.addFreshEntity(tentacle))return false;
        spell.playSound(world,host,ModSounds.WHIP_SWING.get());return true;
    }
}
