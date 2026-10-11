package dev.goetynouveau.api;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.Polarice3.Goety.api.magic.ISpell;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/** Read existing Goety focuses without depending on either version's spell-registration internals. */
public final class FocusSpells {
    // This native utility spell has no IFocus item in either pinned release.
    // Its existing charm asset provides the icon; the addon glyph is its own casting key.
    private static final ISpell WARDING = new com.Polarice3.Goety.common.magic.spells.WardingSpell();
    private FocusSpells() {}
    public static Optional<ISpell> read(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IFocus focus
                ? Optional.ofNullable(focus.getSpell()) : Optional.empty();
    }
    public static Optional<ISpell> read(ResourceLocation focusItem) {
        if (!BuiltInRegistries.ITEM.containsKey(focusItem)) return Optional.empty();
        if(focusItem.equals(dev.arsconflux.api.projectile.CarrierRegistry.id("goety_nouveau:glyph_warding")))return Optional.of(WARDING);
        return read(new ItemStack(BuiltInRegistries.ITEM.get(focusItem)));
    }
}
