package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.spells.necromancy.HauntedSkullSpell;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

/** Retain the loader's actual native spell differences. */
public final class NativeSummonHooks {
    private NativeSummonHooks() {}
    public static LivingEntity owner(LivingEntity caster) { return caster; }
    public static int count(HauntedSkullSpell spell, ItemStack staff) {
        return staff.is(ModItems.NAMELESS_STAFF.get()) ? 5 : spell.rightStaff(staff) ? 3 : 1;
    }
    public static int burning(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.BURNING.get(), caster); }
    public static int additionalDuration(LivingEntity caster) { return 1; }
    public static void sharpen(com.Polarice3.Goety.common.entities.ally.illager.raider.AllyVex vex, int potency) {
        vex.getMainHandItem().enchant(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS,potency);
        vex.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,vex.getMainHandItem());
    }
    public static void finalizeSpawn(ServerLevel world, net.minecraft.world.entity.Mob skull, BlockPos position) {
        skull.finalizeSpawn(world, world.getCurrentDifficultyAt(position), MobSpawnType.MOB_SUMMONED, null, null);
    }
}
