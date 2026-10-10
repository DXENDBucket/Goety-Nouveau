package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Use an actual Ars book in the main hand; no native casting wand is needed for alternate casts. */
public final class VariantBehaviorCases {
    private static Vec3 scene(GameTestHelper h) {
        var pos=h.absoluteVec(new Vec3(3,0,3)); return new Vec3(pos.x,h.getLevel().getMaxBuildHeight()-48,pos.z);
    }
    private static void prepare(GameTestHelper h,ServerPlayer p) {
        p.setPos(scene(h)); p.setYRot(0); p.setYHeadRot(0); p.setXRot(0); p.setGameMode(GameType.SURVIVAL);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ItemsRegistry.NOVICE_SPELLBOOK.get()));
        p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); p.setShiftKeyDown(false);
        SEHelper.setSEActive(p,true); SEHelper.setSESouls(p,10000);
        var start=BlockPos.containing(scene(h));
        for(int x=-3;x<=3;x++) for(int z=-2;z<=15;z++) for(int y=-3;y<=5;y++)
            h.getLevel().setBlock(start.offset(x,y,z),Blocks.AIR.defaultBlockState(),2);
    }
    private static void cast(GameTestHelper h,ServerPlayer p,AbstractSpellPart payload) {
        var spell=new Spell(MethodProjectile.INSTANCE,payload);
        var cast=new SpellResolver(new SpellContext(h.getLevel(),spell,p,LivingCaster.from(p),p.getMainHandItem()));
        h.assertTrue(cast.onCast(p.getMainHandItem(),h.getLevel()),"Real Ars book pipeline casts "+payload.getRegistryName());
    }
    private static <T extends Projectile> List<T> shots(GameTestHelper h,ServerPlayer p,Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type,p.getBoundingBox().inflate(40),e->e.getOwner()==p);
    }
    private static void clear(GameTestHelper h,ServerPlayer p) {
        shots(h,p,Projectile.class).forEach(Entity::discard); ExecutionSessions.tick();
    }
    public static void bookOnlySneaking(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h,p);
            h.assertTrue(GoetyResources.staff(p).isEmpty(),"Fixture holds only Ars book, no Goety wand");
            for(boolean sneak:List.of(false,true)) {
                p.setShiftKeyDown(sneak);
                cast(h,p,GlyphCatalog.WITHER_SKULL);
                h.assertTrue(shots(h,p,ModWitherSkull.class).size()==1 && shots(h,p,ModWitherSkull.class).get(0).isDangerous()==sneak,
                        "Book-only sneaking selects blue skull; standing selects black");
                clear(h,p); cast(h,p,GlyphCatalog.LAVABALL);
                h.assertTrue(shots(h,p,Lavaball.class).size()==1 && shots(h,p,Lavaball.class).get(0).isDangerous()!=sneak,
                        "Book-only sneaking disables native lava griefing");
                clear(h,p); cast(h,p,GlyphCatalog.FIREBALL);
                h.assertTrue(shots(h,p,ModFireball.class).size()==1 && shots(h,p,ModFireball.class).get(0).isDangerous()!=sneak,
                        "Fireball uses the same Ars sneaking rule"); clear(h,p);
            }
            h.succeed();
        } finally { clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
    public static void offhandFrostStaffPierces(GameTestHelper h,ServerPlayer p) {
        var old=GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var targets=new ArrayList<LivingEntity>();
        try {
            prepare(h,p); cast(h,p,GlyphCatalog.ICE_SPIKE);
            var normal=shots(h,p,IceSpike.class).get(0); double normalSpeed=normal.getDeltaMovement().length();
            h.assertTrue(!(normal instanceof IceSpear),"Without staff the payload is a normal spike"); clear(h,p);
            p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(ModItems.FROST_STAFF.get()));
            cast(h,p,GlyphCatalog.ICE_SPIKE); var shot=shots(h,p,IceSpike.class).get(0);
            h.assertTrue(shot instanceof IceSpear && shot.getType()==com.Polarice3.Goety.common.entities.ModEntityType.ICE_SPEAR.get(),
                        "Main-hand Ars book and offhand Frost staff launch the real native spear type");
            h.assertTrue(Math.abs(shot.getDeltaMovement().length()/normalSpeed-1.5)<.02,"Frost staff retains its 50 percent speed bonus");
            for(int i=0;i<2;i++) {
                var cow=EntityType.COW.create(h.getLevel()); cow.setNoAi(true); cow.setNoGravity(true);
                cow.setPos(scene(h).add(0,0,4+i*4)); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); cow.setHealth(100);
                h.getLevel().addFreshEntity(cow); targets.add(cow);
            }
            for(int i=0;i<10 && !shot.isRemoved();i++) { ++shot.tickCount; shot.tick(); ExecutionSessions.tick(); }
            h.assertTrue(targets.stream().allMatch(t->t.getHealth()<100),"Natural flight pierces and damages both aligned targets");
            h.succeed();
        } finally { targets.forEach(Entity::discard); clear(h,p); p.discard(); GoetyConfig.RESOURCE_SOURCE.set(old); }
    }
}
