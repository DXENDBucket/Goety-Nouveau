package dev.goetynouveau.platform;

import com.Polarice3.Goety.utils.WandUtil;
import com.Polarice3.Goety.config.SpellConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Differences verified against the pinned Goety dependency, not a newer source checkout. */
public final class NativeLocationHooks {
    private static final ModConfigSpec.ConfigValue<?> LAUNCH_BASE = launchSetting("LaunchBasePower");
    private static final ModConfigSpec.ConfigValue<?> LAUNCH_POTENCY = launchSetting("LaunchPotencyPower");
    private NativeLocationHooks() {}
    public static void finalizeVolcano(net.minecraft.server.level.ServerLevel world,
            com.Polarice3.Goety.common.entities.neutral.Volcano volcano, net.minecraft.core.BlockPos pos) {
        volcano.finalizeSpawn(world, world.getCurrentDifficultyAt(pos),
                net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null);
    }
    public static boolean ghostFireTower(ItemStack staff) { return false; }
    public static double launchPower(boolean matchingStaff) {
        double base = LAUNCH_BASE == null ? 1.0 : ((Number)LAUNCH_BASE.get()).doubleValue();
        return base * (matchingStaff ? 2.5 : 1.0);
    }
    public static double launchPotencyPower() {
        return LAUNCH_POTENCY == null ? .5 : ((Number)LAUNCH_POTENCY.get()).doubleValue();
    }
    // Goety 3.2.1 added these settings. Older 3.2.0 installs retain their native constants.
    private static ModConfigSpec.ConfigValue<?> launchSetting(String name) {
        try {
            return (ModConfigSpec.ConfigValue<?>)SpellConfig.class.getField(name).get(null);
        } catch (NoSuchFieldException missingOnOlderGoety) {
            return null;
        } catch (IllegalAccessException inaccessible) {
            throw new ExceptionInInitializerError(inaccessible);
        }
    }
    public static int launchPotency(LivingEntity caster, boolean matchingStaff) {
        int level = WandUtil.getPotencyLevel(caster);
        return matchingStaff ? level : level / 2;
    }
}
