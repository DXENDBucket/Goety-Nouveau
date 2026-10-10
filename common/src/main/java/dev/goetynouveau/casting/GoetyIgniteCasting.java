package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.spells.IgniteSpell;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Use the native fire-charge block interactions and the complete native entity touch result,
 * with the actual Ars hit instead of another view ray trace. */
public final class GoetyIgniteCasting {
    private GoetyIgniteCasting() {}
    public static boolean apply(ServerLevel world, CastContext frame, IgniteSpell spell, int burning) {
        if (frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target) {
            if (!target.isAlive() || target.fireImmune()) return false;
            int previous = target.getRemainingFireTicks();
            var stats = spell.defaultStats(); stats.setBurning(Math.max(0, burning));
            spell.touchResult(world, frame.caster(), target, GoetyResources.staff(frame.caster()), stats);
            return target.getRemainingFireTicks() > previous;
        }
        if (frame.target() instanceof BlockHitResult hit) {
            var player = frame.caster() instanceof Player actual ? actual : null;
            // A fresh disposable fire charge; never consume or rewrite the caster's held item.
            return Items.FIRE_CHARGE.useOn(new UseOnContext(world, player, InteractionHand.MAIN_HAND,
                    new ItemStack(Items.FIRE_CHARGE), hit)).consumesAction();
        }
        return false;
    }
}
