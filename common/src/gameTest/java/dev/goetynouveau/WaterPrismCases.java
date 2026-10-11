package dev.goetynouveau;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.entity.GoetyBeamDisplay;
import dev.goetynouveau.glyph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.EntityHitResult;
import java.util.*;

public final class WaterPrismCases {
    private static GoetyBeamDisplay display(GameTestHelper h) {
        var all=RainStormCases.entities(h,GoetyBeamDisplay.class);
        h.assertTrue(all.size()==1,"One tracked native-style visual entity");return all.get(0);
    }
    private static void finish(GameTestHelper h,ServerPlayer p) {p.discard();RainStormCases.clear(h,p);h.succeed();}
    private static void durable(LivingEntity entity) {
        entity.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);entity.setHealth(200);
    }
    private static void aim(LivingEntity host,LivingEntity target) {
        var direction=target.position().add(0,target.getBbHeight()/2,0).subtract(host.getEyePosition());
        float yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
        host.setYRot(yaw);host.setYHeadRot(yaw);
        host.setXRot((float)-Math.toDegrees(Math.atan2(direction.y,Math.sqrt(direction.x*direction.x+direction.z*direction.z))));
    }
    private static int prism(GameTestHelper h,ServerPlayer p,LivingEntity host,LivingEntity owner,AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,GlyphCatalog.PRISMA_BEAM);for(var a:augments)spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
        resolver.spellContext.nextPart();var hit=new EntityHitResult(host);
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null).withDamageOwner(owner));
        int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,GlyphCatalog.PRISMA_BEAM.focus().spell(),List.of(augments)).amount();
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before,"Native prism charge setup does not pay before its result");return fee;
    }
    public static void waterNativeDamageVisualAndVariants(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);host.setYRot(0);host.setXRot(0);
        var target=host.getTarget();target.setPos(host.position().add(0,0,6));aim(host,target);durable(target);target.setRemainingFireTicks(100);
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-10,0,0));h.getLevel().addFreshEntity(owner);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
        var originalTarget=MiscCapHelper.getClientTarget(p);
        int fee=RainStormCases.cast(h,p,GlyphCatalog.WATER_JET,host,owner,true,AugmentAmplify.INSTANCE);
        var display=display(h);
        h.assertTrue(display.style()==GoetyBeamDisplay.WATER && display.host()==host && display.target()==target,"Synced visual state follows real allied host and native ray-selected entity");
        h.assertTrue(target.getHealth()<200 && target.getLastDamageSource().getEntity()==owner,"Actual native water damage retains explicit owner");
        h.assertTrue(!target.isOnFire() && target.getActiveEffects().stream().anyMatch(e->e.getEffect().equals(dev.goetynouveau.platform.NativeEffectHooks.sapped(1,0).getEffect())),"Native extinguishing and Abyss staff Sapped both execute");
        h.assertTrue(MiscCapHelper.getClientTarget(p)==originalTarget && !display.shouldBeSaved(),"Visuals neither overwrite native player capability nor persist orphan state");
        h.assertTrue(display.origin(1).distanceTo(host.getEyePosition())<.01 && display.origin(1).distanceTo(p.position())>12,"Display origin is real executor's eyes");
        SEHelper.setSESouls(p,fee*2);host.setYRot(90);host.setYHeadRot(90);host.setXRot(0);
        h.runAfterDelay(22,()->{
            h.assertTrue(SEHelper.getSESouls(p)==fee && display.target()==null,"Water upkeep and live native aim continue");
            h.assertTrue(display.end(1).subtract(display.origin(1)).x< -5,"Water visual endpoint follows changed direction");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());ExecutionSessions.tick();
            h.assertTrue(display.isRemoved() && !ExecutionSessions.isCasting(p),"Changed alliance clears water display and channel");finish(h,p);
        });
    }
    public static void waterEnvironmentalAndEmptyAccount(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);host.getTarget().discard();host.setTarget(null);
        host.setYRot(0);host.setXRot(25);
        var fire=BlockPos.containing(host.position()).offset(0,0,3);
        h.getLevel().setBlock(fire,Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT,true),2);
        RainStormCases.cast(h,p,GlyphCatalog.WATER_JET,host,p,true);
        h.assertTrue(!h.getLevel().getBlockState(fire).getValue(CampfireBlock.LIT),"Native water callback extinguishes a selected campfire");
        var display=display(h);SEHelper.setSESouls(p,0);display.tick();ExecutionSessions.tick();
        h.assertTrue(display.isRemoved() && !ExecutionSessions.isCasting(p),"Empty account removes display and water channel");
        RainStormCases.cast(h,p,GlyphCatalog.WATER_JET,host,p,false);
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);host.setYRot(0);host.setXRot(0);durable(host.getTarget());
        host.getTarget().setPos(host.position().add(0,0,6));aim(host,host.getTarget());
        p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);
        RainStormCases.cast(h,p,GlyphCatalog.WATER_JET,host,p,true,AugmentBurning.INSTANCE);
        h.assertTrue(host.getTarget().isOnFire() && SEHelper.getSESouls(p)==0,"Native boiling-water Burning branch and creative payment");
        finish(h,p);
    }
    public static void prismNativeChargeAndFinalPayment(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);var target=host.getTarget();durable(target);
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-10,0,0));h.getLevel().addFreshEntity(owner);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
        var originalTarget=MiscCapHelper.getClientTarget(p);
        int fee=prism(h,p,host,owner,AugmentAmplify.INSTANCE,AugmentExtendTime.INSTANCE),before=SEHelper.getSESouls(p);
        var display=display(h);var nativeSpell=GlyphCatalog.PRISMA_BEAM.focus().spell();
        int ticks=GoetyChannelCasting.boundedTicks(nativeSpell.castDuration(p,p.getOffhandItem()),1);
        h.assertTrue(GoetyActiveCastProvider.preparationTicks(nativeSpell,p,p.getOffhandItem())==0,"Native target-dependent charge is not duplicated before the Ars hit");
        h.assertTrue(display.style()==GoetyBeamDisplay.PRISMA && display.target()==target && target.getHealth()==200,"Native charge visuals select target without early damage");
        h.assertTrue(MiscCapHelper.getClientTarget(p)==originalTarget,"Prism display does not replace original held-wand target state");
        h.runAfterDelay(5,()->{
            h.assertTrue(target.getHealth()==200 && SEHelper.getSESouls(p)==before && !display.isRemoved(),"Charge progresses before one final result");
            h.runAfterDelay(ticks-3,()->{
                h.assertTrue(target.getHealth()<200 && target.getLastDamageSource().getEntity()==owner,"Native final magic/attack result and explicit source");
                h.assertTrue(SEHelper.getSESouls(p)==before-fee && display.isRemoved() && !ExecutionSessions.isCasting(p),"Final result pays once and clears charge visuals");
                finish(h,p);
            });
        });
    }
    public static void prismInvalidAndInterruptedTargets(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);
        int before=SEHelper.getSESouls(p);prism(h,p,host.getTarget(),p);
        h.assertTrue(RainStormCases.entities(h,GoetyBeamDisplay.class).isEmpty(),"Enemy cannot become prism executor");
        prism(h,p,host,p);var display=display(h);host.setTarget(p);ExecutionSessions.tick();
        h.assertTrue(display.isRemoved() && SEHelper.getSESouls(p)==before,"Friendly native target interrupts without damage or payment");
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);prism(h,p,host,p);display=display(h);
        h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());ExecutionSessions.tick();
        h.assertTrue(display.isRemoved() && SEHelper.getSESouls(p)==before,"Lost executor alliance cancels native charge without payment");
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);prism(h,p,host,p);display=display(h);
        SEHelper.setSESouls(p,0);display.tick();ExecutionSessions.tick();
        h.assertTrue(display.isRemoved() && !ExecutionSessions.isCasting(p),"Unfunded final charge cannot survive or attack");
        finish(h,p);
    }
}
