package dev.goetynouveau.compat.charms;

import com.hollingsworth.arsnouveau.api.spell.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.context.CastContexts;
import dev.arsconflux.api.glyph.AbilityTags;
import dev.arsconflux.api.glyph.GlyphAbilities;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetycharms.api.CharmAccess;
import dev.goetycharms.api.CharmAdapter;
import dev.goetynouveau.casting.CharmInventoryOrder;
import dev.goetynouveau.platform.NativeMenuHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.Set;

/** Item-paid utility: the charm already contains the paid ritual, not another focus cast. */
public final class ActivateCharmGlyph extends AbstractEffect {
    public ActivateCharmGlyph() {
        super(CarrierRegistry.id("goety_nouveau:glyph_activate_charm"), "Activate Charm");
        spellSchools.add(SpellSchools.CONJURATION);
        SpellSchools.CONJURATION.addSpellPart(this);
        GlyphAbilities.declare(getRegistryName(), Set.of(AbilityTags.UTILITY, AbilityTags.PLAYER_ONLY,
                AbilityTags.POSITION, AbilityTags.ENEMY, AbilityTags.ALLY, AbilityTags.SELF));
    }

    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
    @Override protected Set<AbstractAugment> getCompatibleAugments() { return Set.of(); }

    public static boolean eligible(ItemStack stack) {
        var kind = CharmAccess.kind(stack);
        return (kind == CharmAdapter.Kind.SUMMONING || kind == CharmAdapter.Kind.CONVERSION) && CharmAccess.sealed(stack);
    }

    public static ItemStack select(ServerPlayer player) {
        return CharmInventoryOrder.first(player.getOffhandItem(), player.getInventory()::getItem, ActivateCharmGlyph::eligible);
    }

    public static boolean release(ServerPlayer player, HitResult hit) {
        if (hit == null || hit.getType() == HitResult.Type.MISS || !player.isAlive()
                || player.isRemoved() || player.isSpectator() || !NativeMenuHooks.realPlayer(player)) return false;
        var stack = select(player);
        if (stack.isEmpty()) return false;
        var target = hit instanceof EntityHitResult entity && entity.getEntity() instanceof LivingEntity living ? living : null;
        var origin = target != null ? target.blockPosition()
                : hit instanceof BlockHitResult block ? block.getBlockPos().relative(block.getDirection())
                : BlockPos.containing(hit.getLocation());
        // One selection and one attempt. Wrong targets never fall through to consume another charm.
        boolean result = CharmAccess.release(stack, player, origin, target);
        if (result) player.getInventory().setChanged();
        return result;
    }

    @Override public void onResolve(HitResult hit, Level world, LivingEntity ignored, SpellStats stats,
                                    SpellContext context, SpellResolver resolver) {
        if (!isEnabled() || !(world instanceof ServerLevel server)) return;
        var frame = CastContext.of(context, hit, null);
        if (!(frame.caster() instanceof ServerPlayer player) || player.level() != server) return;
        CastContexts.bind(context, frame);
        CastContexts.scoped(frame, () -> release(player, hit));
    }
}
