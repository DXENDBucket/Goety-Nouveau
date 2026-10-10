package dev.goetynouveau.platform;

import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Differences verified against the pinned Goety dependency, not a newer source checkout. */
public final class NativeLocationHooks {
    private NativeLocationHooks() {}
    public static void finalizeVolcano(net.minecraft.server.level.ServerLevel world,
            com.Polarice3.Goety.common.entities.neutral.Volcano volcano, net.minecraft.core.BlockPos pos) {
        volcano.finalizeSpawn(world, world.getCurrentDifficultyAt(pos),
                net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null);
    }
    public static boolean ghostFireTower(ItemStack staff) { return false; }
    public static double launchPower(boolean matchingStaff) { return matchingStaff ? 2.5 : 1.0; }
    public static int launchPotency(LivingEntity caster, boolean matchingStaff) {
        int level = WandUtil.getPotencyLevel(caster);
        return matchingStaff ? level : level / 2;
    }
}
