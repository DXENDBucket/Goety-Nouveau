package dev.goetynouveau;

import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.validation.StandardSpellValidator;
import dev.arsconflux.api.projectile.ProjectileDispatch;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.glyph.*;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import java.util.List;

/** Matches Ars' palette filter, which checks only errors on the newly appended glyph. */
public final class BurningValidationCases {
    public static void check(GameTestHelper h, ServerPlayer player) {
        var validator = new StandardSpellValidator(false);
        for (var part : GlyphCatalog.ALL) {
            if (!(part instanceof GoetyBoltGlyph) && !(part instanceof GoetyEffectGlyph)) continue;
            var method = part instanceof GoetyBoltGlyph ? MethodProjectile.INSTANCE : MethodTouch.INSTANCE;
            var recipe = List.<AbstractSpellPart>of(method, part, AugmentBurning.INSTANCE);
            boolean supported = part instanceof GoetyBoltGlyph bolt ? GoetyAugmentValidation.supportsBurning(bolt.focus())
                    : GoetyAugmentValidation.supportsBurning(((GoetyEffectGlyph)part).focus());
            boolean addingError = validator.validate(recipe).stream().anyMatch(e -> e.getPosition() == 2);
            h.assertTrue(addingError != supported, "Palette eligibility matches native Burning support for " + part.getRegistryName());
        }
        var harm = List.<AbstractSpellPart>of(MethodProjectile.INSTANCE, GlyphCatalog.FIREBALL, EffectHarm.INSTANCE, AugmentBurning.INSTANCE);
        h.assertTrue(validator.validate(harm).stream().anyMatch(e -> e.getPosition() == 3),
                "Earlier supported projectile does not allow Burning on an unrelated later effect");
        var both = List.<AbstractSpellPart>of(MethodTouch.INSTANCE, GlyphCatalog.FANGS, AugmentGoety.LIFE_ABSORB, AugmentAmplify.INSTANCE);
        h.assertTrue(validator.validate(both).stream().anyMatch(e -> e.getPosition() == 3),
                "Adding Amplify after native absorption is disabled too");

        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setGameMode(GameType.SURVIVAL);
            SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
            for (var payload : List.of(GlyphCatalog.SOUL_BOLT, GlyphCatalog.MAGIC_BOLT, GlyphCatalog.POISON_DART)) {
                var spell = new Spell(MethodProjectile.INSTANCE, payload, AugmentBurning.INSTANCE);
                var context = new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY);
                context.nextPart();
                var carrier = new EntityProjectileSpell(h.getLevel(), new SpellResolver(context));
                carrier.setDeltaMovement(1, 0, 0);
                h.assertTrue(ProjectileDispatch.convert(carrier), "Raw invalid recipe is consumed, without falling back to a normal carrier");
            }
            h.assertTrue(SEHelper.getSESouls(player) == 10000
                            && h.getLevel().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(80),
                                    projectile -> projectile.getOwner() == player).isEmpty(),
                    "Invalid generated projectile recipes create no native projectile and charge nothing");
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); }
        h.succeed();
    }
}
