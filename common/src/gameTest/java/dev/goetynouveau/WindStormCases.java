package dev.goetynouveau;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class WindStormCases {
    private static void finish(GameTestHelper h,ServerPlayer p) {p.discard();RainStormCases.clear(h,p);h.succeed();}
    private static void aim(LivingEntity host,LivingEntity target) {
        var d=target.position().add(0,target.getBbHeight()/2.0,0).subtract(host.getEyePosition());
        float yaw=(float)Math.toDegrees(Math.atan2(-d.x,d.z));
        host.setYRot(yaw);host.setYHeadRot(yaw);host.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.sqrt(d.x*d.x+d.z*d.z))));
    }
    private static void durable(LivingEntity e) {e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);e.setHealth(200);}
    public static void flyingMovementAndBilling(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);host.setYRot(0);host.setYHeadRot(0);host.setXRot(0);
        var spell=(com.Polarice3.Goety.common.magic.Spell)GlyphCatalog.FLYING.focus().spell();
        dev.goetynouveau.platform.NativeBreathHooks.result(spell,h.getLevel(),host,ItemStack.EMPTY,WandUtil.getStats(host,spell));
        double nativePower=host.getDeltaMovement().length();host.setDeltaMovement(Vec3.ZERO);host.fallDistance=10;
        int fee=RainStormCases.cast(h,p,GlyphCatalog.FLYING,host,p,true,AugmentAmplify.INSTANCE,AugmentAmplify.INSTANCE);
        h.assertTrue(Math.abs(host.getDeltaMovement().length()-nativePower-1)<.0001 && host.getDeltaMovement().z>0 && host.fallDistance==0,"Native flight power plus integer potency, executor orientation and fall reset");
        h.assertTrue(p.getDeltaMovement().lengthSqr()==0,"Remote original caster does not fly");
        host.setYRot(-90);host.setYHeadRot(-90);SEHelper.setSESouls(p,fee*2);
        h.runAfterDelay(21,()->{
            h.assertTrue(host.getDeltaMovement().x>0 && Math.abs(host.getDeltaMovement().z)<.01,"Continuous flight follows live executor aim");
            h.assertTrue(SEHelper.getSESouls(p)==fee,"One additional native group of twenty results pays upkeep");
            SEHelper.setSESouls(p,0);ExecutionSessions.tick();
            h.assertTrue(!ExecutionSessions.isCasting(p),"Empty resources stop future motion callbacks");finish(h,p);
        });
    }
    public static void flyingStaffCreativeAndAlliance(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);host.setYRot(0);host.setYHeadRot(0);host.setXRot(0);
        var spell=(com.Polarice3.Goety.common.magic.Spell)GlyphCatalog.FLYING.focus().spell();
        dev.goetynouveau.platform.NativeBreathHooks.result(spell,h.getLevel(),host,ItemStack.EMPTY,WandUtil.getStats(host,spell));
        double nativePower=host.getDeltaMovement().length();host.setDeltaMovement(Vec3.ZERO);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WIND_STAFF.get()));p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);
        RainStormCases.cast(h,p,GlyphCatalog.FLYING,host,p,true);
        h.assertTrue(Math.abs(host.getDeltaMovement().length()-nativePower*2)<.0001 && SEHelper.getSESouls(p)==0,"Original caster Wind staff boost and creative resource bypass");
        h.runAfterDelay(3,()->{
            h.assertTrue(ExecutionSessions.isCasting(p),"Creative flight continues with an empty pool");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());ExecutionSessions.tick();
            h.assertTrue(!ExecutionSessions.isCasting(p),"Lost executor allegiance ends flight");finish(h,p);
        });
    }
    public static void whirlwindNativeMotionAndStaff(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);var target=host.getTarget();target.setPos(host.position().add(1,0,0));
        host.setRemainingFireTicks(100);host.setDeltaMovement(Vec3.ZERO);
        RainStormCases.cast(h,p,GlyphCatalog.WHIRLWIND,host,p,true,AugmentAmplify.INSTANCE);
        h.assertTrue(Math.abs(host.getDeltaMovement().y-.05)<.0001 && host.getRemainingFireTicks()==99 && target.getDeltaMovement().x>0,"Native radial push, potency lift and extinguishing run at the executor");
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);host.setDeltaMovement(0,-.5,0);host.setRemainingFireTicks(100);
        var arrow=EntityType.ARROW.create(h.getLevel());arrow.setPos(host.position().add(1,0,0));arrow.setDeltaMovement(-1,0,0);h.getLevel().addFreshEntity(arrow);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WIND_STAFF.get()));
        RainStormCases.cast(h,p,GlyphCatalog.WHIRLWIND,host,p,true,AugmentAmplify.INSTANCE);
        h.assertTrue(Math.abs(host.getDeltaMovement().y+.3)<.0001 && host.getRemainingFireTicks()==96,"Native falling lift overrides potency and Wind staff extinguishes four ticks");
        h.assertTrue(arrow.getDeltaMovement().x>0,"Wind staff uses native airborne arrow reflection");finish(h,p);
    }
    public static void shockingNativeDamageAndOwnership(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);var target=host.getTarget();target.setPos(host.position().add(0,0,5));durable(target);aim(host,target);
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-10,0,0));h.getLevel().addFreshEntity(owner);
        int fee=RainStormCases.cast(h,p,GlyphCatalog.SHOCKING,host,owner,true,AugmentAmplify.INSTANCE);
        h.assertTrue(target.getHealth()<200 && target.getLastDamageSource().getEntity()==owner,"Native electric hurt callback selects the executor's visible target and attributes explicit owner");
        SEHelper.setSESouls(p,fee*2);
        h.runAfterDelay(21,()->{
            h.assertTrue(SEHelper.getSESouls(p)==fee && target.getHealth()<198,"Actual native repeated shock and grouped upkeep");
            SEHelper.setSESouls(p,0);ExecutionSessions.tick();h.assertTrue(!ExecutionSessions.isCasting(p),"No unpaid lightning callbacks");finish(h,p);
        });
    }
    public static void shockingRodAndRejectedExecutors(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);var target=host.getTarget();target.setPos(host.position().add(0,0,5));durable(target);aim(host,target);
        var rod=BlockPos.containing(host.position().add(0,0,7));h.getLevel().setBlock(rod,Blocks.LIGHTNING_ROD.defaultBlockState(),2);
        RainStormCases.cast(h,p,GlyphCatalog.SHOCKING,host,p,true);
        h.assertTrue(target.getHealth()==200,"Native rod diversion uses the remote executor's aim, preserving native no-hit branch and lightning packet");
        h.getLevel().setBlock(rod,Blocks.AIR.defaultBlockState(),2);RainStormCases.clear(h,p);host=RainStormCases.host(h,p);
        for(var glyph:java.util.List.of(GlyphCatalog.FLYING,GlyphCatalog.WHIRLWIND,GlyphCatalog.SHOCKING)) {
            RainStormCases.cast(h,p,glyph,host.getTarget(),p,false);
            h.assertTrue(!ExecutionSessions.isCasting(p),"Enemy cannot execute a beneficial sustained spell");
        }
        SEHelper.setSESouls(p,0);RainStormCases.cast(h,p,GlyphCatalog.SHOCKING,host,p,false);
        h.assertTrue(!ExecutionSessions.isCasting(p),"Unfunded channel has no native callback");finish(h,p);
    }
}

