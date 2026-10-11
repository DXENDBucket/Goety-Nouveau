package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.client.inventory.container.CraftingFocusMenu;
import com.Polarice3.Goety.utils.WandUtil;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeMenuHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.phys.EntityHitResult;

/** Personal utility menus use the original caster's inventory, never a selected entity's. */
public final class GoetyMenuCasting {
    private GoetyMenuCasting() {}

    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame) {
        if (focus != EffectFocus.CRAFTING && focus != EffectFocus.ENDER_CHEST) return false;
        if (!(frame.caster() instanceof ServerPlayer player) || !NativeMenuHooks.realPlayer(player)
                || player.connection == null || !player.isAlive() || player.isRemoved()
                || player.isSpectator() || player.level() != world
                || !world.hasChunkAt(player.blockPosition())) return false;
        // Like Ars' Access Ender Inventory, an entity trigger opens the caster's own chest.
        // Crafting accepts block or entity triggers. Neither redirects menus to the hit player.
        if (focus == EffectFocus.ENDER_CHEST && !(frame.target() instanceof EntityHitResult)) return false;
        var before = player.containerMenu;
        var spell = (Spell) focus.spell();
        spell.SpellResult(world, player, GoetyResources.staff(player).copy(), WandUtil.getStats(player, spell));
        // Native openMenu can be denied by another mod. Do not charge a failed opening.
        return player.containerMenu != before && (focus == EffectFocus.CRAFTING
                ? player.containerMenu instanceof CraftingFocusMenu : player.containerMenu instanceof ChestMenu);
    }
}
