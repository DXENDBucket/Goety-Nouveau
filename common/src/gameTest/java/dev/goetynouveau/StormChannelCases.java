package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.ModFireball;
import com.Polarice3.Goety.common.entities.util.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.AugmentBurning;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class StormChannelCases {
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        RainStormCases.prepare(h,p);var center=BlockPos.containing(RainStormCases.scene(h));
        for(int x=-25;x<=25;x++) for(int z=-25;z<=35;z++) h.getLevel().setBlock(center.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
    }
    public static void bombardmentHostMotionAndCadence(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var host=RainStormCases.host(h,p);host.setYRot(0);host.setXRot(0);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NETHER_STAFF.get()));p.setShiftKeyDown(true);
        var direction=p.getDeltaMovement();
        int fee=RainStormCases.cast(h,p,GlyphCatalog.BOMBARDMENT,host,p,true,AugmentAmplify.INSTANCE,AugmentBurning.INSTANCE);
        var first=RainStormCases.entities(h,ModFireball.class);
        h.assertTrue(first.size()==3 && first.stream().allMatch(e->e.getOwner()==p && !e.isDangerous() && e.getFiery()>0),"Native staff fireball batch, owner and book-only sneak/burning");
        for(var shot:first) h.assertTrue(shot.position().distanceTo(host.getEyePosition())<4 && shot.position().distanceTo(p.position())>12,"Bombardment starts at executor's eyes");
        h.assertTrue(host.getDeltaMovement().y>0 && p.getDeltaMovement().equals(direction),"Only actual executor receives native hovering");
        var ids=new HashSet<java.util.UUID>();first.forEach(e->ids.add(e.getUUID()));
        int interval=Math.max(1,((com.Polarice3.Goety.api.magic.IChargingSpell)GlyphCatalog.BOMBARDMENT.focus().spell()).Cooldown());
        int balance=SEHelper.getSESouls(p);
        h.runAfterDelay(Math.max(0,interval-1),()->{
            h.assertTrue(RainStormCases.entities(h,ModFireball.class).stream().allMatch(e->ids.contains(e.getUUID())),"No salvo before native cadence");
            h.runAfterDelay(2,()->{
                h.assertTrue(RainStormCases.entities(h,ModFireball.class).stream().anyMatch(e->!ids.contains(e.getUUID())),"Native second bombardment salvo");
                h.assertTrue(SEHelper.getSESouls(p)==balance,"Slow salvos count native ever-charge emissions");
                SEHelper.setSESouls(p,0);RainStormCases.entities(h,ModFireball.class).forEach(Entity::discard);
                h.runAfterDelay(2,()->{h.assertTrue(!ExecutionSessions.isCasting(p),"Empty account stops hovering and salvos");p.discard();RainStormCases.clear(h,p);h.succeed();});
            });
        });
    }
    public static void nativeThunderstormVariantsAndOwner(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var host=RainStormCases.host(h,p);var owner=host.getTarget();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
        RainStormCases.cast(h,p,GlyphCatalog.THUNDERSTORM,host,owner,true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE);
        var traps=RainStormCases.entities(h,MagicLightningTrap.class);
        h.assertTrue(traps.size()==2 && traps.stream().allMatch(t->t.getOwner()==owner && t.getDamage()>0 && t.radius()>1),"Native storm staff creates two augmented traps with explicit owner");
        for(var t:traps) h.assertTrue(Math.abs(t.getX()-host.getX())<=17 && Math.abs(t.getZ()-host.getTarget().getZ())<=17,"Native first phase uses executor target");
        RainStormCases.cast(h,p,GlyphCatalog.THUNDERSTORM,host.getTarget(),p,false);
        RainStormCases.clear(h,p);host=RainStormCases.host(h,p);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.VOID_STAFF.get()));
        RainStormCases.cast(h,p,GlyphCatalog.THUNDERSTORM,host,p,true);
        h.assertTrue(RainStormCases.entities(h,MagicLightningTrap.class).stream().anyMatch(t->t instanceof VoidLightningTrap),"Native Void staff retains void lightning traps");
        // Normal channel pulses use native useSpell; check upkeep and natural stopping in real world ticks.
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        int balance=SEHelper.getSESouls(p);
        var finalHost=host;
        h.runAfterDelay(22,()->{
            h.assertTrue(SEHelper.getSESouls(p)<balance && RainStormCases.entities(h,MagicLightningTrap.class).size()>1,"Native continuous traps and ongoing payment");
            h.getLevel().getScoreboard().removePlayerFromTeam(finalHost.getScoreboardName());ExecutionSessions.tick();
            h.assertTrue(!ExecutionSessions.isCasting(p),"Loss of ally status stops storm generation");
            p.discard();RainStormCases.clear(h,p);h.succeed();
        });
    }
    public static void tridentLocationWarmupAndNativeStrike(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var point=RainStormCases.scene(h).add(0,0,8);
        var caster=new SpellResolver(new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.TRIDENT_STORM),p,LivingCaster.from(p),ItemStack.EMPTY));
        caster.spellContext.nextPart();var hit=new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
        CastContexts.bind(caster.spellContext,CastContext.of(caster.spellContext,hit,null));
        int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(caster.spellContext,GlyphCatalog.TRIDENT_STORM.focus().spell()).amount();
        caster.onResolveEffect(h.getLevel(),hit);
        var tridents=RainStormCases.entities(h,TridentStorm.class);
        h.assertTrue(tridents.size()==4 && SEHelper.getSESouls(p)==before-fee,"Native minor layout is one paid group");
        for(var trident:tridents) h.assertTrue(trident.getOwner()==p && trident.position().distanceTo(point)<6,"Grounded tridents surround selected location, not caster");
        var trident=tridents.get(0);
        h.assertTrue(((ArsTriggeredEntity)trident).goetyNouveau$arsTriggered(),"Ars provenance is synced before preactivation ticks");
        var saved=new net.minecraft.nbt.CompoundTag();trident.addAdditionalSaveData(saved);
        var restored=com.Polarice3.Goety.common.entities.ModEntityType.TRIDENT_STORM.get().create(h.getLevel());restored.readAdditionalSaveData(saved);
        h.assertTrue(((ArsTriggeredEntity)restored).goetyNouveau$arsTriggered(),"Ars provenance survives native save/load");
        var victim=EntityType.COW.create(h.getLevel());victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(trident.position().add(0,2,0));h.getLevel().addFreshEntity(victim);
        for(int i=0;i<trident.getWarmUp()+trident.delay+6&&!trident.isRemoved();i++) trident.tick();
        h.assertTrue(victim.getHealth()<victim.getMaxHealth(),"Native warmup animation culminates in actual trident lightning damage");
        for(int i=0;i<80&&!trident.isRemoved();i++) trident.tick();h.assertTrue(trident.isRemoved(),"Native trident expires");
        // An ordinary Goety startSpell still creates an unmarked trap that cancels without a held wand.
        RainStormCases.clear(h,p);
        GlyphCatalog.TRIDENT_STORM.focus().spell().startSpell(h.getLevel(),p,ItemStack.EMPTY,GlyphCatalog.TRIDENT_STORM.focus().spell().defaultStats());
        var nativeOnly=RainStormCases.entities(h,TridentStorm.class).get(0);nativeOnly.tick();
        h.assertTrue(!((ArsTriggeredEntity)nativeOnly).goetyNouveau$arsTriggered() && nativeOnly.isRemoved(),"Native wand preactivation cancellation is unchanged");
        RainStormCases.clear(h,p);p.setGameMode(GameType.CREATIVE);SEHelper.setSESouls(p,0);
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
        var host=RainStormCases.host(h,p);RainStormCases.cast(h,p,GlyphCatalog.TRIDENT_STORM,host,p,true);
        h.assertTrue(RainStormCases.entities(h,TridentStorm.class).size()>=8 && SEHelper.getSESouls(p)==0,"Native matching staff layout and creative bypass");
        h.assertTrue(GoetyNativeGeometry.current()==null,"Native context does not leak");
        p.discard();RainStormCases.clear(h,p);h.succeed();
    }
}
