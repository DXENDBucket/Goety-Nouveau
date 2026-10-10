package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.Fangs;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.context.TriggerGeometry;
import dev.arsconflux.api.glyph.SpellValidators;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.glyph.AugmentGoety;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.*;
import java.util.List;

/** Real native entities/global damage handlers and actual fire-charge interactions on both loaders. */
public final class FangCases {
    // Remain inside GameTest's already entity-ticking area; high above other fixtures.
    private static Vec3 scene(GameTestHelper h) { return h.absoluteVec(new Vec3(2.5, 8, 2.5)); }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        player.setPos(scene(h).add(-2, 0, -2)); player.setYRot(0); player.setXRot(0);
        player.setGameMode(GameType.SURVIVAL); player.setShiftKeyDown(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
    }
    private static SpellResolver effect(GameTestHelper h, ServerPlayer player, GoetyEffectGlyph glyph, AbstractAugment... augments) {
        var spell = new Spell(MethodTouch.INSTANCE, glyph);
        for (var augment : augments) spell = spell.add(augment);
        var result = new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
        result.spellContext.nextPart(); TriggerGeometry.write(result.spellContext, new Vec3(0, 0, 1));
        return result;
    }
    private static void floor(GameTestHelper h) {
        var start = BlockPos.containing(scene(h));
        for (int x = -6; x <= 6; ++x) for (int z = -6; z <= 25; ++z)
            h.getLevel().setBlockAndUpdate(start.offset(x, -1, z), Blocks.STONE.defaultBlockState());
    }
    private static void loadedScene(GameTestHelper h, Runnable check) {
        h.runAfterDelay(8, () -> { floor(h); check.run(); });
    }
    private static BlockHitResult hit(GameTestHelper h) {
        var point = scene(h); return new BlockHitResult(point, Direction.UP, BlockPos.containing(point).below(), false);
    }
    private static List<Fangs> fangs(GameTestHelper h, ServerPlayer player) {
        return h.getLevel().getEntitiesOfClass(Fangs.class, new AABB(scene(h), scene(h)).inflate(40), f -> f.getOwner() == player);
    }
    private static void cleanup(GameTestHelper h, ServerPlayer player) {
        fangs(h, player).forEach(Fangs::discard); player.discard();
    }
    public static void nativeFangGeometryAndBilling(GameTestHelper h, ServerPlayer player) {
        loadedScene(h, () -> nativeFangGeometryAndBillingLoaded(h, player));
    }
    private static void nativeFangGeometryAndBillingLoaded(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var cast = effect(h, player, GlyphCatalog.FANGS, AugmentGoety.EXTENSION, AugmentAmplify.INSTANCE);
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.FANGS.focus().spell(), List.of(AugmentGoety.EXTENSION)).amount();
            cast.onResolveEffect(h.getLevel(), hit(h)); var row = fangs(h, player);
            int count = GlyphCatalog.FANGS.focus().spell().defaultStats().getRange() + 1;
            h.assertTrue(row.size() == count && SEHelper.getSESouls(player) == 10000 - price,
                    "A complete extended row pays once: count=" + row.size() + "/" + count
                            + ", souls=" + SEHelper.getSESouls(player) + ", price=" + price);
            h.assertTrue(row.stream().anyMatch(f -> f.position().distanceTo(scene(h)) < .0001), "First fang is at the Ars hit point");
            h.assertTrue(row.stream().allMatch(f -> Math.abs(f.getX() - scene(h).x) < .0001), "Incoming direction controls the row independently of caster position");
            for (var fang : row) for (int i = 0; i < 60 && !fang.isRemoved(); ++i) fang.tick();
            h.assertTrue(row.stream().allMatch(Fangs::isRemoved), "Original warmup and native expiry remain intact");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.OMINOUS_STAFF.get()));
            var staffCast = effect(h, player, GlyphCatalog.FANGS);
            int before = SEHelper.getSESouls(player);
            int staffPrice = GoetyResources.quote(staffCast.spellContext, GlyphCatalog.FANGS.focus().spell()).amount();
            staffCast.onResolveEffect(h.getLevel(), hit(h));
            h.assertTrue(fangs(h, player).size() == 3 * (count - 1) && SEHelper.getSESouls(player) == before - staffPrice,
                    "Ominous staff creates three native rows for one actual charge");
            fangs(h, player).forEach(Fangs::discard);
            player.setShiftKeyDown(true); effect(h, player, GlyphCatalog.FANGS).onResolveEffect(h.getLevel(), hit(h));
            h.assertTrue(fangs(h, player).size() == 38, "Sneaking staff preserves all four native rings");
            fangs(h, player).forEach(Fangs::discard);
            player.setShiftKeyDown(false); player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            SEHelper.setSESouls(player, 0); effect(h, player, GlyphCatalog.FANGS).onResolveEffect(h.getLevel(), hit(h));
            h.assertTrue(fangs(h, player).isEmpty(), "Insufficient souls cannot spawn the batch");
            player.setGameMode(GameType.CREATIVE); effect(h, player, GlyphCatalog.FANGS).onResolveEffect(h.getLevel(), hit(h));
            h.assertTrue(!fangs(h, player).isEmpty() && SEHelper.getSESouls(player) == 0, "Creative fangs are free");
            fangs(h, player).forEach(Fangs::discard); player.setGameMode(GameType.SURVIVAL); SEHelper.setSESouls(player, 10000);
            var floating = scene(h).add(0, 20, 0);
            // Forge's test world can retain natural terrain above the template.
            // Explicitly clear every ground candidate, rather than assuming this is air.
            var clear = BlockPos.containing(floating);
            for (int x = -1; x <= 1; ++x) for (int z = -1; z <= 21; ++z) for (int y = -2; y <= 1; ++y)
                h.getLevel().setBlockAndUpdate(clear.offset(x, y, z), Blocks.AIR.defaultBlockState());
            effect(h, player, GlyphCatalog.FANGS).onResolveEffect(h.getLevel(), new BlockHitResult(floating, Direction.UP, BlockPos.containing(floating), false));
            h.assertTrue(fangs(h, player).isEmpty() && SEHelper.getSESouls(player) == 10000, "No supported ground means no spawn or fee: count=" + fangs(h, player).size()
                            + ", souls=" + SEHelper.getSESouls(player));
            h.succeed();
        } finally { cleanup(h, player); if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void nativeFangAbsorptionAndSoulGain(GameTestHelper h, ServerPlayer player) {
        loadedScene(h, () -> nativeFangAbsorptionAndSoulGainLoaded(h, player));
    }
    private static void nativeFangAbsorptionAndSoulGainLoaded(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        int previousGain = SpellConfig.FangGainSouls.get(); SpellConfig.FangGainSouls.set(4);
        LivingEntity target = null;
        try {
            prepare(h, player); player.setHealth(2);
            target = EntityType.ZOMBIE.create(h.getLevel()); target.setPos(scene(h));
            ((net.minecraft.world.entity.Mob)target).setNoAi(true); target.setNoGravity(true);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200); target.setHealth(200);
            h.getLevel().addFreshEntity(target);
            var augments = List.of(AugmentGoety.LIFE_ABSORB, AugmentGoety.SOUL_EATER, AugmentGoety.SOUL_EATER, AugmentBurning.INSTANCE);
            var cast = effect(h, player, GlyphCatalog.FANGS, augments.toArray(AbstractAugment[]::new));
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.FANGS.focus().spell(), augments).amount();
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var fang = fangs(h, player).stream().min(java.util.Comparator.comparingDouble(f -> f.position().distanceTo(scene(h)))).orElseThrow();
            h.assertTrue(fang.isAbsorbing() && fang.getSoulEater() == 2 && !fang.isTotemSpawned(), "Native flags use the real spell variant, not the totem variant");
            int afterCast = SEHelper.getSESouls(player);
            for (int i = 0; i < 8; ++i) fang.tick();
            h.assertTrue(afterCast == 10000 - price && target.getHealth() < 200 && player.getHealth() > 2,
                    "Native delayed damage and global Life Absorb event actually heal the owner: target=" + target.getHealth()
                            + ", owner=" + player.getHealth() + ", fang=" + fang.position() + ", targetPos=" + target.position());
            h.assertTrue(SEHelper.getSESouls(player) == afterCast + 8 && target.getRemainingFireTicks() >= 100,
                    "Native eligible successful hit grants souls and Burning ignites the target");
            h.assertTrue(SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FANGS, AugmentGoety.LIFE_ABSORB)).isEmpty(),
                    "A single Life Absorb is a valid native augment");
            h.assertTrue(!SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FANGS, AugmentGoety.LIFE_ABSORB, AugmentAmplify.INSTANCE)).isEmpty(),
                    "Editor rejects Absorb with Amplify");
            h.assertTrue(!SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FANGS, AugmentGoety.LIFE_ABSORB, AugmentGoety.LIFE_ABSORB)).isEmpty(),
                    "Absorb remains a binary native flag");
            h.assertTrue(!SpellValidators.validate(List.of(MethodTouch.INSTANCE, GlyphCatalog.FROST_NOVA, AugmentGoety.SOUL_EATER)).isEmpty(),
                    "A niche augment cannot silently modify an unrelated spell");
            fangs(h, player).forEach(Fangs::discard);
            int beforeInvalid = SEHelper.getSESouls(player);
            effect(h, player, GlyphCatalog.FANGS, AugmentGoety.LIFE_ABSORB, AugmentAmplify.INSTANCE).onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(fangs(h, player).isEmpty() && SEHelper.getSESouls(player) == beforeInvalid,
                    "Raw generated spells cannot bypass server augment validation");
            h.succeed();
        } finally {
            if (target != null) target.discard(); cleanup(h, player);
            SpellConfig.FangGainSouls.set(previousGain); if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous);
        }
    }
    public static void nativeIgniteInteractions(GameTestHelper h, ServerPlayer player) {
        loadedScene(h, () -> nativeIgniteInteractionsLoaded(h, player));
    }
    private static void nativeIgniteInteractionsLoaded(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); if (previous != ResourceSource.SOUL_ENERGY) GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        LivingEntity target = null;
        try {
            prepare(h, player); target = EntityType.COW.create(h.getLevel()); target.setPos(scene(h));
            ((net.minecraft.world.entity.Mob)target).setNoAi(true); h.getLevel().addFreshEntity(target);
            var cast = effect(h, player, GlyphCatalog.IGNITE, AugmentBurning.INSTANCE);
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.IGNITE.focus().spell(), List.of(AugmentBurning.INSTANCE)).amount();
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(target.getRemainingFireTicks() == 20 * (SpellConfig.IgniteFireSeconds.get() + 1)
                    && SEHelper.getSESouls(player) == 10000 - price, "Native entity ignition uses Burning and the actual trigger charge");
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price, "Unchanged ignition does not bill again");
            var block = BlockPos.containing(scene(h)).offset(3, 0, 0);
            h.getLevel().setBlockAndUpdate(block, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
            var blockHit = new BlockHitResult(Vec3.atCenterOf(block), Direction.UP, block, false);
            cast.onResolveEffect(h.getLevel(), blockHit);
            h.assertTrue(h.getLevel().getBlockState(block).getValue(CampfireBlock.LIT)
                    && SEHelper.getSESouls(player) == 10000 - 2 * price, "Native fire-charge interaction lights the Ars-selected campfire");
            SEHelper.setSESouls(player, 0); target.clearFire(); cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(target.getRemainingFireTicks() <= 0, "Insufficient resources refuse native ignition");
            player.setGameMode(GameType.CREATIVE); cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(target.getRemainingFireTicks() > 0 && SEHelper.getSESouls(player) == 0, "Creative native ignition remains free");
            h.succeed();
        } finally { if (target != null) target.discard(); cleanup(h, player); if (GoetyConfig.RESOURCE_SOURCE.get() != previous) GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
