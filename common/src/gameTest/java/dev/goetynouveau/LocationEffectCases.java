package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import com.Polarice3.Goety.common.entities.projectiles.IceBouquet;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import dev.goetynouveau.platform.NativeLocationHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Focused real native entities/events, without broad catalogue or client rendering claims. */
public final class LocationEffectCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-36,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var start=BlockPos.containing(scene(h));
        for(int x=-10;x<=10;x++) for(int z=-5;z<=5;z++) for(int y=-1;y<=4;y++)
            h.getLevel().setBlock(start.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-8,0,0)); p.setYRot(-90); p.setXRot(0); p.setGameMode(GameType.SURVIVAL);
        // SpellEntity resolves its synced owner UUID through the actual world's entity index.
        h.getLevel().addNewPlayer(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(point);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
        h.getLevel().addFreshEntity(cow); return cow;
    }
    private static SpellResolver resolver(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph); for(var a:augments) spell=spell.add(a);
        var result=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        result.spellContext.nextPart(); return result;
    }
    private static int resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,AbstractAugment... augments) {
        int before=SEHelper.getSESouls(p); var cast=resolver(h,p,glyph,augments);
        int cost=GoetyResources.quote(cast.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        cast.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(p.isCreative()?0:cost),"Successful "+glyph.focus()+" pays one focus charge");
        return cost;
    }
    private static BlockHitResult block(Vec3 point) {
        return new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(18));
    }
    private static void clear(GameTestHelper h) {
        entities(h,EarthFist.class).forEach(Entity::discard); entities(h,IceBouquet.class).forEach(Entity::discard);
    }
    public static void nativePlacementDamageExpiryAndStaff(GameTestHelper h,ServerPlayer p) {
        var previous=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("soil"+UUID.randomUUID().toString().substring(0,8));
        prepare(h,p); var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(0,0,1)); var other=cow(h,scene(h).add(5,0,0));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.EARTH_FIST,new EntityHitResult(enemy));
            var fist=entities(h,EarthFist.class).get(0);
            h.assertTrue(fist.getOwner()==p && fist.position().distanceTo(enemy.position())<.01,"Native fist uses selected target and original owner");
            h.assertTrue(enemy.getHealth()==200,"Fist does not become an instant replacement damage effect");
            for(int i=0;i<11;i++) { ++fist.tickCount; fist.tick(); }
            h.assertTrue(enemy.getHealth()<200 && enemy.getDeltaMovement().y>0 && ally.getHealth()==200,
                    "Native warmup produces real damage/lift and excludes allies");
            for(int i=0;i<40;i++) { ++fist.tickCount; fist.tick(); }
            h.assertTrue(fist.isRemoved(),"Native fist expires rather than leaving a persistent fake entity");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
            resolve(h,p,GlyphCatalog.EARTH_FIST,block(scene(h)));
            h.assertTrue(entities(h,EarthFist.class).size()==2,"Geomancy staff raises fists for the two enemies around the Ars point");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            enemy.invulnerableTime=0; enemy.setHealth(200);
            resolve(h,p,GlyphCatalog.GHOST_FIRE,block(scene(h)),AugmentExtendTime.INSTANCE);
            var flowers=entities(h,IceBouquet.class);
            h.assertTrue(flowers.size()==4 && flowers.stream().filter(IceBouquet::isCenter).count()==1,
                    "Ordinary native four-flower cluster keeps exactly one center");
            h.assertTrue(flowers.stream().allMatch(e->e.getOwner()==p && e.getLifeSpan()==120),"Extra duration is seconds added to native 100-tick life");
            for(int i=0;i<14;i++) for(var flower:flowers) { ++flower.tickCount; flower.tick(); }
            h.assertTrue(enemy.getHealth()<200 && ally.getHealth()==200,"Real native flowers damage enemies and spare allies");
            for(int i=0;i<112;i++) for(var flower:flowers) if(!flower.isRemoved()) { ++flower.tickCount; flower.tick(); }
            h.assertTrue(flowers.stream().allMatch(Entity::isRemoved),"Native ghost-fire flowers finish their lifetime");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));
            resolve(h,p,GlyphCatalog.GHOST_FIRE,block(scene(h)));
            int count=entities(h,IceBouquet.class).size();
            h.assertTrue(count==5 || count==9,"Necromancy staff retains native cross/full cluster variants");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));
            resolve(h,p,GlyphCatalog.GHOST_FIRE,block(scene(h)));
            count=entities(h,IceBouquet.class).size();
            h.assertTrue(NativeLocationHooks.ghostFireTower(p.getOffhandItem())?count==21:count==5 || count==9,
                    "Nameless staff retains the pinned platform's native tower or necromancy cluster");
            clear(h); SEHelper.setSESouls(p,0);
            resolver(h,p,GlyphCatalog.EARTH_FIST).onResolveEffect(h.getLevel(),block(scene(h)));
            resolver(h,p,GlyphCatalog.GHOST_FIRE).onResolveEffect(h.getLevel(),block(scene(h)));
            h.assertTrue(entities(h,EarthFist.class).isEmpty() && entities(h,IceBouquet.class).isEmpty(),"No resources means no delayed native entities");
            p.setGameMode(GameType.CREATIVE);
            resolve(h,p,GlyphCatalog.GHOST_FIRE,block(scene(h)));
            h.assertTrue(!entities(h,IceBouquet.class).isEmpty(),"Creative placement works with an empty soul pool");
            h.succeed();
        } finally { clear(h); enemy.discard(); ally.discard(); other.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void leechHealingAndNativeLaunch(GameTestHelper h,ServerPlayer p) {
        var previous=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("life"+UUID.randomUUID().toString().substring(0,8));
        prepare(h,p); var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(3,0,0));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            p.setHealth(10); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NECRO_STAFF.get()));
            resolve(h,p,GlyphCatalog.LEECHING,new EntityHitResult(enemy));
            h.assertTrue(enemy.getHealth()<200 && p.getHealth()>10,"Native leech damage event heals its true caster");
            h.assertTrue(enemy.hasEffect(MobEffects.WEAKNESS) && enemy.getEffect(MobEffects.WEAKNESS).getDuration()==60,
                    "Necromancy staff's successful leech weakens the victim");
            int before=SEHelper.getSESouls(p); float health=enemy.getHealth();
            enemy.setInvulnerable(true);
            resolver(h,p,GlyphCatalog.LEECHING).onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            resolver(h,p,GlyphCatalog.LEECHING).onResolveEffect(h.getLevel(),new EntityHitResult(ally));
            resolver(h,p,GlyphCatalog.LEECHING).onResolveEffect(h.getLevel(),block(scene(h)));
            h.assertTrue(SEHelper.getSESouls(p)==before && enemy.getHealth()==health && ally.getHealth()==200,
                    "Blocked damage, allies and non-living targets cannot produce a paid drain");
            enemy.setInvulnerable(false); enemy.invulnerableTime=0;
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            var launch=GlyphCatalog.LAUNCH.focus().spell();
            launch.SpellResult(h.getLevel(),p,ItemStack.EMPTY,launch.defaultStats());
            var nativeVelocity=p.getDeltaMovement(); p.setDeltaMovement(Vec3.ZERO);
            resolve(h,p,GlyphCatalog.LAUNCH,new EntityHitResult(p));
            h.assertTrue(p.getDeltaMovement().distanceTo(nativeVelocity)<.001,"Self launch matches this loader's native default spell");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.WIND_STAFF.get()));
            launch.SpellResult(h.getLevel(),p,p.getOffhandItem(),launch.defaultStats());
            double staffPower=p.getDeltaMovement().length();
            ally.setYRot(90); ally.setXRot(0); ally.fallDistance=5;
            resolve(h,p,GlyphCatalog.LAUNCH,new EntityHitResult(ally));
            h.assertTrue(Math.abs(ally.getDeltaMovement().length()-staffPower)<.001 && ally.getDeltaMovement().x<0 && ally.fallDistance==0,
                    "Caster's Wind staff acts on ally in ally's own facing direction and resets fall distance");
            before=SEHelper.getSESouls(p);
            resolver(h,p,GlyphCatalog.LAUNCH).onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            resolver(h,p,GlyphCatalog.LAUNCH).onResolveEffect(h.getLevel(),block(scene(h)));
            h.assertTrue(SEHelper.getSESouls(p)==before,"Enemy and block launch targets are uncharged");
            SEHelper.setSESouls(p,0); p.setDeltaMovement(Vec3.ZERO); health=enemy.getHealth();
            resolver(h,p,GlyphCatalog.LEECHING).onResolveEffect(h.getLevel(),new EntityHitResult(enemy));
            resolver(h,p,GlyphCatalog.LAUNCH).onResolveEffect(h.getLevel(),new EntityHitResult(p));
            h.assertTrue(enemy.getHealth()==health && p.getDeltaMovement().equals(Vec3.ZERO),"Exhausted resources cannot drain or launch");
            p.setGameMode(GameType.CREATIVE);
            resolve(h,p,GlyphCatalog.LAUNCH,new EntityHitResult(p));
            h.assertTrue(p.getDeltaMovement().length()>0,"Creative self launch is valid and free");
            h.succeed();
        } finally { enemy.discard(); ally.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
