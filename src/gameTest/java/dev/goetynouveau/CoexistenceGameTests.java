package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsconflux.api.execution.ExecutionSessions;
import dev.arsconflux.api.glyph.SpellValidators;
import dev.arsconflux.api.projectile.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.*;
import java.util.List;

/** Exercises foreign glyphs through public Ars/Core APIs, without linking any Iron classes. */
@GameTestHolder("goety_nouveau_coexistence") @PrefixGameTestTemplate(false)
public final class CoexistenceGameTests {
    @GameTest(template="empty", timeoutTicks=40)
    public static void providerGlyphGroupsKeepCategories(GameTestHelper h) {
        PresentationCases.check(h, com.hollingsworth.arsnouveau.setup.registry.CreativeTabRegistry.COMPARE_SPELL_TYPE_NAME);
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeSkullSummoning(GameTestHelper h) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ConfluxSummons"));
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
        mana.setMaxMana(10000); mana.setMana(10000);
        SummonCases.nativeSkullsKeepTargetOwnerExpiryAndOneBatchCharge(h, player);
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void nativeGoetyBillsDoNotOverrideFollowingProviders(GameTestHelper h) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ConfluxAccounts"));
        var mana = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
        mana.setMaxMana(10000); mana.setMana(10000);
        EffectCases.nativeContinuationKeepsSourceAccount(h, player);
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void ironProjectileContinuesIntoGoetyVines(GameTestHelper h) {
        h.assertTrue(ModList.get().isLoaded("irons_nouveau"), "Real Iron provider is loaded");
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ConfluxMixed"));
        player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(-90); player.setXRot(0); player.setGameMode(GameType.CREATIVE);
        var iron = GlyphRegistry.getSpellPart(CarrierRegistry.id("irons_nouveau:glyph_firebolt"));
        for (var glyph : List.of(iron, GlyphCatalog.ENTANGLING)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(glyph.getGlyph()));
            glyph.getGlyph().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
        }
        var target = h.spawn(net.minecraft.world.entity.EntityType.COW, 7, 3, 2);
        target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100); target.setNoAi(true); target.setNoGravity(true);
        target.setPos(target.getX(), player.getEyeY() - target.getBbHeight() / 2, player.getZ());
        var recipe = List.of(MethodProjectile.INSTANCE, iron, GlyphCatalog.ENTANGLING);
        h.assertTrue(SpellValidators.validate(recipe).isEmpty(), "Native Iron payload accepts a Goety hit effect");
        var resolver = new SpellResolver(SpellContext.fromEntity(new Spell(recipe), player, ItemStack.EMPTY));
        h.assertTrue(resolver.onCast(ItemStack.EMPTY, h.getLevel()), "Mixed provider spell launches through normal Ars entrypoint");
        var projectiles = h.getLevel().getEntities(player, player.getBoundingBox().inflate(24),
                e -> e instanceof Projectile p && p.getOwner() == player && namespace(e).equals("irons_spellbooks"));
        h.assertTrue(projectiles.size() == 1, "One native Iron projectile carries the remaining Goety effect");
        var projectile = projectiles.get(0);
        for (int i = 0; i < 30 && !projectile.isRemoved(); ++i) { ++projectile.tickCount; projectile.tick(); ExecutionSessions.tick(); }
        var vines = h.getLevel().getEntitiesOfClass(com.Polarice3.Goety.common.entities.projectiles.EntangleVines.class,
                target.getBoundingBox().inflate(8));
        h.assertTrue(target.getHealth() < 100 && vines.size() == 1 && vines.get(0).getOwner() == player && vines.get(0).getTarget() == target,
                "Real Iron impact damages target and continues once into correctly attributed native Goety vines");
        vines.forEach(Entity::discard); projectiles.forEach(Entity::discard); ExecutionSessions.tick();
        target.discard(); player.discard(); h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=40)
    public static void ironAndGoetyShareCoreWithoutClaimingEachOthersProjectiles(GameTestHelper h) {
        h.assertTrue(ModList.get().isLoaded("irons_nouveau") && ModList.get().isLoaded("irons_spellbooks"), "Both real Iron mods are loaded alongside Goety");
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ConfluxStudent"));
        player.setPos(h.absoluteVec(new Vec3(2, 3, 2))); player.setYRot(-90); player.setXRot(0); player.setGameMode(GameType.CREATIVE);
        var iron = GlyphRegistry.getSpellPart(CarrierRegistry.id("irons_nouveau:glyph_firebolt"));
        h.assertTrue(iron instanceof ProjectileForm, "The real Iron Firebolt is registered as a Core payload");
        h.assertTrue(MethodProjectile.INSTANCE.compatibleAugments.contains(iron)
                && MethodProjectile.INSTANCE.compatibleAugments.contains(GlyphCatalog.SOUL_BOLT), "One Ars form accepts both providers");
        for (var glyph : List.of(GlyphCatalog.SOUL_BOLT, iron)) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(glyph.getGlyph()));
            glyph.getGlyph().use(h.getLevel(), player, InteractionHand.MAIN_HAND);
            h.assertTrue(SpellValidators.validate(List.of(MethodProjectile.INSTANCE, glyph)).isEmpty(), "A provider does not reject the other provider's valid recipe");
            var resolver = new SpellResolver(SpellContext.fromEntity(new Spell(MethodProjectile.INSTANCE, glyph), player, ItemStack.EMPTY));
            h.assertTrue(resolver.onCast(ItemStack.EMPTY, h.getLevel()), "Real Ars entrypoint accepts each learned provider glyph");
        }
        var nativeEntities = h.getLevel().getEntities(player, player.getBoundingBox().inflate(24),
                e -> e instanceof Projectile p && p.getOwner() == player);
        h.assertTrue(nativeEntities.stream().filter(e -> namespace(e).equals("goety")).count() == 1, "Exactly one native Goety projectile");
        h.assertTrue(nativeEntities.stream().filter(e -> namespace(e).equals("irons_spellbooks")).count() == 1, "Exactly one native Iron projectile");
        h.assertTrue(nativeEntities.stream().noneMatch(EntityProjectileSpell.class::isInstance), "Neither provider leaves a duplicated Ars carrier");
        h.assertTrue(!SpellValidators.validate(List.of(MethodProjectile.INSTANCE, GlyphCatalog.SOUL_BOLT, iron)).isEmpty(), "Two providers cannot both claim one projectile");
        nativeEntities.forEach(Entity::discard); ExecutionSessions.tick(); player.discard(); h.succeed();
    }
    private static String namespace(Entity entity) { return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getNamespace(); }
}
