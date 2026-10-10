package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.Fangs;
import com.Polarice3.Goety.common.entities.projectiles.ModWitherSkull;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.glyph.SpellValidators;
import dev.arsconflux.api.projectile.*;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.*;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.*;

/** Complete native hit paths and real fang damage; both loaders share these cases. */
public final class SkullFeastCases {
    // Same preloaded horizontal area, above other tests so Feast cannot select their creatures.
    private static Vec3 scene(GameTestHelper h) {
        var origin = h.absoluteVec(new Vec3(2.5, 0, 2.5));
        return new Vec3(origin.x, h.getLevel().getMaxBuildHeight() - 32, origin.z);
    }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        var start = BlockPos.containing(scene(h));
        for (int x = -6; x <= 12; ++x) for (int z = -4; z <= 10; ++z) for (int y = 0; y <= 8; ++y)
            h.getLevel().setBlock(start.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        player.setPos(scene(h).add(-4, 0, 0)); player.setYRot(-90); player.setXRot(-15);
        player.setGameMode(GameType.SURVIVAL); player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
    }
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer player, Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
    }
    private static EntityProjectileSpell launch(GameTestHelper h, ServerPlayer player, Spell spell) {
        var cast = resolver(h, player, spell); cast.spellContext.nextPart();
        var carrier = new EntityProjectileSpell(h.getLevel(), cast);
        carrier.setDeltaMovement(player.getViewVector(1).scale(.75));
        h.assertTrue(ProjectileDispatch.convert(carrier), "Wither skull dispatches through Core");
        return carrier;
    }
    private static List<ModWitherSkull> skulls(GameTestHelper h, ServerPlayer player) {
        return h.getLevel().getEntitiesOfClass(ModWitherSkull.class, player.getBoundingBox().inflate(30), p -> p.getOwner() == player);
    }
    private static List<Fangs> fangs(GameTestHelper h, ServerPlayer player) {
        return h.getLevel().getEntitiesOfClass(Fangs.class, new AABB(scene(h), scene(h)).inflate(20), f -> f.getOwner() == player);
    }
    private static LivingEntity cow(GameTestHelper h, Vec3 point) {
        var target = EntityType.COW.create(h.getLevel()); target.setPos(point);
        target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); target.setHealth(200);
        h.getLevel().addFreshEntity(target); return target;
    }
    public static void witherNativeFlightSalvoAndHits(GameTestHelper h, ServerPlayer player) {
        h.runAfterDelay(8, () -> {
            var previous = GoetyConfig.RESOURCE_SOURCE.get();
            if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
            LivingEntity target = null;
            try {
                prepare(h, player);
                var spell = GlyphCatalog.WITHER_SKULL.focus().spell();
                spell.SpellResult(h.getLevel(), player, ItemStack.EMPTY, spell.defaultStats());
                var reference = skulls(h, player).get(0);
                var origin = reference.position();
                var positions = new ArrayList<Vec3>(); var velocities = new ArrayList<Vec3>(); var rotations = new ArrayList<Float>();
                // Run the reference first: native skulls may collide with another projectile.
                for (int i = 0; i < 8; ++i) {
                    reference.tick(); h.assertTrue(!reference.isRemoved(), "Native reference flies in an empty corridor");
                    positions.add(reference.position()); velocities.add(reference.getDeltaMovement()); rotations.add(reference.getYRot());
                }
                reference.discard();
                launch(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.WITHER_SKULL));
                var nativeShot = skulls(h, player).stream().filter(p -> ((ManagedBolt)p).goetyNouveau$managed()).findFirst().orElseThrow();
                h.assertTrue(nativeShot.position().distanceTo(origin) < .0001, "Skull retains native launch offset");
                for (int i = 0; i < 8; ++i) {
                    nativeShot.tick();
                    h.assertTrue(!nativeShot.isRemoved()
                            && nativeShot.position().distanceTo(positions.get(i)) < .0001
                            && nativeShot.getDeltaMovement().distanceTo(velocities.get(i)) < .000001
                            && Math.abs(nativeShot.getYRot() - rotations.get(i)) < .0001,
                            "Skull retains loader-specific native flight and rotation at tick " + i);
                }
                reference.discard(); nativeShot.discard(); ExecutionSessions.tick();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NETHER_STAFF.get()));
                player.setShiftKeyDown(true);
                var marker = new Marker();
                var recipe = new Spell(MethodProjectile.INSTANCE, GlyphCatalog.WITHER_SKULL,
                        AugmentAOE.INSTANCE, AugmentAmplify.INSTANCE, AugmentBurning.INSTANCE, marker);
                int price = GoetyResources.quote(resolver(h, player, recipe).spellContext, spell, List.of(AugmentBurning.INSTANCE)).amount();
                int before = SEHelper.getSESouls(player);
                launch(h, player, recipe); var batch = skulls(h, player);
                h.assertTrue(batch.size() == 3 && batch.stream().allMatch(p -> p.isDangerous() && p.getFiery() == 1 && p.getExtraDamage() == 1)
                        && SEHelper.getSESouls(player) == before - price, "Blue native staff salvo and Burning pay one batch fee");
                h.assertTrue(batch.get(0).getExplosionPower() == 2 && batch.get(1).getExplosionPower() == 3,
                        "Native primary and side-shot radius rules remain intact");
                target = cow(h, scene(h).add(8, 0, 0)); var first = batch.get(0); first.setPos(target.position());
                ((ProjectileHitAccess)first).goetyNouveau$hit(new EntityHitResult(target));
                ((ProjectileHitAccess)first).goetyNouveau$hit(new EntityHitResult(target));
                h.assertTrue(first.isRemoved() && target.getHealth() < 200 && target.hasEffect(MobEffects.WITHER)
                        && target.getEffect(MobEffects.WITHER).getAmplifier() == 1 && marker.hits == 1,
                        "Complete native hit applies Wither II and resumes Ars exactly once");
                target.discard(); target = cow(h, scene(h).add(8, 0, 3)); target.setHealth(1); player.setHealth(2);
                batch.get(1).setPos(target.position());
                ((ProjectileHitAccess)batch.get(1)).goetyNouveau$hit(new EntityHitResult(target));
                h.assertTrue(!target.isAlive() && player.getHealth() >= 7, "Native direct kill heals its actual owner");
                var block = BlockPos.containing(scene(h).add(8, 0, 7));
                batch.get(2).setPos(Vec3.atCenterOf(block));
                ((ProjectileHitAccess)batch.get(2)).goetyNouveau$hit(new BlockHitResult(Vec3.atCenterOf(block), Direction.UP, block, false));
                h.assertTrue(batch.get(2).isRemoved() && marker.hits == 3, "Full native block explosion completes its Ars continuation");
                ExecutionSessions.tick(); SEHelper.setSESouls(player, 0);
                launch(h, player, recipe); h.assertTrue(skulls(h, player).isEmpty(), "Denied skull salvo leaves no native side shots");
                player.setGameMode(GameType.CREATIVE); launch(h, player, recipe);
                h.assertTrue(skulls(h, player).size() == 3 && SEHelper.getSESouls(player) == 0, "Creative skull salvos are free");
                h.succeed();
            } finally {
                if (target != null) target.discard(); skulls(h, player).forEach(Entity::discard); player.discard(); ExecutionSessions.tick();
                if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous);
            }
        });
    }
    public static void repeatedWitherImpactsAndNativeExplosion(GameTestHelper h, ServerPlayer player) {
        h.runAfterDelay(8, () -> {
            var previous = GoetyConfig.RESOURCE_SOURCE.get();
            if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
            LivingEntity target = null;
            try {
                prepare(h, player); var marker = new Marker();
                var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.WITHER_SKULL, marker));
                int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.WITHER_SKULL.focus().spell()).amount();
                SEHelper.setSESouls(player, price * 2); cast.spellContext.nextPart();
                var orbit = new EntityOrbitProjectile(h.getLevel(), cast, player); orbit.setPos(orbit.getNextHitPosition());
                h.assertTrue(ProjectileDispatch.convert(orbit), "Orbit accepts the new native family");
                var skull = skulls(h, player).get(0); var pos = skull.position(); var velocity = skull.getDeltaMovement();
                skull.tick();
                h.assertTrue(skull.position().distanceTo(pos) < .00001 && skull.getDeltaMovement().distanceTo(velocity) < .00001,
                        "Native ticking cannot duplicate special-carrier movement");
                h.assertTrue(SEHelper.getSESouls(player) == price * 2, "Repeated skulls do not prepay future hits");
                target = cow(h, scene(h).add(8, 0, 0));
                var point = target.position().add(.7, 0, 0);
                var hit = new BlockHitResult(point, Direction.UP, BlockPos.containing(point), false);
                for (int i = 0; i < 3; ++i) {
                    target.invulnerableTime = 0;
                    ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(), hit);
                    h.assertTrue(marker.hits == Math.min(i + 1, 2), "Repeated explosions only continue after paid activation");
                    if (i < 2) h.assertTrue(!skull.isRemoved(), "A native block explosion does not discard its continuing Ars trajectory");
                }
                h.assertTrue(target.getHealth() < 200 && skull.isRemoved() && SEHelper.getSESouls(player) == 0,
                        "Block hits actually execute native explosion damage and resource exhaustion removes the batch");
                h.succeed();
            } finally {
                if (target != null) target.discard(); skulls(h, player).forEach(Entity::discard); player.discard(); ExecutionSessions.tick();
                if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous);
            }
        });
    }
    public static void feastAlliesDamageAndBatchBilling(GameTestHelper h, ServerPlayer player) {
        h.runAfterDelay(8, () -> {
            var previous = GoetyConfig.RESOURCE_SOURCE.get();
            if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
            var subjects = new ArrayList<LivingEntity>(); var board = h.getLevel().getScoreboard();
            var team = board.addPlayerTeam("feast" + UUID.randomUUID().toString().substring(0, 8));
            try {
                prepare(h, player); player.setHealth(2); player.setPos(scene(h).add(-20, 0, 0));
                var ground = BlockPos.containing(scene(h));
                for (int x = -3; x <= 6; ++x) for (int z = -3; z <= 6; ++z) {
                    h.getLevel().setBlockAndUpdate(ground.offset(x, -1, z), Blocks.STONE.defaultBlockState());
                    // Forge retains natural terrain above its below-ground GameTest origin.
                    for (int y = 0; y <= 3; ++y)
                        h.getLevel().setBlockAndUpdate(ground.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
                var enemy = cow(h, scene(h)); var ally = cow(h, scene(h).add(4, 0, 0)); subjects.addAll(List.of(enemy, ally));
                board.addPlayerToTeam(player.getScoreboardName(), team); board.addPlayerToTeam(ally.getScoreboardName(), team);
                var recipe = new Spell(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentBurning.INSTANCE, AugmentGoety.LIFE_ABSORB);
                var cast = resolver(h, player, recipe); cast.spellContext.nextPart();
                int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.FEAST.focus().spell(), List.of(AugmentBurning.INSTANCE, AugmentGoety.LIFE_ABSORB)).amount();
                cast.onResolveEffect(h.getLevel(), new EntityHitResult(enemy)); var batch = fangs(h, player);
                h.assertTrue(batch.size() == 1 && batch.get(0).position().distanceTo(enemy.position()) < .0001
                        && SEHelper.getSESouls(player) == 10000 - price, "Feast selects an Ars-centered enemy, excludes its ally, and pays once: count="
                                + batch.size() + ", souls=" + SEHelper.getSESouls(player) + ", price=" + price
                                + ", points=" + batch.stream().map(Entity::position).toList()
                                + ", ally=" + com.Polarice3.Goety.utils.MobUtil.areAllies(ally, player));
                for (int i = 0; i < 9; ++i) batch.get(0).tick();
                h.assertTrue(enemy.getHealth() < 200 && player.getHealth() > 2 && ally.getHealth() == 200 && enemy.getRemainingFireTicks() > 0,
                        "Native Feast fangs keep warmup, Burning and global absorption with actual owner attribution");
                batch.forEach(Entity::discard); enemy.discard();
                int before = SEHelper.getSESouls(player); cast.onResolveEffect(h.getLevel(), new EntityHitResult(ally));
                h.assertTrue(fangs(h, player).isEmpty() && SEHelper.getSESouls(player) == before, "An allies-only area is uncharged");
                enemy = cow(h, scene(h)); subjects.add(enemy);
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.OMINOUS_STAFF.get()));
                SEHelper.setSESouls(player, 0); cast.onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
                h.assertTrue(fangs(h, player).isEmpty(), "Unfunded Feast cannot spawn any part of a batch");
                player.setGameMode(GameType.CREATIVE); cast.onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
                h.assertTrue(fangs(h, player).size() == 6 && SEHelper.getSESouls(player) == 0, "Creative staff Feast spawns center and five native ring fangs for free");
                h.assertTrue(SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentGoety.LIFE_ABSORB)).isEmpty()
                        && SpellValidators.validate(List.of(MethodProjectile.INSTANCE, GlyphCatalog.WITHER_SKULL, AugmentBurning.INSTANCE)).isEmpty(), "New payloads accept supported augments");
                h.assertTrue(!SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentGoety.EXTENSION)).isEmpty()
                        && !SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentGoety.SOUL_EATER)).isEmpty()
                        && !SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentGoety.LIFE_ABSORB, AugmentAmplify.INSTANCE)).isEmpty(),
                        "Niche augments and Absorb/Amplify exclusion remain explicit");
                fangs(h, player).forEach(Entity::discard); player.setGameMode(GameType.SURVIVAL); SEHelper.setSESouls(player, 10000);
                var invalid = resolver(h, player, new Spell(MethodTouch.INSTANCE, GlyphCatalog.FEAST, AugmentGoety.EXTENSION));
                invalid.spellContext.nextPart(); invalid.onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
                h.assertTrue(fangs(h, player).isEmpty() && SEHelper.getSESouls(player) == 10000, "Raw recipes cannot bypass server validation");
                h.succeed();
            } finally {
                subjects.forEach(Entity::discard); fangs(h, player).forEach(Entity::discard); player.discard(); board.removePlayerTeam(team);
                if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous);
            }
        });
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker() { super(CarrierRegistry.id("goety_nouveau:glyph_skull_marker"), "Marker"); }
        public int getDefaultManaCost() { return 0; }
        public int getCastingCost() { return 0; }
        public SpellTier defaultTier() { return SpellTier.ONE; }
        public SpellTier getConfigTier() { return SpellTier.ONE; }
        public boolean isEnabled() { return true; }
        public Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }
        public void onResolve(HitResult hit, Level world, LivingEntity caster, SpellStats stats, SpellContext ctx, SpellResolver resolver) { ++hits; }
    }
}
