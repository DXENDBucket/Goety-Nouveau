package dev.goetynouveau;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.spells.TelekinesisSpell;
import com.Polarice3.Goety.common.magic.spells.geomancy.BurrowingSpell;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.AugmentBurning;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class UtilityChannelCases {
    private static void finish(GameTestHelper h,ServerPlayer p){p.discard();RainStormCases.clear(h,p);h.succeed();}
    private static void addPlayer(GameTestHelper h,ServerPlayer p){p.setPos(RainStormCases.scene(h).add(-24,0,-12));p.setNoGravity(true);h.getLevel().addNewPlayer(p);p.setGameMode(GameType.SURVIVAL);SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,100000);}
    private static void face(LivingEntity e){if(e instanceof Mob)e.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));e.setYRot(0);e.setYHeadRot(0);e.setXRot(0);}
    private static void ally(GameTestHelper h,ServerPlayer p,ServerPlayer host){addPlayer(h,host);host.setPos(RainStormCases.scene(h));face(host);var board=h.getLevel().getScoreboard();var team=board.addPlayerTeam("mine_"+java.util.UUID.randomUUID().toString().substring(0,8));board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(host.getScoreboardName(),team);}
    private static BlockPos rock(GameTestHelper h,ServerPlayer host){var pos=BlockPos.containing(host.position().add(0,1,4));h.getLevel().setBlock(pos,Blocks.STONE.defaultBlockState(),2);return pos;}
    public static void telekinesisSessionIsolation(GameTestHelper h,ServerPlayer p,ServerPlayer second){
        RainStormCases.prepare(h,p);addPlayer(h,second);var a=RainStormCases.host(h,p);face(a);a.getTarget().setPos(a.position().add(2,0,4));
        var b=RainStormCases.host(h,second);b.setPos(a.position().add(10,0,0));face(b);b.getTarget().setPos(b.position().add(-2,0,4));
        int fee=RainStormCases.cast(h,p,GlyphCatalog.TELEKINESIS,a,p,true);RainStormCases.cast(h,second,GlyphCatalog.TELEKINESIS,b,second,true);
        h.assertTrue(a.getTarget().getDeltaMovement().x<0 && b.getTarget().getDeltaMovement().x>0,"Two independent native grabs apply opposite corrections");
        var singleton=(TelekinesisSpell)GlyphCatalog.TELEKINESIS.focus().spell();h.assertTrue(singleton.victim==null,"Native focus singleton was never used as channel state");
        SEHelper.setSESouls(p,fee*2);
        h.runAfterDelay(21,()->{
            h.assertTrue(ExecutionSessions.isCasting(p) && ExecutionSessions.isCasting(second) && SEHelper.getSESouls(p)==fee,"Independent channels retain targets and charge upkeep: a="+ExecutionSessions.isCasting(p)+", b="+ExecutionSessions.isCasting(second)+", fee="+fee+", remaining="+SEHelper.getSESouls(p)+", hurt="+a.hurtTime+"/"+b.hurtTime+", distances="+a.distanceTo(a.getTarget())+"/"+b.distanceTo(b.getTarget()));
            SEHelper.setSESouls(p,0);ExecutionSessions.tick();h.assertTrue(!ExecutionSessions.isCasting(p) && ExecutionSessions.isCasting(second),"Stopping one grab does not stop or clear the other");
            second.discard();finish(h,p);
        });
    }
    public static void telekinesisNativeCollisionAndRange(GameTestHelper h,ServerPlayer p){
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);face(host);var target=host.getTarget();target.setPos(host.position().add(2,0,4));target.setDeltaMovement(5,0,0);target.horizontalCollision=true;
        var owner=EntityType.COW.create(h.getLevel());owner.setNoAi(true);owner.setPos(host.position().add(-8,0,0));h.getLevel().addFreshEntity(owner);
        RainStormCases.cast(h,p,GlyphCatalog.TELEKINESIS,host,owner,true,AugmentAmplify.INSTANCE);
        h.assertTrue(target.getHealth()<target.getMaxHealth() && target.getLastDamageSource().getEntity()==owner,"Native collision damage retains explicit source");
        target.horizontalCollision=false;target.setPos(host.position().add(0,0,100));ExecutionSessions.tick();
        h.assertTrue(!ExecutionSessions.isCasting(p),"Native maximum grab distance cancels the channel");finish(h,p);
    }
    public static void telekinesisInvalidAndHurt(GameTestHelper h,ServerPlayer p){
        RainStormCases.prepare(h,p);var host=RainStormCases.host(h,p);face(host);
        RainStormCases.cast(h,p,GlyphCatalog.TELEKINESIS,host.getTarget(),p,false);
        h.getLevel().getScoreboard().addPlayerToTeam(host.getTarget().getScoreboardName(),h.getLevel().getScoreboard().getPlayersTeam(p.getScoreboardName()));
        RainStormCases.cast(h,p,GlyphCatalog.TELEKINESIS,host,p,false);
        h.getLevel().getScoreboard().removePlayerFromTeam(host.getTarget().getScoreboardName());host.getTarget().setPos(host.position().add(1,0,4));
        RainStormCases.cast(h,p,GlyphCatalog.TELEKINESIS,host,p,true);host.hurtTime=5;ExecutionSessions.tick();
        h.assertTrue(!ExecutionSessions.isCasting(p),"Native recipient hurt cancels its grab");finish(h,p);
    }
    public static void burrowingProgressDropsAndCleanup(GameTestHelper h,ServerPlayer p,ServerPlayer host){
        RainStormCases.prepare(h,p);ally(h,p,host);var pos=rock(h,host);
        RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true);
        h.assertTrue(pos.equals(SEHelper.getMiningPos(host)) && SEHelper.getMiningProgress(host)>0 && SEHelper.getMiningProgress(p)==0,"Actual executor owns native mining progress, original caster pays");
        h.runAfterDelay(18,()->{
            h.assertTrue(h.getLevel().getBlockState(pos).isAir() && RainStormCases.entities(h,ItemEntity.class).stream().anyMatch(e->e.getItem().is(Items.COBBLESTONE)),"Original mining callback really breaks and drops stone");
            h.assertTrue(!ExecutionSessions.isCasting(p) && SEHelper.getMiningProgress(host)==0 && SEHelper.getMiningPos(host)==null,"No target ends mining and removes native cracks/state");host.discard();finish(h,p);
        });
    }
    public static void burrowingLeasePermissionsAndResources(GameTestHelper h,ServerPlayer p,ServerPlayer host,ServerPlayer second){
        RainStormCases.prepare(h,p);ally(h,p,host);addPlayer(h,second);h.getLevel().getScoreboard().addPlayerToTeam(second.getScoreboardName(),h.getLevel().getScoreboard().getPlayersTeam(p.getScoreboardName()));var pos=rock(h,host);
        SEHelper.setMiningProgress(host,3);SEHelper.setMiningPos(host,pos);RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,false);
        h.assertTrue(SEHelper.getMiningProgress(host)==3 && pos.equals(SEHelper.getMiningPos(host)),"Existing native mining writer is untouched");BurrowingSpell.resetMiningProgress(h.getLevel(),host);
        host.setGameMode(GameType.ADVENTURE);RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,false);h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.STONE),"Actual player's build permission is authoritative");host.setGameMode(GameType.SURVIVAL);
        int fee=RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true);RainStormCases.cast(h,second,GlyphCatalog.BURROWING,host,second,false);
        h.assertTrue(SEHelper.getMiningProgress(host)==1,"Second channel cannot reset the first writer's progress");SEHelper.setSESouls(p,0);ExecutionSessions.tick();
        h.assertTrue(!ExecutionSessions.isCasting(p) && SEHelper.getMiningProgress(host)==0 && SEHelper.getMiningPos(host)==null,"Resource interruption clears only owned mining state");
        p.setGameMode(GameType.CREATIVE);RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true);h.assertTrue(SEHelper.getSESouls(p)==0 && SEHelper.getMiningProgress(host)==1,"Creative free mining can acquire the released executor");
        host.discard();second.discard();finish(h,p);
    }
    public static void burrowingNativeStaffAndBurning(GameTestHelper h,ServerPlayer p,ServerPlayer host){
        RainStormCases.prepare(h,p);ally(h,p,host);var pos=rock(h,host);for(int x=-1;x<=1;x++)for(int y=0;y<=2;y++)h.getLevel().setBlock(pos.offset(x,y-1,0),Blocks.STONE.defaultBlockState(),2);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
        RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true,AugmentAmplify.INSTANCE,AugmentBurning.INSTANCE);
        h.runAfterDelay(18,()->{
            h.assertTrue(h.getLevel().getBlockState(pos).isAir() && h.getLevel().getBlockState(pos.offset(1,0,0)).isAir(),"Original caster's Geomancy staff retains native area mining");
            h.assertTrue(RainStormCases.entities(h,ItemEntity.class).stream().anyMatch(e->e.getItem().is(Items.STONE)),"Native Burning really smelts excavated drops");host.discard();finish(h,p);
        });
    }
    private static void primeWand(GameTestHelper h,ServerPlayer player,ItemStack wand){for(int i=0;i<2;i++)wand.getItem().inventoryTick(wand,h.getLevel(),player,0,true);}
    public static void burrowingNativeWandHandoff(GameTestHelper h,ServerPlayer p,ServerPlayer host){
        RainStormCases.prepare(h,p);ally(h,p,host);var pos=rock(h,host);
        RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true);
        var other=new ItemStack(ModItems.GEO_STAFF.get());
        com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler.get(other).insertItem(new ItemStack(ModItems.QUAKING_FOCUS.get()));
        host.setItemInHand(InteractionHand.MAIN_HAND,other);primeWand(h,host,other);other.use(h.getLevel(),host,InteractionHand.MAIN_HAND);
        h.assertTrue(host.isUsingItem(),"Native held wand starts normally");ExecutionSessions.tick();
        h.assertTrue(!ExecutionSessions.isCasting(p) && SEHelper.getMiningProgress(host)==0 && SEHelper.getMiningPos(host)==null,"Switching to a different native spell cleans owned mining cracks");
        host.stopUsingItem();host.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        RainStormCases.cast(h,p,GlyphCatalog.BURROWING,host,p,true);
        var wand=new ItemStack(ModItems.GEO_STAFF.get());
        com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler.get(wand).insertItem(new ItemStack(ModItems.BURROWING_FOCUS.get()));
        host.setItemInHand(InteractionHand.MAIN_HAND,wand);primeWand(h,host,wand);wand.use(h.getLevel(),host,InteractionHand.MAIN_HAND);
        var spell=(BurrowingSpell)GlyphCatalog.BURROWING.focus().spell();var stats=com.Polarice3.Goety.utils.WandUtil.getStats(host,spell);
        spell.startSpell(h.getLevel(),host,wand,stats);dev.goetynouveau.platform.NativeBreathHooks.result(spell,h.getLevel(),host,wand,stats);
        int progress=SEHelper.getMiningProgress(host);h.assertTrue(progress>0,"Native wand really writes its own mining state");
        ExecutionSessions.tick();
        h.assertTrue(!ExecutionSessions.isCasting(p) && SEHelper.getMiningProgress(host)==progress && pos.equals(SEHelper.getMiningPos(host)),"Native Burrowing takes ownership without addon cleanup deleting its progress");
        host.stopUsingItem();BurrowingSpell.resetMiningProgress(h.getLevel(),host);host.discard();finish(h,p);
    }

}
