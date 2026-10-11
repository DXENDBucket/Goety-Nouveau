package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import java.util.*;

/** Check native entities and behavior, the actual resource account and remote executor geometry. */
public final class RainStormCases {
    static Vec3 scene(GameTestHelper h) {
        var p=h.absolutePos(new BlockPos(4,0,4));return new Vec3(p.getX()+.5,h.getLevel().getMaxBuildHeight()-48,p.getZ()+.5);
    }
    static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(64));
    }
    static void clear(GameTestHelper h,ServerPlayer p) {
        for(var e:h.getLevel().getEntities(p,new AABB(scene(h),scene(h)).inflate(64))) e.discard();
        ExecutionSessions.tick();
    }
    static void prepare(GameTestHelper h,ServerPlayer p) {
        clear(h,p);p.setPos(scene(h).add(-24,0,0));p.setNoGravity(true);h.getLevel().addNewPlayer(p);
        p.setYRot(-90);p.setXRot(0);p.setGameMode(GameType.SURVIVAL);
        p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true);SEHelper.setSESouls(p,100000);
        // Disposable high-altitude arena: earlier runs can leave ceiling/obstacles here.
        var center=BlockPos.containing(scene(h));
        for(int x=-16;x<=16;x++) for(int z=-16;z<=24;z++) for(int y=0;y<=25;y++)
            h.getLevel().setBlock(center.offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
    }
    static Mob host(GameTestHelper h,ServerPlayer p) {
        var host=EntityType.ZOMBIE.create(h.getLevel());host.setNoAi(true);host.setNoGravity(true);host.setPos(scene(h));
        h.getLevel().addFreshEntity(host);
        var board=h.getLevel().getScoreboard();var team=board.getPlayersTeam(p.getScoreboardName());
        if(team==null) team=board.addPlayerTeam("rain_"+UUID.randomUUID().toString().substring(0,8));
        board.addPlayerToTeam(p.getScoreboardName(),team);board.addPlayerToTeam(host.getScoreboardName(),team);
        var target=EntityType.COW.create(h.getLevel());target.setNoAi(true);target.setNoGravity(true);
        target.setPos(scene(h).add(0,0,8));h.getLevel().addFreshEntity(target);host.setTarget(target);return host;
    }
    static int cast(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,LivingEntity host,LivingEntity owner,boolean success,AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph);for(var a:augments) spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
        resolver.spellContext.nextPart();var hit=new EntityHitResult(host);
        CastContexts.bind(resolver.spellContext,CastContext.of(resolver.spellContext,hit,null).withDamageOwner(owner));
        int before=SEHelper.getSESouls(p),price=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(success&&!p.isCreative()?price:0),"First successful emission pays once: "+glyph.focus());
        return price;
    }
    public static void nativeRainGeometryAndVariants(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);
        try {
            for(var glyph:List.of(GlyphCatalog.ARROW_RAIN,GlyphCatalog.BLIZZARD,GlyphCatalog.METEOR_SHOWER)) {
                clear(h,p);var host=host(h,p);var owner=host.getTarget();
                cast(h,p,glyph,host,owner,true,AugmentAmplify.INSTANCE);
                var batch=entities(h,Projectile.class);h.assertTrue(!batch.isEmpty(),"Native rain emits actual projectiles");
                for(var shot:batch) {
                    h.assertTrue(shot.getOwner()==owner,"Native projectile keeps explicit damage owner");
                    h.assertTrue(shot.getY()>host.getY()+10 && Math.abs(shot.getX()-host.getX())<14,
                            "Native upper cloud: "+glyph.focus()+", shot="+shot.position()+", host="+host.position()+", target="+host.getTarget().position());
                }
                h.assertTrue(glyph!=GlyphCatalog.ARROW_RAIN || batch.get(0) instanceof RainArrow,"Native rain arrow");
                h.assertTrue(glyph!=GlyphCatalog.BLIZZARD || batch.get(0) instanceof IceSpike,"Native rain ice spike");
                h.assertTrue(glyph!=GlyphCatalog.METEOR_SHOWER || batch.get(0) instanceof Lavaball,"Native meteor");
                int before=SEHelper.getSESouls(p);cast(h,p,glyph,host.getTarget(),p,false);
                h.assertTrue(before==SEHelper.getSESouls(p),"An enemy cannot sustain the spell");
                h.assertTrue(GoetyNativeGeometry.current()==null,"Native scope is restored after the callback");
            }
            clear(h,p);var host=host(h,p);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            cast(h,p,GlyphCatalog.BLIZZARD,host,p,true);
            h.assertTrue(entities(h,IceSpear.class).size()>0,"Native frost staff switches blizzard to rain spears");
            clear(h,p);host=host(h,p);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);p.setShiftKeyDown(true);
            cast(h,p,GlyphCatalog.METEOR_SHOWER,host,p,true);
            h.assertTrue(entities(h,Lavaball.class).stream().allMatch(e->!e.isDangerous()),"Book-only sneak cast retains native harmless-to-blocks meteors");
            p.setShiftKeyDown(false);
            clear(h,p);host=host(h,p);p.setGameMode(GameType.CREATIVE);
            cast(h,p,GlyphCatalog.ARROW_RAIN,host,p,true,AugmentBurning.INSTANCE);
            h.assertTrue(entities(h,RainArrow.class).stream().allMatch(Entity::isOnFire),"Burning modifies the actual native arrows");
        } finally {p.discard();clear(h,p);}
        h.succeed();
    }
    public static void upkeepAndAllegiance(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var host=host(h,p);
        int fee=cast(h,p,GlyphCatalog.ARROW_RAIN,host,p,true);
        SEHelper.setSESouls(p,fee*2);int[] atStart={fee*2};
        h.runAfterDelay(22,()->{
            h.assertTrue(SEHelper.getSESouls(p)==atStart[0]-fee,"Next native group of 20 emissions pays once");
            var board=h.getLevel().getScoreboard();var team=board.getPlayersTeam(host.getScoreboardName());
            board.removePlayerFromTeam(host.getScoreboardName(),team);
            entities(h,Projectile.class).forEach(Entity::discard);
            h.runAfterDelay(2,()->{
                h.assertTrue(entities(h,Projectile.class).isEmpty(),"Changing alliance stops further rain immediately");
                int before=SEHelper.getSESouls(p);
                h.runAfterDelay(22,()->{h.assertTrue(SEHelper.getSESouls(p)==before,"Stopped channel has no upkeep");p.discard();clear(h,p);h.succeed();});
            });
        });
    }
    public static void meteorCadenceAndEmptyAccount(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);var host=host(h,p);
        cast(h,p,GlyphCatalog.METEOR_SHOWER,host,p,true);
        var first=new java.util.HashSet<UUID>();entities(h,Projectile.class).forEach(e->first.add(e.getUUID()));
        int interval=Math.max(1,((com.Polarice3.Goety.api.magic.IChargingSpell)GlyphCatalog.METEOR_SHOWER.focus().spell()).Cooldown());
        int balance=SEHelper.getSESouls(p);
        h.runAfterDelay(Math.max(0,interval-1),()->{
            h.assertTrue(entities(h,Projectile.class).stream().allMatch(e->first.contains(e.getUUID())),"No extra native salvo before its interval");
            h.runAfterDelay(2,()->{
                h.assertTrue(entities(h,Projectile.class).stream().anyMatch(e->!first.contains(e.getUUID())),"Next meteor salvo uses the native interval");
                h.assertTrue(SEHelper.getSESouls(p)==balance,"Native ever-charge period counts emissions, not elapsed ticks");
                SEHelper.setSESouls(p,0);entities(h,Projectile.class).forEach(Entity::discard);
                h.runAfterDelay(2,()->{
                    h.assertTrue(!ExecutionSessions.isCasting(p) && entities(h,Projectile.class).isEmpty(),"An empty account ends sustained meteor casting");
                    p.discard();clear(h,p);h.succeed();
                });
            });
        });
    }
    public static void nativeCloudFlightAndFreeze(GameTestHelper h,ServerPlayer p) {
        prepare(h,p);p.setPos(scene(h));p.setYRot(0);p.setXRot(0);
        var spell=new Spell(MethodProjectile.INSTANCE,GlyphCatalog.ICE_STORM);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),ItemStack.EMPTY));
        resolver.spellContext.nextPart();var carrier=new EntityProjectileSpell(h.getLevel(),resolver);
        carrier.setDeltaMovement(new Vec3(0,0,.75));
        int before=SEHelper.getSESouls(p),fee=GoetyResources.quote(resolver.spellContext,GlyphCatalog.ICE_STORM.focus().spell()).amount();
        h.assertTrue(ProjectileDispatch.convert(carrier),"Native moving cloud is an Ars projectile replacement");
        var storms=entities(h,IceStorm.class);h.assertTrue(storms.size()==1 && storms.get(0).getOwner()==p,"Native storm and original owner");
        h.assertTrue(SEHelper.getSESouls(p)==before-fee,"Native storm pays one launch charge");
        var storm=storms.get(0);var origin=storm.position();var size=storm.getSize();
        var target=EntityType.COW.create(h.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(origin);
        h.getLevel().addFreshEntity(target);storm.tick();
        h.assertTrue(target.getHealth()<target.getMaxHealth(),"Native cloud applies its area damage and freezing");
        h.assertTrue(storm.getSize()>size,"Native cloud grows: "+size+" -> "+storm.getSize());
        for(int i=0;i<2;i++) storm.tick();
        h.assertTrue(storm.position().distanceTo(origin)>.001,"Native cloud flies after its native initial acceleration");
        storm.tickCount=101;storm.tick();h.assertTrue(storm.isRemoved(),"Native cloud expiry is preserved");
        p.discard();clear(h,p);h.succeed();
    }
}
