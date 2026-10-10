package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.undead.HauntedSkull;
import com.Polarice3.Goety.common.entities.projectiles.ModFireball;
import com.Polarice3.Goety.common.entities.projectiles.PoisonQuill;
import com.Polarice3.Goety.common.entities.util.Cushion;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.glyph.SpellValidators;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.Set;

/** Compare real native ticks and impacts, including resource-denied and repeated trajectories. */
public final class ProjectileBatchCases {
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer player, Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
    }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        player.setPos(h.absoluteVec(new Vec3(2, 4, 2))); player.setYRot(-90); player.setXRot(-15);
        player.setGameMode(GameType.SURVIVAL); player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
    }
    private static EntityProjectileSpell launch(GameTestHelper h, ServerPlayer player, Spell spell) {
        var cast = resolver(h, player, spell); cast.spellContext.nextPart();
        var carrier = new EntityProjectileSpell(h.getLevel(), cast);
        carrier.setDeltaMovement(player.getViewVector(1).scale(0.75));
        h.assertTrue(ProjectileDispatch.convert(carrier), "Native payload dispatches through public Core API");
        return carrier;
    }
    private static <T extends Entity> java.util.List<T> entities(GameTestHelper h, ServerPlayer player, Class<T> type) {
        return h.getLevel().getEntitiesOfClass(type, player.getBoundingBox().inflate(80));
    }
    private static LivingEntity cow(GameTestHelper h, int x, int z) {
        var target = h.spawn(EntityType.COW, x, 4, z); target.setNoAi(true); target.setNoGravity(true);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
        target.setHealth(200); return target;
    }
    public static void fireballNativeMotionAndSalvo(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var nativeSpell = GlyphCatalog.FIREBALL.focus().spell();
            nativeSpell.SpellResult(h.getLevel(), player, ItemStack.EMPTY, nativeSpell.defaultStats());
            var reference = entities(h, player, ModFireball.class).get(0);
            launch(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.FIREBALL));
            var fire = entities(h, player, ModFireball.class).stream().filter(p -> ((ManagedBolt)p).goetyNouveau$managed()).findFirst().orElseThrow();
            h.assertTrue(fire.position().distanceTo(reference.position()) < 1.0e-4, "Fireball retains native birth position");
            for (int i = 0; i < 8; ++i) {
                reference.tick(); fire.tick();
                h.assertTrue(fire.position().distanceTo(reference.position()) < 1.0e-4
                        && fire.getDeltaMovement().distanceTo(reference.getDeltaMovement()) < 1.0e-6
                        && Math.abs(fire.getYRot() - reference.getYRot()) < 1.0e-4,
                        "Default Fireball preserves actual native acceleration and rotation at tick " + i);
            }
            reference.discard(); fire.discard(); ExecutionSessions.tick();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NETHER_STAFF.get()));
            var marker = new Marker();
            var recipe = new Spell(MethodProjectile.INSTANCE, GlyphCatalog.FIREBALL, AugmentBurning.INSTANCE,
                    AugmentBurning.INSTANCE, AugmentAmplify.INSTANCE, marker);
            var cost = GoetyResources.quote(resolver(h, player, recipe).spellContext, nativeSpell,
                    java.util.List.of(AugmentBurning.INSTANCE, AugmentBurning.INSTANCE)).amount();
            int before = SEHelper.getSESouls(player);
            launch(h, player, recipe);
            var salvo = entities(h, player, ModFireball.class);
            h.assertTrue(salvo.size() == 3 && SEHelper.getSESouls(player) == before - cost,
                    "Native staff three-shot salvo pays once, including both Burning units");
            fire = salvo.get(0); var target = cow(h, 8, 2);
            ((ProjectileHitAccess)fire).goetyNouveau$hit(new EntityHitResult(target));
            ((ProjectileHitAccess)fire).goetyNouveau$hit(new EntityHitResult(target));
            h.assertTrue(fire.isRemoved() && target.getHealth() < 200 && target.getRemainingFireTicks() >= 300 && marker.hits == 1,
                    "Native full fireball hit ignites with Burning and continues once; removed projectiles cannot hit twice");
            salvo.forEach(Entity::discard); target.discard(); ExecutionSessions.tick();
            SEHelper.setSESouls(player, 0);
            launch(h, player, recipe);
            h.assertTrue(entities(h, player, ModFireball.class).isEmpty(), "An unaffordable salvo does not spawn side shots");
            player.setGameMode(GameType.CREATIVE);
            launch(h, player, recipe);
            h.assertTrue(entities(h, player, ModFireball.class).size() == 3 && SEHelper.getSESouls(player) == 0,
                    "Creative salvos and Burning are free");
            entities(h, player, ModFireball.class).forEach(Entity::discard); h.succeed();
        } finally { player.discard(); ExecutionSessions.tick(); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void quillNativePhysicsAndPiercing(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var nativeSpell = GlyphCatalog.POISON_DART.focus().spell();
            var reference = new PoisonQuill(h.getLevel(), player);
            var direction = player.getViewVector(1);
            reference.shoot(direction.x, direction.y, direction.z, nativeSpell.defaultStats().getVelocity(), 0);
            h.getLevel().addFreshEntity(reference);
            launch(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.POISON_DART));
            var quill = entities(h, player, PoisonQuill.class).stream().filter(p -> ((ManagedBolt)p).goetyNouveau$managed()).findFirst().orElseThrow();
            for (int i = 0; i < 8; ++i) {
                reference.tick(); quill.tick();
                h.assertTrue(quill.position().distanceTo(reference.position()) < 1.0e-4
                        && quill.getDeltaMovement().distanceTo(reference.getDeltaMovement()) < 1.0e-6,
                        "Poison Dart retains native arrow gravity and inertia at tick " + i);
            }
            reference.discard(); quill.discard(); ExecutionSessions.tick();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.WILD_STAFF.get()));
            var marker = new Marker();
            int before = SEHelper.getSESouls(player);
            launch(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.POISON_DART, AugmentAmplify.INSTANCE,
                    AugmentExtendTime.INSTANCE, marker));
            quill = entities(h, player, PoisonQuill.class).get(0);
            var targets = java.util.List.of(cow(h, 6, 2), cow(h, 7, 2), cow(h, 8, 2));
            var first = new EntityHitResult(targets.get(0));
            ((ProjectileHitAccess)quill).goetyNouveau$hit(first);
            ((ProjectileHitAccess)quill).goetyNouveau$hit(first);
            ((ProjectileHitAccess)quill).goetyNouveau$hit(new EntityHitResult(targets.get(1)));
            ((ProjectileHitAccess)quill).goetyNouveau$hit(new EntityHitResult(targets.get(2)));
            int duration = (2 + nativeSpell.defaultStats().getDuration() + 1) * 20;
            h.assertTrue(quill.isSpear() && quill.getSpearLevel() == 2 && marker.hits == 3
                    && targets.stream().allMatch(t -> t.getHealth() < 200 && t.hasEffect(MobEffects.POISON)
                    && t.getEffect(MobEffects.POISON).getDuration() == duration),
                    "Native spear pierces distinct targets, preserves extended poison, and does not repeat a tracked target");
            int price = GoetyResources.quote(resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.POISON_DART)).spellContext, nativeSpell).amount();
            h.assertTrue(SEHelper.getSESouls(player) == before - price, "Piercing impacts do not each charge the launch again");
            targets.forEach(Entity::discard); quill.discard(); h.succeed();
        } finally { player.discard(); ExecutionSessions.tick(); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void areaEffectsAndBurningSummons(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var target = cow(h, 8, 2);
            var shock = resolver(h, player, new Spell(MethodTouch.INSTANCE, GlyphCatalog.SHOCKWAVE, AugmentAmplify.INSTANCE));
            shock.spellContext.nextPart(); shock.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(target.getHealth() < 200 && player.getHealth() == player.getMaxHealth(),
                    "Shockwave native damage is centered on selected entity, not caster facing");
            var block = h.absolutePos(new BlockPos(8, 1, 5));
            var hit = new BlockHitResult(Vec3.atCenterOf(block), Direction.UP, block, false);
            var cushion = resolver(h, player, new Spell(MethodTouch.INSTANCE, GlyphCatalog.CUSHION, AugmentAOE.INSTANCE, AugmentExtendTime.INSTANCE));
            cushion.spellContext.nextPart(); cushion.onResolveEffect(h.getLevel(), hit);
            var pad = entities(h, player, Cushion.class).get(0);
            var defaults = GlyphCatalog.CUSHION.focus().spell().defaultStats();
            var reference = new Cushion(ModEntityType.CUSHION.get(), h.getLevel());
            h.assertTrue(pad.position().distanceTo(Vec3.atBottomCenterOf(block).add(0, 1, 0)) < 1.0e-6
                    && pad.getRadius() == (int)defaults.getRadius() + 1
                    && pad.lifeTicks == reference.lifeTicks + (defaults.getDuration() + 1) * 20,
                    "Cushion preserves native radius, lifespan and selected block location");
            pad.lifeTicks = 1; pad.tick(); h.assertTrue(pad.isRemoved(), "Native cushion expires without a separate integration timer");
            h.assertTrue(SpellValidators.validate(java.util.List.of(MethodProjectile.INSTANCE, GlyphCatalog.FIREBALL, AugmentBurning.INSTANCE)).isEmpty()
                    && !SpellValidators.validate(java.util.List.of(MethodProjectile.INSTANCE, GlyphCatalog.POISON_DART, AugmentBurning.INSTANCE)).isEmpty(),
                    "Burning validation uses the selected payload rather than broad carrier compatibility");
            var skullCast = resolver(h, player, new Spell(MethodTouch.INSTANCE, GlyphCatalog.HAUNTED_SKULL, AugmentBurning.INSTANCE));
            var price = GoetyResources.quote(skullCast.spellContext, GlyphCatalog.HAUNTED_SKULL.focus().spell(),
                    java.util.List.of(AugmentBurning.INSTANCE)).amount();
            int before = SEHelper.getSESouls(player);
            skullCast.spellContext.nextPart(); skullCast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var skulls = entities(h, player, HauntedSkull.class);
            h.assertTrue(!skulls.isEmpty() && skulls.stream().allMatch(s -> s.getBurning() == 1)
                    && SEHelper.getSESouls(player) == before - price, "Burning also modifies native summons and joins their successful batch charge");
            skulls.forEach(Entity::discard); target.discard(); h.succeed();
        } finally { player.discard(); ExecutionSessions.tick(); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void repeatedNewPayloadBilling(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var marker = new Marker();
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.POISON_DART, marker));
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.POISON_DART.focus().spell()).amount();
            SEHelper.setSESouls(player, price * 2);
            cast.spellContext.nextPart(); var orbit = new EntityOrbitProjectile(h.getLevel(), cast, player);
            orbit.setPos(orbit.getNextHitPosition()); ProjectileDispatch.convert(orbit);
            var quill = entities(h, player, PoisonQuill.class).get(0);
            h.assertTrue(SEHelper.getSESouls(player) == price * 2, "Special carrier does not prepay future hits");
            var block = h.absolutePos(new BlockPos(6, 1, 5));
            var hit = new BlockHitResult(Vec3.atCenterOf(block), Direction.UP, block, false);
            for (int i = 0; i < 3; ++i) {
                ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(), hit);
                h.assertTrue(marker.hits == Math.min(i + 1, 2), "Paid quill impacts continue once; empty resource stops repeated trajectory");
                if (i < 2) { quill.tick(); h.assertTrue(!quill.isRemoved(), "Native arrow ground latch must not destroy Ars-owned special motion"); }
            }
            h.assertTrue(quill.isRemoved() && SEHelper.getSESouls(player) == 0, "Denied repeated hit closes the native quill and trajectory");
            h.succeed();
        } finally { player.discard(); ExecutionSessions.tick(); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker() { super(CarrierRegistry.id("goety_nouveau:glyph_batch_marker"), "Marker"); }
        public int getDefaultManaCost() { return 0; }
        public int getCastingCost() { return 0; }
        public SpellTier defaultTier() { return SpellTier.ONE; }
        public SpellTier getConfigTier() { return SpellTier.ONE; }
        public boolean isEnabled() { return true; }
        public Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }
        public void onResolve(HitResult hit, Level world, LivingEntity caster, SpellStats stats, SpellContext ctx, SpellResolver resolver) { ++hits; }
    }
}
