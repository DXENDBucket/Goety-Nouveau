package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.projectiles.EntangleVines;
import com.Polarice3.Goety.common.entities.projectiles.IceChunk;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.glyph.GoetyEffectGlyph;
import dev.goetynouveau.platform.GoetyConfig;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.context.CastContexts;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.resource.ResourceAccount;
import dev.arsconflux.api.resource.ArsManaAccount;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

/** Tests native world behavior and per-focus trigger billing through the real Ars resolver. */
public final class EffectCases {
    private static final class AccountMarker extends AbstractEffect {
        private final ResourceAccount expected;
        private int hits;
        AccountMarker(ResourceAccount expected) { super(CarrierRegistry.id("goety_nouveau:glyph_test_account"), "Account"); this.expected = expected; }
        @Override public int getDefaultManaCost() { return 0; }
        @Override public int getCastingCost() { return 0; }
        @Override public SpellTier defaultTier() { return SpellTier.ONE; }
        @Override public SpellTier getConfigTier() { return SpellTier.ONE; }
        @Override public boolean isEnabled() { return true; }
        @Override protected java.util.Set<AbstractAugment> getCompatibleAugments() { return java.util.Set.of(); }
        @Override public void onResolve(HitResult hit, net.minecraft.world.level.Level world,
                net.minecraft.world.entity.LivingEntity caster, SpellStats stats, SpellContext context, SpellResolver resolver) {
            if (CastContext.of(context, hit, null).account() != expected)
                throw new AssertionError("Goety's native payment leaked into a following provider's inherited account");
            ++hits;
        }
    }
    public static void nativeContinuationKeepsSourceAccount(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player); player.setYRot(-90);
            var target = h.spawn(EntityType.COW, 7, 2, 2); target.setNoAi(true); target.setNoGravity(true);
            target.setPos(target.getX(), player.getEyeY() - target.getBbHeight() / 2, player.getZ());
            var base = new SpellContext(h.getLevel(), new Spell(MethodProjectile.INSTANCE), player, LivingCaster.from(player), ItemStack.EMPTY);
            var source = new ArsManaAccount(base.getCaster());
            var marker = new AccountMarker(source);
            var context = new SpellContext(h.getLevel(), new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT, GlyphCatalog.ENTANGLING, marker),
                    player, LivingCaster.from(player), ItemStack.EMPTY);
            CastContexts.bind(context, CastContext.of(context, null, source));
            var cast = new SpellResolver(context);
            int boltCost = GoetyResources.quote(context).amount();
            int vineCost = GoetyResources.quote(context, GlyphCatalog.ENTANGLING.focus().spell()).amount();
            h.assertTrue(cast.onCast(ItemStack.EMPTY, h.getLevel()), "Managed bolt plus effect launches with an inherited foreign source account");
            var bolt = h.getLevel().getEntitiesOfClass(com.Polarice3.Goety.common.entities.projectiles.SoulBolt.class,
                    player.getBoundingBox().inflate(16), entity -> entity.getOwner() == player).get(0);
            for (int i = 0; i < 30 && !bolt.isRemoved(); ++i) { ++bolt.tickCount; bolt.tick(); ExecutionSessions.tick(); }
            h.assertTrue(marker.hits == 1 && SEHelper.getSESouls(player) == 10000 - boltCost - vineCost,
                    "Native projectile and effect each pay souls; following effect retains original source account");
            var rootMarker = new AccountMarker(null);
            var root = new SpellResolver(new SpellContext(h.getLevel(), new Spell(MethodTouch.INSTANCE, GlyphCatalog.ENTANGLING, rootMarker),
                    player, LivingCaster.from(player), ItemStack.EMPTY));
            root.spellContext.nextPart(); root.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(rootMarker.hits == 1 && SEHelper.getSESouls(player) == 10000 - boltCost - 2 * vineCost,
                    "Root Goety effect does not publish a default soul account for later providers");
            h.getLevel().getEntitiesOfClass(EntangleVines.class, target.getBoundingBox().inflate(8)).forEach(Entity::discard);
            target.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer player, GoetyEffectGlyph glyph, AbstractAugment... augments) {
        var spell = new Spell(MethodTouch.INSTANCE, glyph);
        for (var augment : augments) spell = spell.add(augment);
        var resolver = new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
        resolver.spellContext.nextPart();
        return resolver;
    }
    private static void prepare(GameTestHelper h, ServerPlayer player) {
        player.setPos(h.absoluteVec(new Vec3(2, 2, 2)));
        player.setYRot(90); player.setXRot(0); // Face away from the Ars-selected targets.
        player.setGameMode(GameType.SURVIVAL);
        SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
    }
    public static void nativeIceTracksSelectedTarget(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var target = h.spawn(EntityType.COW, 7, 2, 2); target.setNoAi(true); target.setNoGravity(true);
            var cast = resolver(h, player, GlyphCatalog.ICE_CHUNK, AugmentAmplify.INSTANCE);
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.ICE_CHUNK.focus().spell()).amount();
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var ice = h.getLevel().getEntitiesOfClass(IceChunk.class, target.getBoundingBox().inflate(8)).get(0);
            // SpellEntity resolves owners by server UUID; this unregistered fake player is
            // deliberately absent from that lookup, so verify the persisted owner identity.
            h.assertTrue(player.getUUID().equals(ice.getOwnerId()) && ice.getTarget() == target, "Native ice keeps attribution and Ars target despite caster facing away");
            h.assertTrue(ice.getY() == target.getY() + 4 && ice.getExtraDamage() == 1, "Native height and Ars potency retained");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price, "Ice pays its own focus price exactly once on spawn");
            float health = target.getHealth();
            ice.hovering = 100;
            for (int i = 0; i < 20 && !ice.isRemoved(); ++i) ice.tick();
            h.assertTrue(ice.isRemoved() && target.getHealth() < health, "Native falling, collision and impact damage execute");
            SEHelper.setSESouls(player, 0);
            resolver(h, player, GlyphCatalog.ICE_CHUNK).onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(h.getLevel().getEntitiesOfClass(IceChunk.class, target.getBoundingBox().inflate(8)).isEmpty(), "Empty pool cannot summon another chunk");
            target.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void nativeVinesAndBlockTarget(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            prepare(h, player);
            var target = h.spawn(EntityType.COW, 7, 2, 2); target.setNoAi(true);
            var cast = resolver(h, player, GlyphCatalog.ENTANGLING, AugmentExtendTime.INSTANCE);
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.ENTANGLING.focus().spell()).amount();
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var vines = h.getLevel().getEntitiesOfClass(EntangleVines.class, target.getBoundingBox().inflate(8)).get(0);
            h.assertTrue(vines.getOwner() == player && vines.getTarget() == target && vines.getLifeSpan() == 100,
                    "Native vines bind selected target and map Extend Time to twenty extra ticks");
            for (int i = 0; i < 5; ++i) vines.tick();
            h.assertTrue(target.getActiveEffects().stream().anyMatch(effect -> effect.getDescriptionId().contains("tangled")),
                    "Native vine ticks actually restrain the target");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price, "Vines use native entangling cost");
            vines.discard(); target.discard();
            var point = h.absoluteVec(new Vec3(8.5, 2, 4.5));
            var hit = new BlockHitResult(point, Direction.UP, BlockPos.containing(point).below(), false);
            resolver(h, player, GlyphCatalog.ICE_CHUNK).onResolveEffect(h.getLevel(), hit);
            var ice = h.getLevel().getEntitiesOfClass(IceChunk.class, new AABB(point, point).inflate(8)).get(0);
            h.assertTrue(ice.position().distanceTo(point.add(0, 4, 0)) < 0.00001, "Block hit summons above actual Ars point, not caster look ray");
            ice.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void novaAndHealingRespectAllies(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get(); GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        var board = h.getLevel().getScoreboard();
        var team = board.addPlayerTeam("goety_" + player.getUUID().toString().substring(0, 8));
        try {
            prepare(h, player);
            var ally = h.spawn(EntityType.COW, 7, 2, 3); ally.setNoAi(true); ally.setNoGravity(true);
            var enemy = h.spawn(EntityType.COW, 8, 2, 2); enemy.setNoAi(true); enemy.setNoGravity(true);
            board.addPlayerToTeam(player.getScoreboardName(), team); board.addPlayerToTeam(ally.getScoreboardName(), team);
            var point = h.absoluteVec(new Vec3(7.5, 2, 2.5));
            var nova = resolver(h, player, GlyphCatalog.FROST_NOVA, AugmentExtendTime.INSTANCE);
            int price = GoetyResources.quote(nova.spellContext, GlyphCatalog.FROST_NOVA.focus().spell()).amount();
            float allyHealth = ally.getHealth(), enemyHealth = enemy.getHealth();
            nova.onResolveEffect(h.getLevel(), new BlockHitResult(point, Direction.UP, BlockPos.containing(point).below(), false));
            h.assertTrue(enemy.getHealth() < enemyHealth && ally.getHealth() == allyHealth, "Native nova damages enemies at Ars point and excludes allies");
            h.assertTrue(enemy.getLastDamageSource().getEntity() == player && !enemy.getActiveEffects().isEmpty(), "Native freeze keeps caster attribution and secondary effect");
            h.assertTrue(SEHelper.getSESouls(player) == 10000 - price, "Nova pays once at area trigger");
            int before = SEHelper.getSESouls(player);
            var heal = resolver(h, player, GlyphCatalog.SOUL_HEAL);
            heal.onResolveEffect(h.getLevel(), new EntityHitResult(enemy));
            h.assertTrue(SEHelper.getSESouls(player) == before, "Healing an enemy is invalid and uncharged");
            ally.setHealth(4);
            int healPrice = GoetyResources.quote(heal.spellContext, GlyphCatalog.SOUL_HEAL.focus().spell()).amount();
            resolver(h, player, GlyphCatalog.SOUL_HEAL).onResolveEffect(h.getLevel(), new EntityHitResult(ally));
            h.assertTrue(ally.getHealth() > 4 && player.getHealth() == player.getMaxHealth(), "Healing resolves on selected ally, not original caster");
            h.assertTrue(SEHelper.getSESouls(player) == before - healPrice, "Successful heal pays native healing cost");
            ally.setHealth(ally.getMaxHealth()); before = SEHelper.getSESouls(player);
            resolver(h, player, GlyphCatalog.SOUL_HEAL).onResolveEffect(h.getLevel(), new EntityHitResult(ally));
            h.assertTrue(SEHelper.getSESouls(player) == before, "Full-health target does not consume souls");
            ally.discard(); enemy.discard(); player.discard(); h.succeed();
        } finally { board.removePlayerTeam(team); GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
