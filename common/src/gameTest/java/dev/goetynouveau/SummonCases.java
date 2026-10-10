package dev.goetynouveau;

import com.Polarice3.Goety.common.entities.ally.undead.HauntedSkull;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.SEHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.goetynouveau.api.ResourceSource;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.platform.GoetyConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;

public final class SummonCases {
    private static SpellResolver resolver(GameTestHelper h, ServerPlayer player, AbstractAugment... augments) {
        var spell = new Spell(MethodTouch.INSTANCE, GlyphCatalog.HAUNTED_SKULL);
        for (var augment : augments) spell = spell.add(augment);
        var cast = new SpellResolver(new SpellContext(h.getLevel(), spell, player, LivingCaster.from(player), ItemStack.EMPTY));
        cast.spellContext.nextPart();
        return cast;
    }
    public static void nativeSkullsKeepTargetOwnerExpiryAndOneBatchCharge(GameTestHelper h, ServerPlayer player) {
        var previous = GoetyConfig.RESOURCE_SOURCE.get();
        var oldLimit = SpellConfig.SkullLimit.get();
        GoetyConfig.RESOURCE_SOURCE.set(ResourceSource.SOUL_ENERGY);
        try {
            player.setPos(h.absoluteVec(new Vec3(2, 2, 2)));
            player.setGameMode(GameType.SURVIVAL); player.setYRot(90);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.NECRO_STAFF.get()));
            SEHelper.setSEActive(player, true); SEHelper.setSESouls(player, 10000);
            var target = h.spawn(EntityType.COW, 7, 2, 2); target.setNoAi(true);
            var cast = resolver(h, player, AugmentExtendTime.INSTANCE, AugmentAOE.INSTANCE);
            int price = GoetyResources.quote(cast.spellContext, GlyphCatalog.HAUNTED_SKULL.focus().spell()).amount();
            cast.onResolveEffect(h.getLevel(), new EntityHitResult(target));
            var skulls = h.getLevel().getEntitiesOfClass(HauntedSkull.class, target.getBoundingBox().inflate(8),
                    skull -> player.getUUID().equals(skull.getOwnerId()));
            h.assertTrue(skulls.size() == 3 && SEHelper.getSESouls(player) == 10000 - price,
                    "Native necromancy staff creates a batch of three but charges the focus once");
            for (var skull : skulls) {
                h.assertTrue(skull.getTarget() == target && skull.getBoundOrigin().distSqr(target.blockPosition()) <= 9,
                        "Summons bind the Ars target and spawn near it, independently of caster facing");
                h.assertTrue(skull.getLifespan() == 2400 && skull.hasLifespan() && skull.getExplosionPower() > 1,
                        "Native lifetime and explosion radius receive Ars augments");
            }
            SpellConfig.SkullLimit.set(3);
            int before = SEHelper.getSESouls(player);
            resolver(h, player).onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(SEHelper.getSESouls(player) == before, "A full native summon limit does not charge another batch");
            // Use the entity's own expiry path; there is no integration-owned timer or cleanup message.
            for (var skull : skulls) { skull.setLifespan(1); skull.ownedTick(); }
            h.assertTrue(skulls.stream().allMatch(HauntedSkull::isRemoved), "Native expiry actually removes every summoned skull");
            SEHelper.setSESouls(player, 0);
            resolver(h, player).onResolveEffect(h.getLevel(), new EntityHitResult(target));
            h.assertTrue(h.getLevel().getEntitiesOfClass(HauntedSkull.class, target.getBoundingBox().inflate(8),
                    skull -> player.getUUID().equals(skull.getOwnerId())).isEmpty(), "No souls means no new summons");
            target.discard(); player.discard(); h.succeed();
        } finally { GoetyConfig.RESOURCE_SOURCE.set(previous); SpellConfig.SkullLimit.set(oldLimit); }
    }
}
