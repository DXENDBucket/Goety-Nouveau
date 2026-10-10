package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.neutral.Volcano;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.*;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Three focused native lifecycle cases; no full-catalogue rerun or client visual claim. */
public final class HazardEffectCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(4,0,4)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-36,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        var start=BlockPos.containing(scene(h));
        for(int x=-12;x<=12;x++) for(int z=-12;z<=12;z++) for(int y=-1;y<=12;y++)
            h.getLevel().setBlock(start.offset(x,y,z),y==-1?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
        p.setPos(scene(h).add(-11,0,0)); p.setGameMode(GameType.SURVIVAL); h.getLevel().addNewPlayer(p);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
    }
    private static LivingEntity cow(GameTestHelper h,Vec3 point) {
        var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setPos(point);
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
        h.getLevel().addFreshEntity(cow); return cow;
    }
    private static void resolve(GameTestHelper h,ServerPlayer p,GoetyEffectGlyph glyph,HitResult hit,boolean succeeds,
                                AbstractAugment... augments) {
        var spell=new Spell(MethodTouch.INSTANCE,glyph); for(var a:augments) spell=spell.add(a);
        var resolver=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        resolver.spellContext.nextPart();
        int before=SEHelper.getSESouls(p);
        int cost=GoetyResources.quote(resolver.spellContext,glyph.focus().spell(),List.of(augments)).amount();
        resolver.onResolveEffect(h.getLevel(),hit);
        h.assertTrue(SEHelper.getSESouls(p)==before-(succeeds&&!p.isCreative()?cost:0),
                glyph.focus()+" commits one charge only on a successful batch");
    }
    private static BlockHitResult block(Vec3 point) {
        return new BlockHitResult(point,Direction.UP,BlockPos.containing(point).below(),false);
    }
    private static <T extends Entity> List<T> entities(GameTestHelper h,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,new AABB(scene(h),scene(h)).inflate(24));
    }
    private static void tick(Entity entity,int ticks) {
        for(int i=0;i<ticks&&!entity.isRemoved();i++) { ++entity.tickCount; entity.tick(); }
    }
    private static void clear(GameTestHelper h) {
        for(var type:List.of(Volcano.class,Pyroclast.class,BioMine.class,AcidPool.class,ViciousTooth.class,ViciousPike.class))
            entities(h,type).forEach(Entity::discard);
    }
    public static void volcano(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        try {
            resolve(h,p,GlyphCatalog.ERUPTION,block(scene(h)),true,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE,AugmentBurning.INSTANCE);
            var volcano=entities(h,Volcano.class).get(0);
            h.assertTrue(volcano.getTrueOwner()==p && volcano.getLifeSpan()==6 && volcano.getPotency()==1
                    && volcano.getFlaming()==1 && volcano.getExplosionPower()==3.5F,"Native owner and all payload parameters retained");
            h.assertTrue(entities(h,Pyroclast.class).isEmpty(),"Volcano must emerge before firing");
            tick(volcano,80);
            var rocks=entities(h,Pyroclast.class);
            h.assertTrue(!rocks.isEmpty() && rocks.stream().allMatch(e->e.getOwner()==p && e.getPotency()==1
                    && e.getFlaming()==1 && e.getDeltaMovement().y>0),"Native eruption creates upward owned burning rocks");
            tick(volcano,160); h.assertTrue(volcano.isRemoved(),"Volcano descends and expires natively");
            clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.GEO_STAFF.get()));
            resolve(h,p,GlyphCatalog.ERUPTION,block(scene(h)),true,AugmentExtendTime.INSTANCE);
            h.assertTrue(entities(h,Volcano.class).size()==3 && entities(h,Volcano.class).stream().allMatch(e->e.getLifeSpan()==12),
                    "Geomancy creates three extended volcanoes for one charge");
            clear(h); resolve(h,p,GlyphCatalog.ERUPTION,block(scene(h)),false,AugmentDurationDown.INSTANCE);
            h.assertTrue(entities(h,Volcano.class).isEmpty(),"Zero-duration eruption is uncharged");
            SEHelper.setSESouls(p,0); resolve(h,p,GlyphCatalog.ERUPTION,block(scene(h)),false);
            h.assertTrue(entities(h,Volcano.class).isEmpty(),"No resource means no generated hazard");
            p.setGameMode(GameType.CREATIVE); resolve(h,p,GlyphCatalog.ERUPTION,block(scene(h)),true);
            h.assertTrue(entities(h,Volcano.class).size()==3,"Creative batch is free"); h.succeed();
        } finally { clear(h); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void mines(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        LivingEntity enemy=null;
        try {
            resolve(h,p,GlyphCatalog.BIO_MINE,block(scene(h)),true,AugmentExtendTime.INSTANCE,AugmentAmplify.INSTANCE,AugmentAOE.INSTANCE);
            var mines=entities(h,BioMine.class);
            h.assertTrue(mines.size()==4 && mines.stream().allMatch(m->m.getOwner()==p && m.getExtraDuration()==20
                    && m.getLifeTicks()==120 && m.getExtraRadius()==1 && m.getExtraDamage()==1),"Four native mines retain owner and stats");
            var mine=mines.get(0); mine.setPos(scene(h)); enemy=cow(h,scene(h).add(1,0,0));
            tick(mine,19); h.assertTrue(!mine.isRemoved() && enemy.getHealth()==200,"Native arming delay prevents immediate damage");
            tick(mine,1); h.assertTrue(mine.isRemoved() && enemy.getHealth()<200,"Armed native mine explodes near enemy");
            var pools=entities(h,AcidPool.class);
            h.assertTrue(!pools.isEmpty() && pools.stream().allMatch(a->a.getOwner()==p && a.getDuration()==80),
                    "Real explosion leaves owned extended acid pools");
            for(var pool:pools) tick(pool,110);
            h.assertTrue(pools.stream().allMatch(Entity::isRemoved),"Native acid aftermath expires");
            enemy.discard(); clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.ABYSS_STAFF.get()));
            resolve(h,p,GlyphCatalog.BIO_MINE,block(scene(h)),true);
            h.assertTrue(entities(h,BioMine.class).size()==8,"Abyss staff doubles native mine batch"); h.succeed();
        } finally { if(enemy!=null) enemy.discard(); clear(h); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void teeth(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY); prepare(h,p);
        LivingEntity enemy=null;
        try {
            var roof=BlockPos.containing(scene(h)).above(6); h.getLevel().setBlock(roof,Blocks.STONE.defaultBlockState(),2);
            resolve(h,p,GlyphCatalog.TEETH,block(scene(h)),true,AugmentAmplify.INSTANCE);
            var tooth=entities(h,ViciousTooth.class).get(0);
            h.assertTrue(tooth.getOwner()==p && tooth.getBoundingBox().maxY<roof.getY(),"Tooth fits below actual ceiling");
            double y=tooth.getY(); tick(tooth,20);
            h.assertTrue(!tooth.isRemoved() && tooth.getY()==y,"Native tooth hovers before falling");
            enemy=cow(h,scene(h)); tick(tooth,90);
            h.assertTrue(tooth.isRemoved() && enemy.getHealth()<200,"Native falling tooth collides and damages");
            var pikes=entities(h,ViciousPike.class);
            h.assertTrue(pikes.size()==4 && pikes.stream().allMatch(e->e.getOwner()==p),"Native hit creates all four owned follow-up pikes");
            for(var pike:pikes) tick(pike,80);
            h.assertTrue(pikes.stream().allMatch(Entity::isRemoved),"Follow-up pikes also expire");
            enemy.discard(); clear(h); p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.OMINOUS_STAFF.get()));
            p.setShiftKeyDown(true); resolve(h,p,GlyphCatalog.TEETH,block(scene(h)),true);
            h.assertTrue(entities(h,ViciousTooth.class).size()==13,"Sneaking Ominous staff retains thirteen-tooth formation");
            clear(h); p.setShiftKeyDown(false);
            for(int i=0;i<=9;i++) h.getLevel().setBlock(BlockPos.containing(scene(h)).above(i),Blocks.STONE.defaultBlockState(),2);
            p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            resolve(h,p,GlyphCatalog.TEETH,block(scene(h)),false);
            h.assertTrue(entities(h,ViciousTooth.class).isEmpty(),"No room yields no charge or embedded tooth"); h.succeed();
        } finally { if(enemy!=null) enemy.discard(); p.setShiftKeyDown(false); clear(h); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
}
