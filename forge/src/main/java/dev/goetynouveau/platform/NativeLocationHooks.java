package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Differences verified against the pinned Goety dependency, not a newer source checkout. */
public final class NativeLocationHooks {
    private NativeLocationHooks() {}
    public static boolean ghostFireTower(ItemStack staff) { return staff.is(ModItems.NAMELESS_STAFF.get()); }
    public static double launchPower(boolean matchingStaff) { return matchingStaff ? 2.5 : 1.5; }
    public static int launchPotency(LivingEntity caster, boolean matchingStaff) { return WandUtil.getPotencyLevel(caster); }
}
