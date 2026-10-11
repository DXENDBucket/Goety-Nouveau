package dev.goetynouveau;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.entities.projectiles.VoidShock;
import com.Polarice3.Goety.common.entities.util.ModFallingBlock;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

public final class GroundChannelCases {
    private static void floor(GameTestHelper h) {
        var base=BlockPos.containing(RainStormCases.scene(h));
        for(int x=-18;x<=18;x++)for(int z=-18;z<=24;z++)h.getLevel().setBlock(base.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
    }
    private static void durable(LivingEntity target) {
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);target.setHealth(200);
    }
    private static void finish(GameTestHelper h,ServerPlayer p) {p.discard();RainStormCases.clear(h,p);h.succeed();}
    public static void voidNativeEmissionAndOwnership(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);floor(h);var host=RainStormCases.host(h,p);var target=host.getTarget();durable(target);
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-12,0,0));h.getLevel().addFreshEntity(owner);
        int fee=RainStormCases.cast(h,p,GlyphCatalog.VOID_SHOCK,host,owner,true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE);
        var shots=RainStormCases.entities(h,VoidShock.class);
        h.assertTrue(shots.size()==1 && shots.get(0).getOwner()==owner && shots.get(0).getTarget()==target,"Actual native void shock and explicit owner/native host target: count="+shots.size()+", spell="+GlyphCatalog.VOID_SHOCK.focus().spell().getClass().getName());
        var first=shots.get(0);
        h.assertTrue(Math.abs(first.getX()-host.getX())<=3 && first.getY()>host.getY()+2 && first.getExtraDamage()>=1 && first.getExtraRadius()>=1,"Original launch cloud and Ars potency/radius");
        h.assertTrue(first.initTime==10 && !first.hasTarget,"Native ten-tick hover and target acquisition animation retained");
        h.assertTrue(GoetyActiveCastProvider.preparationTicks(GlyphCatalog.VOID_SHOCK.focus().spell(),p,ItemStack.EMPTY)==10,"Preparation does not repeat the native active barrage");
        SEHelper.setSESouls(p,fee*3);
        h.runAfterDelay(4,()->{
            h.assertTrue(RainStormCases.entities(h,VoidShock.class).size()==1,"No second shock before native five-tick cadence");
            h.runAfterDelay(2,()->{
                h.assertTrue(RainStormCases.entities(h,VoidShock.class).size()==2,"Next shock follows native cadence");
                h.runAfterDelay(25,()->{
                    h.assertTrue(target.getHealth()<200 && target.getLastDamageSource().getEntity()==owner,"Native homing impact/explosion deals real attributed damage");
                    h.assertTrue(SEHelper.getSESouls(p)==fee*2,"Active barrage upkeep pays at twenty ticks");
                    SEHelper.setSESouls(p,0);ExecutionSessions.tick();
                    h.assertTrue(!ExecutionSessions.isCasting(p),"Empty resource pool ends emission");finish(h,p);
                });
            });
        });
    }
    public static void voidStaffAndAllegiance(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);floor(h);var host=RainStormCases.host(h,p);durable(host.getTarget());
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOID_STAFF.get()));
        p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);
        RainStormCases.cast(h,p,GlyphCatalog.VOID_SHOCK,host,p,true);
        h.runAfterDelay(3,()->{
            h.assertTrue(RainStormCases.entities(h,VoidShock.class).size()==2 && SEHelper.getSESouls(p)==0,"Native Void staff two-tick cadence and creative exemption");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());ExecutionSessions.tick();
            h.assertTrue(!ExecutionSessions.isCasting(p),"Lost executor alliance stops the barrage");
            var old=new HashSet<UUID>();RainStormCases.entities(h,VoidShock.class).forEach(e->old.add(e.getUUID()));
            h.runAfterDelay(5,()->{h.assertTrue(RainStormCases.entities(h,VoidShock.class).stream().allMatch(e->old.contains(e.getUUID())),"No new unpaid shocks after interruption");finish(h,p);});
        });
    }
    public static void quakeNativeForwardAndStaff(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);floor(h);var host=RainStormCases.host(h,p);host.yBodyRot=0;host.setYRot(0);var target=host.getTarget();target.setPos(host.position().add(0,0,1.2));durable(target);
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-10,0,0));h.getLevel().addFreshEntity(owner);
        RainStormCases.cast(h,p,GlyphCatalog.QUAKING,host,owner,true,AugmentAmplify.INSTANCE);
        h.assertTrue(target.getHealth()<200 && target.getLastDamageSource().getEntity()==owner,"Original three-lane tremor applies real explicit-owner damage");
        h.assertTrue(!RainStormCases.entities(h,ModFallingBlock.class).isEmpty(),"Native falling debris is actually generated");
        h.assertTrue(GoetyActiveCastProvider.preparationTicks(GlyphCatalog.QUAKING.focus().spell(),p,ItemStack.EMPTY)==GlyphCatalog.QUAKING.focus().spell().defaultCastDuration(),"Preparation excludes active wave range");
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);host.setYRot(0);host.yBodyRot=0;host.getTarget().setPos(host.position().add(0,0,-1.2));durable(host.getTarget());
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
        RainStormCases.cast(h,p,GlyphCatalog.QUAKING,host,p,true);
        h.assertTrue(!RainStormCases.entities(h,ModFallingBlock.class).isEmpty(),"Original Geomancy staff arc geometry is used");
        h.runAfterDelay(15,()->{h.assertTrue(!ExecutionSessions.isCasting(p),"Finite wave terminates naturally");finish(h,p);});
    }
    public static void quakeRingsAndVoidBoundary(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);floor(h);var host=RainStormCases.host(h,p);host.getTarget().setPos(host.position().add(-1,0,0));durable(host.getTarget());
        p.setShiftKeyDown(true);RainStormCases.cast(h,p,GlyphCatalog.QUAKING,host,p,true,AugmentAOE.INSTANCE);
        h.assertTrue(host.getTarget().getHealth()<200,"Book-only original-caster sneak invokes native surrounding ring");
        h.assertTrue(!RainStormCases.entities(h,ModFallingBlock.class).isEmpty(),"Native radial debris appears");
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);host.setPos(host.getX(),h.getLevel().getMinBuildHeight(),host.getZ());host.getTarget().setPos(host.position().add(0,0,3));
        // No floor below this host: the original surrounding loop had no build-height bound.
        RainStormCases.cast(h,p,GlyphCatalog.QUAKING,host,p,true);
        h.assertTrue(h.getLevel().getEntitiesOfClass(ModFallingBlock.class,host.getBoundingBox().inflate(16)).isEmpty(),"No invented bedrock/debris at the void boundary");
        host.getTarget().discard();host.discard();
        p.setShiftKeyDown(false);finish(h,p);
    }
    public static void wardingNativePlayerData(GameTestHelper h,ServerPlayer p,ServerPlayer ally) {
        RainStormCases.prepare(h,p);ally.setPos(RainStormCases.scene(h));ally.setNoGravity(true);h.getLevel().addNewPlayer(ally);
        var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("ward_"+UUID.randomUUID().toString().substring(0,8));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(ally.getScoreboardName(),team);
        SEHelper.setCurrentWarding(ally,0);SEHelper.setCurrentWarding(p,0);SEHelper.setSESouls(p,0);ally.tickCount=5;
        RainStormCases.cast(h,p,GlyphCatalog.WARDING,ally,p,true,AugmentAmplify.INSTANCE);
        h.assertTrue(SEHelper.getMaxWarding(ally)==20 && SEHelper.getWardingLeft(ally)==2 && SEHelper.getWardingLeft(p)==0,"Original native player ward storage, cap and recipient tick cadence");
        h.runAfterDelay(7,()->{
            h.assertTrue(SEHelper.getWardingLeft(ally)>2 && SEHelper.getSESouls(p)==0,"Zero native focus cost and continued original five-tick ward replenishment");
            board.removePlayerFromTeam(ally.getScoreboardName());ExecutionSessions.tick();int remaining=SEHelper.getWardingLeft(ally);
            h.assertTrue(!ExecutionSessions.isCasting(p),"Lost alliance stops native player warding");
            h.runAfterDelay(7,()->{h.assertTrue(SEHelper.getWardingLeft(ally)==remaining,"Stopping replenishment retains native stored ward");ally.discard();finish(h,p);});
        });
    }
    public static void rejectedExecutors(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);
        for(var glyph:List.of(GlyphCatalog.VOID_SHOCK,GlyphCatalog.QUAKING,GlyphCatalog.WARDING))RainStormCases.cast(h,p,glyph,host.getTarget(),p,false);
        RainStormCases.cast(h,p,GlyphCatalog.WARDING,host,p,false);
        SEHelper.setSESouls(p,0);RainStormCases.cast(h,p,GlyphCatalog.VOID_SHOCK,host,p,false);RainStormCases.cast(h,p,GlyphCatalog.QUAKING,host,p,false);
        h.assertTrue(!ExecutionSessions.isCasting(p) && RainStormCases.entities(h,VoidShock.class).isEmpty(),"Unsupported/unfunded executors create no channel or native attack");finish(h,p);
    }
}
