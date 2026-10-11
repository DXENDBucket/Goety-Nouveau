package dev.goetynouveau.platform;
import com.Polarice3.Goety.common.magic.BreathingSpell;
import com.Polarice3.Goety.common.magic.SpellStat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
public final class NativeBreathHooks {
    private NativeBreathHooks() {}
    public static void result(com.Polarice3.Goety.common.magic.Spell spell, ServerLevel world, LivingEntity caster, ItemStack staff, SpellStat stats) {
        com.Vivideru.Goety.common.magic.SpellTypeDamage.cast(spell, () -> spell.SpellResult(world, caster, staff, stats));
    }
    public static void run(com.Polarice3.Goety.common.magic.Spell spell, Runnable action) {
        com.Vivideru.Goety.common.magic.SpellTypeDamage.cast(spell, action);
    }
}
