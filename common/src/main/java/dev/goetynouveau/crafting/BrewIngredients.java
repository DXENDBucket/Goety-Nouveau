package dev.goetynouveau.crafting;

import com.Polarice3.Goety.common.effects.brew.BrewEffectInstance;
import com.Polarice3.Goety.common.effects.brew.BrewEffects;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.BrewUtils;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Match native brew contents, independent of bottle form, strength or other mixed effects. */
public final class BrewIngredients {
    private BrewIngredients() {}
    public static String validate(String effect) {
        if(effect==null || !effect.matches("[a-z0-9_]+"))throw new IllegalArgumentException("Invalid brew effect: "+effect);
        return effect;
    }
    public static boolean test(String effect,ItemStack stack) {
        if(stack==null || stack.isEmpty() || !(stack.is(ModItems.BREW.get()) || stack.is(ModItems.SPLASH_BREW.get())
                || stack.is(ModItems.LINGERING_BREW.get()) || stack.is(ModItems.GAS_BREW.get())))return false;
        return BrewUtils.getBrewEffects(stack).stream().anyMatch(instance->instance.getEffect().getEffectID().equals("effect.goety."+effect));
    }
    public static Stream<ItemStack> examples(String effect) {
        var brew=Objects.requireNonNull(BrewEffects.INSTANCE.getBrewEffect("effect.goety."+effect),"Unknown brew effect: "+effect);
        return Stream.of(ModItems.BREW.get(),ModItems.SPLASH_BREW.get(),ModItems.LINGERING_BREW.get(),ModItems.GAS_BREW.get())
                .map(item->BrewUtils.setCustomEffects(new ItemStack(item),List.of(),List.of(new BrewEffectInstance(brew,1,0))));
    }
}
