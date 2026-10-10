package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Two real native world cases, not a parameter-only or full-catalogue rerun. */
public final class WeatherEffectCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-36,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var start=BlockPos.containing(scene(h));
        for(int x=-12;x<=12;x++) for(int z=-8;z<=8;z++) for(int y=-1;y<=8;y++)
            h.getLevel().setBlock(start.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-10,0,0)); p.setGameMode(GameType.SURVIVAL); h.getLevel().addNewPlayer(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(point);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
        h.getLevel().addFreshEntity(cow); return cow;
    }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph); for(var a:augments) spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        resolver.spellContext.nextPart(); int before=SEHelper.getSESouls(p);
        int cost=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(succeeds&&!p.isCreative()?cost:0),
                glyph.focus()+" pays once on success and never on failed/exhausted triggers");
    }
    private static BlockHitResult block(Vec3 point) {
        return new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(20));
    }
    private static void tick(Entity entity,int ticks) {
        for(int i=0;i<ticks&&!entity.isRemoved();i++) { ++entity.tickCount; entity.tick(); }
    }
    private static void clear(GameTestHelper h) { entities(h,AbstractSpellCloud.class).forEach(Entity::discard); }
    public static void thunderbolt(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("bolt"+UUID.randomUUID().toString().substring(0,8));
        var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(0,0,1)); var second=cow(h,scene(h).add(2,0,0));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        var rod=BlockPos.containing(scene(h)).offset(5,2,0);
        try {
            resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE);
            float expected=SpellConfig.ThunderboltDamage.get().floatValue()*WandUtil.damageMultiply()+1;
            h.assertTrue(Math.abs(enemy.getHealth()-(200-expected))<.001 && enemy.getLastHurtByMob()==p,
                    "Thunderbolt deals native amplified shock attributed to the real caster");
            resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(ally),false);
            h.assertTrue(ally.getHealth()==200,"Selected allies are not attacked");
            enemy.invulnerableTime=0; enemy.setInvulnerable(true);
            resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(enemy),false); enemy.setInvulnerable(false);
            h.getLevel().setBlock(rod,Blocks.LIGHTNING_ROD.defaultBlockState(),2);
            h.getLevel().getPoiManager().add(rod,h.getLevel().registryAccess()
                    .registryOrThrow(Registries.POINT_OF_INTEREST_TYPE).getHolderOrThrow(PoiTypes.LIGHTNING_ROD));
            h.assertTrue(BlockFinder.findLightningRod(h.getLevel(),enemy.blockPosition(),16).isPresent(),"Native rod search sees actual sky-exposed POI");
            float health=enemy.getHealth(); resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(enemy),true);
            h.assertTrue(enemy.getHealth()==health,"Nearby rod redirects native beam without victim damage");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
            resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(enemy),true);
            h.assertTrue(enemy.getHealth()<health && second.getHealth()<200 && ally.getHealth()==200,
                    "Storm staff bypasses rod and native chaining harms another enemy, excluding allies");
            h.getLevel().setBlock(rod,Blocks.AIR.defaultBlockState(),2);
            SEHelper.setSESouls(p,0); enemy.invulnerableTime=0; health=enemy.getHealth();
            resolve(h,p,GlyphCatalog.THUNDERBOLT,new EntityHitResult(enemy),false);
            h.assertTrue(enemy.getHealth()==health,"Exhausted account cannot strike");
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.THUNDERBOLT,block(scene(h).add(6,0,0)),true); h.succeed();
        } finally {
            h.getLevel().setBlock(rod,Blocks.AIR.defaultBlockState(),2);
            enemy.discard(); ally.discard(); second.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(old);
        }
    }
    public static void clouds(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        var board=h.getLevel().getScoreboard(); var team=board.addPlayerTeam("cloud"+UUID.randomUUID().toString().substring(0,8));
        var enemy=cow(h,scene(h)); var ally=cow(h,scene(h).add(1,0,0));
        board.addPlayerToTeam(p.getScoreboardName(),team); board.addPlayerToTeam(ally.getScoreboardName(),team);
        try {
            resolve(h,p,GlyphCatalog.HAIL,new EntityHitResult(enemy),true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE,AugmentExtendTime.INSTANCE);
            var hail=entities(h,HailCloud.class).get(0);
            h.assertTrue(hail.getOwner()==p && hail.getTarget()==enemy && hail.getRadius()==3 && hail.lifeSpan==200,
                    "Real hail cloud retains owner/target and native tick lifetime scaling");
            tick(hail,19); h.assertTrue(enemy.getHealth()==200,"No attack before native twenty-tick warmup");
            tick(hail,2); h.assertTrue(enemy.getHealth()<200 && ally.getHealth()==200
                    && enemy.hasEffect(NativeEffectHooks.freezing(1,0).getEffect()),"Native rain tick damages and freezes enemies only");
            tick(hail,205); h.assertTrue(hail.isRemoved(),"Hail expires after native active lifetime");
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            resolve(h,p,GlyphCatalog.HAIL,new EntityHitResult(enemy),true); hail=entities(h,HailCloud.class).get(0);
            Vec3 start=hail.position(); enemy.setPos(enemy.position().add(3,0,0)); tick(hail,1);
            h.assertTrue(hail.position().x>start.x && hail.isStaff(),"Frost staff cloud follows selected target using native movement");
            clear(h); enemy.setPos(scene(h)); enemy.invulnerableTime=0;
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.STORM_STAFF.get()));
            resolve(h,p,GlyphCatalog.MONSOON,block(scene(h)),true,AugmentExtendTime.INSTANCE);
            var storm=entities(h,MonsoonCloud.class).get(0);
            h.assertTrue(storm.getOwner()==p && storm.getRadius()==3 && storm.isStaff() && storm.lifeSpan==200,
                    "Native Monsoon gets storm staff area and active tick lifetime");
            float health=enemy.getHealth(); start=storm.position();
            // Exercise actual native rain ticks; reset vanilla immunity as time advances in this synchronous fixture.
            // At warmup + lifespan the native counter has just reached zero;
            // removal happens on its next tick, not on that last damaging tick.
            for(int i=0;i<225&&!storm.isRemoved();i++) { enemy.invulnerableTime=0; tick(storm,1); }
            h.assertTrue(storm.isRemoved() && enemy.getHealth()<health && ally.getHealth()==200 && storm.position().equals(start),
                    "Stationary native storm strikes enemies, filters allies, and expires");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.NAMELESS_STAFF.get()));
            resolve(h,p,GlyphCatalog.MONSOON,block(scene(h)),true);
            h.assertTrue(entities(h,MonsoonCloud.class).get(0).getLightningColor()==0xa7fc3e,"Nameless staff retains native lightning color");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            var roof=BlockPos.containing(scene(h)).above(3);
            for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) h.getLevel().setBlock(roof.offset(x,0,z),Blocks.STONE.defaultBlockState(),2);
            resolve(h,p,GlyphCatalog.HAIL,block(scene(h)),true); hail=entities(h,HailCloud.class).get(0);
            h.assertTrue(hail.getBoundingBox().maxY<roof.getY(),"Cloud fits below a low ceiling rather than inside it");
            clear(h);
            for(int x=-3;x<=3;x++) for(int z=-3;z<=3;z++) h.getLevel().setBlock(roof.offset(x,0,z),Blocks.AIR.defaultBlockState(),2);
            resolve(h,p,GlyphCatalog.HAIL,block(scene(h)),false,AugmentDurationDown.INSTANCE);
            h.getLevel().setBlock(BlockPos.containing(scene(h)),Blocks.STONE.defaultBlockState(),2);
            resolve(h,p,GlyphCatalog.MONSOON,block(scene(h)),false);
            h.assertTrue(entities(h,AbstractSpellCloud.class).isEmpty(),"No room or zero duration creates no charged cloud");
            h.getLevel().setBlock(BlockPos.containing(scene(h)),Blocks.AIR.defaultBlockState(),2);
            SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.HAIL,block(scene(h)),false);
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.HAIL,block(scene(h)),true); h.succeed();
        } finally { clear(h); enemy.discard(); ally.discard(); p.discard(); board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
}
