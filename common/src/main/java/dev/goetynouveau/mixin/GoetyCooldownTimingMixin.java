package dev.goetynouveau.mixin;

import com.Polarice3.Goety.utils.SEHelper;
import dev.goetynouveau.casting.GoetyFocusCooldowns;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Keep addon-defined native cooldowns inside the same final-timing boundary. */
@Mixin(value = SEHelper.class, remap = false)
public abstract class GoetyCooldownTimingMixin {
    @ModifyVariable(method = "addCooldown", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static int goetyNouveau$timing(int ticks, Player player, Item focus, int originalTicks) {
        return GoetyFocusCooldowns.nativeWrite(player, ticks);
    }
}
