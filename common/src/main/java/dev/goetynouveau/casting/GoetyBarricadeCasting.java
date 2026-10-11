package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.AbstractMonolith;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.init.ModAttributes;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import java.util.HashSet;
import java.util.UUID;

/** Native trap geometry, variants, emergence and explosions at the actual Ars hit. */
public final class GoetyBarricadeCasting {
    private GoetyBarricadeCasting() {}
    public static boolean apply(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults, SpellStats ars) {
        var caster = frame.caster();
        LivingEntity target = frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living.isAlive() && living.level() == world ? living : null;
        if (target == null && !(frame.target() instanceof BlockHitResult)) return false;
        BlockPos pos = target != null ? target.blockPosition() : ((BlockHitResult)frame.target()).getBlockPos();
        if (pos.getY() < world.getMinBuildHeight() || pos.getY() + 4 >= world.getMaxBuildHeight()) return false;
        for (int x : new int[]{-8, 8}) for (int z : new int[]{-8, 8})
            if (!world.hasChunkAt(pos.offset(x, 0, z))) return false;
        int potency = Math.max(0, defaults.potency() + (int)ars.getAmpMultiplier());
        int duration = Math.max(0, defaults.duration() + (int)ars.getDurationMultiplier());
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            duration += NativeEffectHooks.duration(caster);
        }
        var staff = GoetyResources.staff(caster);
        EntityType<? extends AbstractMonolith> wall = spell.typeStaff(staff, SpellType.FROST)
                ? ModEntityType.GLACIAL_WALL.get() : ModEntityType.TOTEMIC_WALL.get();
        var area = new AABB(pos).inflate(12);
        var before = new HashSet<UUID>();
        world.getEntitiesOfClass(AbstractMonolith.class, area).forEach(e -> before.add(e.getUUID()));
        int penalty = 0;
        if (GoetyResources.sneaking(caster)) {
            boolean quad = world.random.nextFloat() <= (spell.rightStaff(staff) ? 0.25F : 0.05F);
            penalty = quad ? 60 : 40;
            if (quad) {
                if (target != null) WandUtil.summonQuadOffensiveTrap(caster, target, ModEntityType.TOTEMIC_BOMB.get(), potency);
                else WandUtil.summonQuadOffensiveTrap(caster, pos, ModEntityType.TOTEMIC_BOMB.get(), potency);
            } else if (target != null) WandUtil.summonMonolith(caster, target, ModEntityType.TOTEMIC_BOMB.get(),
                    world.random.nextInt(-1, 1), world.random.nextInt(-1, 1), potency);
            else WandUtil.summonMonolith(caster, pos, ModEntityType.TOTEMIC_BOMB.get(), 0, 0, potency);
        } else if (target == null) WandUtil.summonWallTrap(caster, pos, wall, duration);
        else {
            switch (world.random.nextInt(3)) {
                case 0 -> {
                    int[] opening = switch (Direction.fromYRot(target.getYHeadRot())) {
                        case SOUTH -> WandUtil.CONFIG_1_NORTH_ROW;
                        case NORTH -> WandUtil.CONFIG_1_SOUTH_ROW;
                        case EAST -> WandUtil.CONFIG_1_WEST_ROW;
                        default -> WandUtil.CONFIG_1_EAST_ROW;
                    };
                    WandUtil.summonSquareTrap(caster, target, wall, opening, duration);
                }
                case 1 -> WandUtil.summonWallTrap(caster, target, wall, duration);
                default -> WandUtil.summonRandomPillarsTrap(caster, target, wall, duration);
            }
        }
        boolean success = world.getEntitiesOfClass(AbstractMonolith.class, area).stream().anyMatch(e -> !before.contains(e.getUUID()));
        if (success) {
            // Multiply before rounding, as in normal native spellCooldown, retaining fractional discounts.
            int ticks = Math.max(0, (int)((spell.defaultSpellCooldown() + penalty) * ModAttributes.getCooldownDiscount(caster)));
            GoetyFocusCooldowns.atLeast(caster, BuiltInRegistries.ITEM.get(CarrierRegistry.id("goety:barricade_focus")), ticks);
        }
        return success;
    }
}
