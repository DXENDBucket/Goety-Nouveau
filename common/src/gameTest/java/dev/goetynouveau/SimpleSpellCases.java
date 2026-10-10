package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.MagicBolt;
import com.Polarice3.Goety.common.entities.projectiles.SpellLightningBolt;
import com.Polarice3.Goety.common.entities.util.MagicGround;
import com.Polarice3.Goety.common.entities.util.UpdraftBlast;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import dev.goetynouveau.platform.NativeEffectHooks;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.Set;

/** Native behavior checks, rather than mocks of spell damage or particle implementations. */
public final class SimpleSpellCases {
    private static SpellResolver effect(GameTestHelper h, ServerPlayer player, GoetyEffectGlyph glyph, AbstractAugment... augments) {
        var spell = new Spell(MethodTouch.INSTANCE, glyph);
        for (var augment : augments) spell = spell.add(augment);
        var result = resolver(h, player, spell);
        result.spellContext.nextPart();
        return result;
    }
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer player, Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
    }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(90); player.setXRot(0);
        player.setGameMode(GameType.SURVIVAL); SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
    }
    public static void nativeBuffsAndWeakening(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board = h.getLevel().getScoreboard();
        var team = board.addPlayerTeam("hide_" + player.getUUID().toString().substring(0, 8));
        var ally = h.spawn(EntityType.COW, 7, 3, 2); ally.setNoAi(true);
        var neighbor = h.spawn(EntityType.COW, 8, 3, 2); neighbor.setNoAi(true);
        var enemy = h.spawn(EntityType.COW, 7, 3, 4); enemy.setNoAi(true);
        board.addPlayerToTeam(player.getScoreboardName(), team);
        board.addPlayerToTeam(ally.getScoreboardName(), team);
        board.addPlayerToTeam(neighbor.getScoreboardName(), team);
        try {
            prepare(h, player);
            var iron = effect(h, player, GlyphCatalog.IRON_HIDE, AugmentAmplify.INSTANCE, AugmentExtendTime.INSTANCE);
            int price = GoetyResources.quote(iron.spellContext, GlyphCatalog.IRON_HIDE.focus().spell()).amount();
            iron.onResolveEffect(h.getLevel(), new EntityHitResult(ally));
            var hide = ally.getEffect(NativeEffectHooks.ironHide(1, 0).getEffect());
            h.assertTrue(hide != null && hide.getAmplifier() == 1 && hide.getDuration() == 2400 && !hide.isVisible(),
                    "Selected ally receives native Iron Hide with hidden particles and native minute duration");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price && !player.hasEffect(hide.getEffect()),
                    "Caster pays once without becoming recipient");
            effect(h, player, GlyphCatalog.IRON_HIDE).onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
            effect(h, player, GlyphCatalog.IRON_HIDE).onResolveEffect(h.getLevel(),
                    new BlockHitResult(ally.position(), Direction.UP, ally.blockPosition(), false));
            h.assertTrue(!enemy.hasEffect(hide.getEffect()) && SEHelper.getSESouls(player) == 10000 - price,
                    "Enemy/block buff targets do not consume souls");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FROST_STAFF.get()));
            var chill = effect(h, player, GlyphCatalog.CHILL_HIDE, AugmentAmplify.INSTANCE, AugmentExtendTime.INSTANCE);
            int chillPrice = GoetyResources.quote(chill.spellContext, GlyphCatalog.CHILL_HIDE.focus().spell()).amount();
            chill.onResolveEffect(h.getLevel(), new EntityHitResult(ally));
            var chilled = ally.getEffect(NativeEffectHooks.chillHide(1, 0).getEffect());
            h.assertTrue(chilled != null && chilled.getDuration() == 1800 && chilled.getAmplifier() == 1
                    && neighbor.hasEffect(chilled.getEffect()) && !enemy.hasEffect(chilled.getEffect()),
                    "Native Frost staff expands the selected protection to nearby allies, retaining forty-five-second units");
            var weakening = effect(h, player, GlyphCatalog.WEAKENING, AugmentAmplify.INSTANCE, AugmentExtendTime.INSTANCE);
            int weakPrice = GoetyResources.quote(weakening.spellContext, GlyphCatalog.WEAKENING.focus().spell()).amount();
            weakening.onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
            var sapped = enemy.getEffect(NativeEffectHooks.sapped(1, 0).getEffect());
            var weak = enemy.getEffect(MobEffects.WEAKNESS);
            h.assertTrue(sapped != null && weak != null && sapped.getAmplifier() == 5 && weak.getAmplifier() == 1
                    && weak.getDuration() == 120 && !ally.hasEffect(sapped.getEffect()) && !player.hasEffect(sapped.getEffect()),
                    "Weakening retains both native debuffs and filters using caster allegiance");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price - chillPrice - weakPrice,
                    "Every focus uses its own native successful-trigger charge");
            h.succeed();
        } finally {
            ally.discard(); neighbor.discard(); enemy.discard(); player.discard();
            board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous);
        }
    }
    public static void nativeLightningAndUpdraft(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var target = h.spawn(EntityType.COW, 8, 3, 2); target.setNoAi(true); target.setNoGravity(true);
            target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100); target.setHealth(100);
            var lightningCast = effect(h, player, GlyphCatalog.LIGHTNING, AugmentAmplify.INSTANCE);
            int lightningPrice = GoetyResources.quote(lightningCast.spellContext, GlyphCatalog.LIGHTNING.focus().spell()).amount();
            lightningCast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var lightning = h.getLevel().getEntitiesOfClass(SpellLightningBolt.class, target.getBoundingBox().inflate(2)).get(0);
            h.assertTrue(lightning.position().distanceTo(target.position()) < 1.0e-6 && player.getUUID().equals(lightning.getOwnerId()),
                    "Native lightning uses selected entity and original owner, independently of caster facing");
            lightning.tick();
            h.assertTrue(target.getHealth() < 100, "Native lightning tick actually damages the selected target");
            lightning.discard(); target.invulnerableTime = 0;
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.WIND_STAFF.get()));
            var draftCast = effect(h, player, GlyphCatalog.UPDRAFT, AugmentAmplify.INSTANCE, AugmentAOE.INSTANCE);
            int draftPrice = GoetyResources.quote(draftCast.spellContext, GlyphCatalog.UPDRAFT.focus().spell()).amount();
            draftCast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var draft = h.getLevel().getEntitiesOfClass(UpdraftBlast.class, target.getBoundingBox().inflate(2)).get(0);
            h.assertTrue(draft.getOwner() == player && draft.position().distanceTo(target.position()) < 1.0e-6
                    && draft.getAreaOfEffect() == 2.5F, "Native updraft receives selected position, owner and Wind staff/Ars area bonuses");
            float health = target.getHealth();
            draft.tickCount = 20; draft.tick();
            h.assertTrue(target.getHealth() < health && target.getDeltaMovement().y > 0,
                    "Native delayed updraft actually damages and lifts the target");
            draft.discard(); target.discard();
            var block = h.absolutePos(new BlockPos(8, 1, 5));
            var point = new Vec3(block.getX() + 0.5, block.getY() + 1, block.getZ() + 0.5);
            effect(h, player, GlyphCatalog.UPDRAFT).onResolveEffect(h.getLevel(), new BlockHitResult(point, Direction.UP, block, false));
            var ground = h.getLevel().getEntitiesOfClass(UpdraftBlast.class, new AABB(point, point).inflate(2)).get(0);
            h.assertTrue(ground.position().distanceTo(point) < 1.0e-6, "Block effect spawns above the selected surface rather than caster");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - lightningPrice - 2 * draftPrice,
                    "Lightning and both updraft casts pay native focus costs once");
            ground.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    private static final class Marker extends AbstractEffect {
        int hits;
        Marker() { super(CarrierRegistry.id("goety_nouveau:glyph_test_magic_continuation"), "Marker"); }
        public int getDefaultManaCost() { return 0; }
        public int getCastingCost() { return 0; }
        public SpellTier defaultTier() { return SpellTier.ONE; }
        public SpellTier getConfigTier() { return SpellTier.ONE; }
        public boolean isEnabled() { return true; }
        public Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }
        public void onResolve(HitResult hit, Level world, LivingEntity caster, SpellStats stats, SpellContext ctx, SpellResolver resolver) { ++hits; }
    }
    public static void nativeMagicBoltFlightAndFullImpacts(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player); player.setYRot(-90); player.setXRot(-15); player.setGameMode(GameType.CREATIVE);
            var spell = GlyphCatalog.MAGIC_BOLT.focus().spell();
            spell.SpellResult(h.getLevel(), player, ItemStack.EMPTY, spell.defaultStats());
            var reference = h.getLevel().getEntitiesOfClass(MagicBolt.class, player.getBoundingBox().inflate(32), e -> e.getOwner() == player).get(0);
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.MAGIC_BOLT));
            cast.spellContext.nextPart();
            var carrier = new EntityProjectileSpell(h.getLevel(), cast); carrier.setDeltaMovement(player.getViewVector(1).scale(0.75));
            h.assertTrue(ProjectileDispatch.convert(carrier), "Shared Core carrier converts to Magic Bolt");
            var bolt = h.getLevel().getEntitiesOfClass(MagicBolt.class, player.getBoundingBox().inflate(32), e -> ((ManagedBolt)e).goetyNouveau$managed()).get(0);
            h.assertTrue(bolt.position().distanceTo(reference.position()) < 1.0e-4 && bolt.getYRot() == reference.getYRot(),
                    "Magic Bolt preserves its own native birth offset and initially unturned model");
            for (int i = 0; i < 12; ++i) {
                reference.tick(); bolt.tick();
                h.assertTrue(bolt.position().distanceTo(reference.position()) < 1.0e-4
                        && bolt.getDeltaMovement().distanceTo(reference.getDeltaMovement()) < 1.0e-6
                        && Math.abs(bolt.getYRot() - reference.getYRot()) < 1.0e-4,
                        "Magic Bolt follows actual native acceleration and turn animation at tick " + i);
            }
            bolt.discard(); reference.discard(); ExecutionSessions.tick();
            player.setXRot(0); player.setGameMode(GameType.SURVIVAL);
            var target = h.spawn(EntityType.COW, 7, 3, 2); target.setNoAi(true); target.setNoGravity(true);
            target.setPos(target.getX(), player.getEyeY() - target.getBbHeight() / 2, player.getZ());
            var marker = new Marker();
            var shot = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.MAGIC_BOLT,
                    AugmentExtendTime.INSTANCE, AugmentAmplify.INSTANCE, marker));
            int price = GoetyResources.quote(shot.spellContext, spell).amount();
            h.assertTrue(shot.onCast(ItemStack.EMPTY, h.getLevel()), "Magic Bolt launches through actual Ars casting");
            bolt = h.getLevel().getEntitiesOfClass(MagicBolt.class, player.getBoundingBox().inflate(32), e -> ((ManagedBolt)e).goetyNouveau$managed()).get(0);
            h.assertTrue(bolt.getExtraDuration() == 1 && bolt.getExtraDamage() == 1, "Ars duration and potency map to Magic Bolt native parameters");
            for (int i = 0; i < 30 && !bolt.isRemoved(); ++i) { ++bolt.tickCount; bolt.tick(); ExecutionSessions.tick(); }
            h.assertTrue(target.getHealth() < target.getMaxHealth() && target.hasEffect(NativeEffectHooks.cursed(1).getEffect()) && marker.hits == 1,
                    "Native entity impact applies both damage and curse, then continues Ars once");
            var areas = h.getLevel().getEntitiesOfClass(MagicGround.class, target.getBoundingBox().inflate(16));
            h.assertTrue(areas.size() == 1 && areas.get(0).getDuration() == 200 && areas.get(0).getOwner() == player,
                    "Native entity impact also creates the attributed lingering magic ground with extended duration");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price, "Magic Bolt pays its own focus charge only on launch");
            areas.forEach(Entity::discard); target.discard();
            var wall = h.absolutePos(new BlockPos(6, 4, 2));
            // Ars retains shot inaccuracy; cover both sides of the integer-aligned player.
            for (int y = -1; y <= 1; ++y) for (int z = -1; z <= 1; ++z)
                h.getLevel().setBlockAndUpdate(wall.offset(0, y, z), Blocks.STONE.defaultBlockState());
            var blockMarker = new Marker();
            var blockShot = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.MAGIC_BOLT, blockMarker));
            blockShot.onCast(ItemStack.EMPTY, h.getLevel());
            bolt = h.getLevel().getEntitiesOfClass(MagicBolt.class, player.getBoundingBox().inflate(32), e -> ((ManagedBolt)e).goetyNouveau$managed()).get(0);
            for (int i = 0; i < 30 && !bolt.isRemoved(); ++i) { ++bolt.tickCount; bolt.tick(); ExecutionSessions.tick(); }
            areas = h.getLevel().getEntitiesOfClass(MagicGround.class, player.getBoundingBox().inflate(24));
            h.assertTrue(bolt.isRemoved() && blockMarker.hits == 1 && areas.size() == 1 && areas.get(0).getDuration() == 100,
                    "Native block impact creates magic ground and continues the Ars hit once too: removed=" + bolt.isRemoved()
                            + ", hits=" + blockMarker.hits + ", areas=" + areas.size() + ", position=" + bolt.position());
            areas.forEach(Entity::discard);
            for (int y = -1; y <= 1; ++y) for (int z = -1; z <= 1; ++z)
                h.getLevel().setBlockAndUpdate(wall.offset(0, y, z), Blocks.AIR.defaultBlockState());
            SEHelper.setSESouls(player, 0);
            resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.MAGIC_BOLT)).onCast(ItemStack.EMPTY, h.getLevel());
            h.assertTrue(h.getLevel().getEntitiesOfClass(MagicBolt.class, player.getBoundingBox().inflate(24)).isEmpty(), "Empty souls do not produce Magic Bolt");
            player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
