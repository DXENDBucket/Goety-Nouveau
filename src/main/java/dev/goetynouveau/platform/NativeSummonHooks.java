package dev.goetynouveau.platform;

import com.Polarice3.Goety.common.enchantments.ModEnchantments;
import com.Polarice3.Goety.common.entities.ally.undead.HauntedSkull;
import com.Polarice3.Goety.common.magic.spells.necromancy.HauntedSkullSpell;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;

/** Retain the loader's actual native spell differences. */
public final class NativeSummonHooks {
    private NativeSummonHooks() {}
    public static LivingEntity owner(LivingEntity caster) { return MobUtil.getSummonOwner(caster); }
    public static int count(HauntedSkullSpell spell, ItemStack staff) { return spell.rightStaff(staff) ? 3 : 1; }
    public static int burning(LivingEntity caster) { return WandUtil.getLevels(ModEnchantments.BURNING, caster); }
    public static int additionalDuration(LivingEntity caster) { return 0; }
    public static void finalizeSpawn(ServerLevel world, HauntedSkull skull, BlockPos position) {
        skull.finalizeSpawn(world, world.getCurrentDifficultyAt(position), MobSpawnType.MOB_SUMMONED, null);
    }
}
