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
