package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.SummonSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.api.*;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

public final class BreathingCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(4,0,4)); return new Vec3(pos.getX()+.5,h.getLevel().getMaxBuildHeight()-36,pos.getZ()+.5);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        // Reused GameTest worlds retain native test entities. They can win Goety's nearest
        // intersection query; isolate batches and clear only this disposable fixture arena.
        entities(h,LivingEntity.class).stream().filter(e->e!=p).forEach(Entity::discard);
        var start=BlockPos.containing(scene(h));
        for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++) for(int y=-1;y<=9;y++)
            h.getLevel().setBlock(start.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-10,0,0)); p.setGameMode(GameType.SURVIVAL); h.getLevel().addNewPlayer(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(point);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200); h.getLevel().addFreshEntity(cow); return cow;
    }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                AbstractAugment... augments) { resolve(h,p,glyph,hit,succeeds,null,augments); }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                LivingEntity owner,AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph); for(var a:augments) spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        resolver.spellContext.nextPart(); int before=SEHelper.getSESouls(p);
        int cost=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null)
                .withDamageOwner(owner==null?p:owner).withIncomingDirection(new Vec3(0,0,1)));
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(succeeds&&!p.isCreative()?cost:0),
                glyph.focus()+" charges once only on a successful trigger: "+before+" -> "+SEHelper.getSESouls(p)+", cost "+cost);
    }
    private static BlockHitResult block(Vec3 point) {
        return new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(25));
    }
    private static void tick(Entity entity,int ticks) {
        for(int i=0;i<ticks&&!entity.isRemoved();i++) { ++entity.tickCount; entity.tick(); }
    }
    private static void clear(GameTestHelper h) {
        entities(h,Owned.class).forEach(Entity::discard); entities(h,net.minecraft.world.entity.projectile.Projectile.class).forEach(Entity::discard);
        ExecutionSessions.tick();
    }
    private static void ally(GameTestHelper h, LivingEntity a, LivingEntity b) {
        var board=h.getLevel().getScoreboard();
        var team=board.getPlayersTeam(a.getScoreboardName());
        if(team==null) team=board.addPlayerTeam("breath_"+java.util.UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(a.getScoreboardName(),team);board.addPlayerToTeam(b.getScoreboardName(),team);
    }
    public static void nativeDamageHostAndOwnership(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);p.setPos(scene(h).add(-10,0,0));
        var host=cow(h,scene(h));host.setYRot(0);host.setXRot(0);ally(h,p,host);
        var owner=cow(h,scene(h).add(-8,0,0));
        var target=cow(h,scene(h).add(0,0,4));
        var friendly=cow(h,scene(h).add(0,0,2));ally(h,host,friendly);ally(h,p,host);
        try {
            for(var glyph:List.of(GlyphCatalog.FIRE_BREATH,GlyphCatalog.FROST_BREATH,GlyphCatalog.SWARM)) {
                target.invulnerableTime=0;target.setHealth(200);target.clearFire();
                resolve(h,p,glyph,new EntityHitResult(host),true,owner);
                h.assertTrue(target.getHealth()<200,glyph.focus()+" invokes its actual native result from the ally's eyes");
                h.assertTrue(target.getLastDamageSource()!=null&&target.getLastDamageSource().getEntity()==owner,
                        "Native damage type retains explicit damage owner");
                h.assertTrue(host.getHealth()==200&&friendly.getHealth()==200,"Executor and caster allies are excluded from breath damage");
            }
            var spell=(com.Polarice3.Goety.common.magic.BreathingSpell)GlyphCatalog.FIRE_BREATH.focus().spell();
            h.assertTrue(!spell.getBreathTarget(p,8).contains(target)&&GoetyBreathGeometry.current()==null,
                    "Outside the adapter scope, original Goety targeting still starts at the actual caster");
            p.setYRot(-90);p.setXRot(0);
            h.assertTrue(!spell.getBreathTarget(p,32).contains(host),"Ordinary Goety breath still excludes allies using its original default predicate");
            int before=SEHelper.getSESouls(p);
            for(var glyph:List.of(GlyphCatalog.FIRE_BREATH,GlyphCatalog.FROST_BREATH,GlyphCatalog.SWARM))
                resolve(h,p,glyph,new EntityHitResult(target),false);
            h.assertTrue(before==SEHelper.getSESouls(p),"Enemy recipients never create a sustained cast or spend souls");
        } finally { host.discard();p.discard();ExecutionSessions.tick(); }
        h.succeed();
    }
    public static void upkeepExpiryAndAllegiance(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var host=cow(h,scene(h));host.setYRot(0);ally(h,p,host);
        var target=cow(h,scene(h).add(0,0,4));
        int cost=GoetyResources.quote(new SpellContext(h.getLevel(),new Spell(MethodTouch.INSTANCE,GlyphCatalog.FIRE_BREATH),
                p,LivingCaster.from(p),p.getMainHandItem()),GlyphCatalog.FIRE_BREATH.focus().spell()).amount();
        SEHelper.setSESouls(p,cost*3);
        resolve(h,p,GlyphCatalog.FIRE_BREATH,new EntityHitResult(host),true);
        int first=SEHelper.getSESouls(p);float health=target.getHealth();
        h.runAtTickTime(12,()->{
            h.assertTrue(SEHelper.getSESouls(p)==first&&target.getHealth()<health,
                    "Native per-tick results / one-second payment: souls "+first+" -> "+SEHelper.getSESouls(p)
                            +", health "+health+" -> "+target.getHealth()+", session "+ExecutionSessions.isCasting(p));
        });
        h.runAtTickTime(23,()->{
            h.assertTrue(SEHelper.getSESouls(p)==first-cost,"Following second commits exactly one native focus charge");
            h.getLevel().getScoreboard().removePlayerFromTeam(host.getScoreboardName());
            ExecutionSessions.tick();
            h.assertTrue(!ExecutionSessions.isCasting(p),"Loss of ally status ends the sustained cast immediately");
            SEHelper.setSESouls(p,0);resolve(h,p,GlyphCatalog.FIRE_BREATH,new EntityHitResult(p),false);
            p.setGameMode(GameType.CREATIVE);
            int duration=GlyphCatalog.FIRE_BREATH.focus().spell() instanceof com.Polarice3.Goety.common.magic.BreathingSpell nativeSpell
                    ? nativeSpell.shotsNumber(p,GoetyResources.staff(p)) : 100;
            resolve(h,p,GlyphCatalog.FIRE_BREATH,new EntityHitResult(p),true);
            h.runAtTickTime(26+duration,()->{
                h.assertTrue(!ExecutionSessions.isCasting(p),"Finite native duration naturally ends the execution without another fee");
                resolve(h,p,GlyphCatalog.FIRE_BREATH,new EntityHitResult(p),true);
                p.discard();ExecutionSessions.tick();
                h.assertTrue(!ExecutionSessions.isCasting(p),"Caster removal cleans up the execution");h.succeed();
            });
        });
    }
}
