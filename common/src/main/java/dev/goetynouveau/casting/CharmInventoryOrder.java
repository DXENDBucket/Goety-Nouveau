package dev.goetynouveau.casting;

import net.minecraft.world.item.ItemStack;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/** Offhand, then hotbar 0..8, then backpack rows 9..35 in visual reading order. */
public final class CharmInventoryOrder {
    public static ItemStack first(ItemStack offhand, IntFunction<ItemStack> inventory, Predicate<ItemStack> eligible) {
        if (!offhand.isEmpty() && eligible.test(offhand)) return offhand;
        for (int slot = 0; slot < 36; slot++) {
            var stack = inventory.apply(slot);
            if (!stack.isEmpty() && eligible.test(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    private CharmInventoryOrder() { }
}
