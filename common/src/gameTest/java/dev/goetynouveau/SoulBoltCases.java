package dev.goetynouveau;

import com.Polarice3.Goety.api.items.magic.ITotem;
import com.Polarice3.Goety.common.entities.projectiles.SoulBolt;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.entity.EntityOrbitProjectile;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsconflux.api.context.CastContexts;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.arsconflux.api.resource.ResourceTransactions;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.ManagedBolt;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** One shared behavioral suite for both Ars/Goety versions, without Iron in the classpath. */
public final class SoulBoltCases {
    private static final class Marker extends AbstractEffect {
        final AtomicInteger hits = new AtomicInteger();
        Marker() { super(CarrierRegistry.id("goety_nouveau:glyph_test_marker"), "Marker"); }
        public int getDefaultManaCost() { return 0; }
        public int getCastingCost() { return 0; }
        public SpellTier defaultTier() { return SpellTier.ONE; }
        public SpellTier getConfigTier() { return SpellTier.ONE; }
        public boolean isEnabled() { return true; }
        public Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }
        public void onResolveEntity(EntityHitResult hit, Level world, LivingEntity caster, SpellStats stats, SpellContext ctx, SpellResolver resolver) {
            if (CastContexts.current() == null || CastContexts.current().damageOwner() != caster)
                throw new IllegalStateException("Native-to-Ars continuation lost attribution");
            hits.incrementAndGet();
        }
    }
    private static SpellResolver resolver(GameTestHelper h, Player player, Spell spell) {
        return new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
    }
    private static List<SoulBolt> bolts(GameTestHelper h, Player player) {
        return h.getLevel().getEntitiesOfClass(SoulBolt.class, player.getBoundingBox().inflate(32), e -> e.getOwner() == player);
    }
    public static void nativeImpactAndContinuation(GameTestHelper h, net.minecraft.server.level.ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(-90); player.setXRot(0);
            player.setGameMode(GameType.SURVIVAL);
            SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 1000);
            var target = h.spawn(EntityType.COW, 7, 3, 2);
            target.setNoAi(true); target.setNoGravity(true);
            target.setPos(target.getX(), player.getEyeY() - target.getBbHeight() / 2, player.getZ());
            var marker = new Marker();
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT, AugmentAmplify.INSTANCE, marker));
            int price = GoetyResources.quote(cast.spellContext).amount();
            h.assertTrue(cast.onCast(ItemStack.EMPTY, h.getLevel()), "Real Ars casting pipeline launches Goety form");
            h.assertTrue(SEHelper.getSESouls(player) == 1000 - price, "Native souls paid exactly once at launch");
            var bolt = bolts(h, player).get(0);
            h.assertTrue(bolt.getExtraDamage() == GoetyResources.spell().defaultStats().getPotency() + 1, "Amplify maps to Goety potency");
            h.assertTrue(((ManagedBolt)bolt).goetyNouveau$managed() && !bolt.shouldBeSaved(), "Managed native entity is transient");
            h.assertTrue(h.getLevel().getEntitiesOfClass(EntityProjectileSpell.class, player.getBoundingBox().inflate(32), e -> e.getOwner() == player).isEmpty(), "Original carrier is not also spawned");
            float health = target.getHealth();
            for (int i = 0; i < 20 && marker.hits.get() == 0; i++) ExecutionSessions.tick();
            ExecutionSessions.tick();
            h.assertTrue(target.getHealth() < health, "Actual Goety onHitEntity damages target");
            h.assertTrue(marker.hits.get() == 1, "Remaining Ars effect resolves once with attribution");
            h.assertTrue(bolt.isRemoved(), "One-shot trajectory removes its native visual");
            h.assertTrue(SEHelper.getSESouls(player) == 1000 - price, "Impact does not charge again");
            target.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void storageBudgetAndCreative(GameTestHelper h, net.minecraft.server.level.ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(-90); player.setXRot(0);
            player.setGameMode(GameType.SURVIVAL); SEHelper.setSEActive(player, false);
            var totem = new ItemStack(ModItems.TOTEM_OF_SOULS.get());
            ITotem.setMaxSoulAmount(totem, 10000); ITotem.setSoulsamount(totem, 1000);
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, totem);
            var plain = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT));
            var quote = GoetyResources.quote(plain.spellContext);
            var again = GoetyResources.quote(plain.spellContext);
            h.assertTrue(quote.account().reservationKey().equals(again.account().reservationKey()), "Wrappers reserve the same native totem pool");
            h.assertTrue(quote.amount() > 0, "Test price is positive");
            ITotem.setSoulsamount(totem, quote.amount() * 3);
            var split = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT, AugmentSplit.INSTANCE, AugmentSplit.INSTANCE));
            h.assertTrue(split.onCast(ItemStack.EMPTY, h.getLevel()), "Native Ars symmetric scatter casts");
            h.assertTrue(bolts(h, player).size() == 3 && ITotem.currentSouls(totem) == 0, "Five-shot request degrades to three, paid from native totem");
            // Ars owns the reduced fan and its native shot inaccuracy; the provider never
            // truncates a pre-generated five-shot fan or rewrites individual directions.
            h.assertTrue(!ResourceTransactions.trigger(again, () -> { throw new AssertionError("Empty pool must not activate"); }), "Empty native pool denies trigger");
            bolts(h, player).forEach(e -> e.discard()); ExecutionSessions.tick();
            player.setGameMode(GameType.CREATIVE); player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
            h.assertTrue(plain.onCast(ItemStack.EMPTY, h.getLevel()) && bolts(h, player).size() == 1, "Creative can cast without totem or souls");
            bolts(h, player).forEach(e -> e.discard()); ExecutionSessions.tick(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void arsManaAlternativeAndFailedSpawn(GameTestHelper h, net.minecraft.server.level.ServerPlayer player,
                                                       java.util.function.IntConsumer setMana, java.util.function.IntSupplier balance) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.ARS_MANA);
        try {
            player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(-90); player.setXRot(0); player.setGameMode(GameType.SURVIVAL);
            SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 123);
            setMana.accept(1000);
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT));
            var quote = GoetyResources.quote(cast.spellContext);
            h.assertTrue(!quote.trigger(() -> false) && balance.getAsInt() == 1000, "Failed action is uncharged");
            h.assertTrue(cast.onCast(ItemStack.EMPTY, h.getLevel()), "Mana option launches native Goety projectile");
            h.assertTrue(balance.getAsInt() == 1000 - dev.arsconflux.platform.ArsAccess.spentCost(cast) - quote.amount(), "Upfront Ars cost plus one native quote, without duplicate billing");
            h.assertTrue(SEHelper.getSESouls(player) == 123, "Mana option leaves native soul pool untouched");
            bolts(h, player).forEach(e -> e.discard()); ExecutionSessions.tick(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void repeatedCarrierPaysAtEachImpact(GameTestHelper h, net.minecraft.server.level.ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setGameMode(GameType.SURVIVAL);
            SEHelper.setSEActive(player, true);
            var marker = new Marker();
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT, marker));
            int price = GoetyResources.quote(cast.spellContext).amount();
            SEHelper.setSESouls(player, price * 2);
            cast.spellContext.nextPart(); // The Ars cast method has been consumed before carrier creation.
            var orbit = new EntityOrbitProjectile(h.getLevel(), cast, player);
            orbit.setPos(orbit.getNextHitPosition());
            h.assertTrue(ProjectileDispatch.convert(orbit), "Actual Orbit carrier dispatches through Conflux");
            var bolt = bolts(h, player).get(0);
            h.assertTrue(SEHelper.getSESouls(player) == price * 2, "Repeating carrier does not prepay every future impact");
            for (int i = 0; i < 3; i++) {
                var target = h.spawn(EntityType.COW, 8, 3, 2);
                target.setNoAi(true); target.setNoGravity(true);
                var next = orbit.getNextHitPosition();
                target.setPos(next.x, next.y - target.getBbHeight() / 2, next.z);
                // Drive the real carrier's resolver callback directly: this case isolates repeated
                // billing/lifecycle from Orbit's native collision cooldown and orbital geometry.
                dev.arsconflux.api.glyph.ArsSpellAccess.resolver(orbit).onResolveEffect(h.getLevel(), new EntityHitResult(target));
                target.discard();
                h.assertTrue(marker.hits.get() == Math.min(i + 1, 2), "Each paid impact continues once; empty pool cannot continue: iteration=" + i + ", hits=" + marker.hits.get() + ", souls=" + SEHelper.getSESouls(player) + ", removed=" + bolt.isRemoved());
                if (i < 2) h.assertTrue(!bolt.isRemoved(), "Native discard is deferred while Orbit remains active");
            }
            h.assertTrue(SEHelper.getSESouls(player) == 0 && bolt.isRemoved(), "Empty pool stops repeating execution and removes native entity");
            player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
    public static void nativeLossEvent(GameTestHelper h, net.minecraft.server.level.ServerPlayer player, java.util.function.IntSupplier eventCount) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setGameMode(GameType.SURVIVAL); SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 1000);
            var cast = resolver(h, player, new Spell(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT));
            var quote = GoetyResources.quote(cast.spellContext);
            h.assertTrue(eventCount.getAsInt() == 0, "AI/scatter quotation emits no native loss events");
            var activation = GoetyResources.activation(quote);
            h.assertTrue(eventCount.getAsInt() == 1 && activation.amount() == 15, "Native loss event adjusts final reservation exactly once");
            h.assertTrue(activation.trigger(() -> true) && SEHelper.getSESouls(player) == 985, "Final native event price is spent without a second discount or event");
            h.assertTrue(eventCount.getAsInt() == 1, "Debit itself never fires the native event again");
            SEHelper.setSESouls(player, 14);
            h.assertTrue(!GoetyResources.activation(quote).trigger(() -> { throw new AssertionError("Cannot overspend event-adjusted quote"); }), "Increased event price is checked before activation");
            h.assertTrue(SEHelper.getSESouls(player) == 14, "Denied event-adjusted activation leaves storage unchanged");
            player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
    }
}
