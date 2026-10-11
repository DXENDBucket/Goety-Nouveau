package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import com.Polarice3.Goety.common.magic.spells.wild.GrappleSpell;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

/** Native player grapple state supplies collision, attachment, rope and force; Core owns lifetime. */
public final class GoetyGrappleCasting {
    private GoetyGrappleCasting(){}
    public static boolean valid(VineHook hook){
        var source=GoetyAreaProvenance.source(hook,null);var host=hook.getPlayerOwner();
        return hook.level() instanceof ServerLevel world && source!=null && host!=null && GlyphCatalog.GRAPPLE.isEnabled()
                && GoetyChannelCasting.eligible(world,source,host) && SEHelper.getGrappling(host)==hook && hook.distanceToSqr(host)<=64*64;
    }
    public static boolean apply(ServerLevel world,CastContext frame,SpellStats ars,SpellContext context){
        if(context==null || !(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof Player host)
                || !GoetyChannelCasting.eligible(world,frame.caster(),host))return false;
        var prior=SEHelper.getGrappling(host);
        // Do not take over a native wand or another caster's lease on this player's shared hook slot.
        if(prior!=null && (!(prior instanceof VineHook hook) || !((GoetyGrappleAccess)hook).goetyNouveau$managed()
                || GoetyAreaProvenance.source(hook,null)!=frame.caster()))return false;
        var spell=new GrappleSpell(){
            @Override public int soulCost(net.minecraft.world.entity.LivingEntity caster,net.minecraft.world.item.ItemStack held){return prior!=null?0:super.soulCost(caster,held);}
        };var staff=GoetyResources.staff(frame.caster()).copy();var stats=WandUtil.getStats(frame.caster(),spell);
        var view=new GoetyNativeGeometry.View(frame.withExecutor(host),host,spell,null,stats);
        var charge=GoetyNativeGeometry.scoped(view,()->GoetyResources.quote(context,spell,ars.getAugments()));
        if(prior!=null)return GoetyResources.activation(charge).trigger(()->GoetyNativeGeometry.scoped(view,()->{
            spell.SpellResult(world,frame.caster(),staff,stats);boolean success=prior.isRemoved() && SEHelper.getGrappling(host)!=prior;
            if(success)GoetyFocusCooldowns.atLeast(frame.caster(),net.minecraft.core.registries.BuiltInRegistries.ITEM.get(EffectFocus.GRAPPLE.focusId()),spell.defaultSpellCooldown());
            return success;
        }));
        int ticks=GoetyChannelCasting.boundedTicks(0,ars.getDurationMultiplier());if(ticks<=0)return false;
        VineHook[] hook={null};
        var driver=new ExecutionDriver<com.Polarice3.Goety.common.magic.SpellStat>(){
            public boolean occupiesCaster(){return false;}
            public boolean start(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session){
                GoetyNativeGeometry.scoped(view,()->{spell.SpellResult(world,frame.caster(),staff,stats);return null;});
                if(SEHelper.getGrappling(host) instanceof VineHook added && world.getEntity(added.getUUID())==added
                        && ((GoetyGrappleAccess)added).goetyNouveau$managed())hook[0]=added;
                return hook[0]!=null;
            }
            public boolean tick(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session){return hook[0]==null || hook[0].isRemoved() || !valid(hook[0]);}
            public void close(ExecutionSession<com.Polarice3.Goety.common.magic.SpellStat> session,ExecutionSession.EndReason reason){
                if(hook[0]!=null){hook[0].discard();GoetyFocusCooldowns.atLeast(frame.caster(),net.minecraft.core.registries.BuiltInRegistries.ITEM.get(EffectFocus.GRAPPLE.focusId()),spell.defaultSpellCooldown());}
            }
        };
        var session=new ExecutionSession<>(world,frame.withExecutor(host).withAccount(charge.account()),stats,ticks,driver,ignored->{},
                ()->GlyphCatalog.GRAPPLE.isEnabled() && GoetyChannelCasting.eligible(world,frame.caster(),host),ExecutionScope.DIRECT);
        session.billing(action->GoetyResources.activation(charge).trigger(action),false);
        return ExecutionSessions.start(session);
    }
    public static int hand(GoetyNativeGeometry.View view){
        if(view.host()==view.context().caster() && view.context().ars()!=null){var tool=view.context().ars().getCasterTool();
            if(!tool.isEmpty() && (tool==view.host().getOffhandItem()
                    || net.minecraft.world.item.ItemStack.matches(tool,view.host().getOffhandItem()) && !net.minecraft.world.item.ItemStack.matches(tool,view.host().getMainHandItem())))return 1;
        }return 0;
    }
}
