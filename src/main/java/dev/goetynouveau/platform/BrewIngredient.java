package dev.goetynouveau.platform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.goetynouveau.crafting.BrewIngredients;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record BrewIngredient(String effect) implements ICustomIngredient {
    public BrewIngredient {BrewIngredients.validate(effect);}
    private static final MapCodec<BrewIngredient> CODEC=Codec.STRING.fieldOf("effect").xmap(BrewIngredient::new,BrewIngredient::effect);
    private static final DeferredRegister<IngredientType<?>> TYPES=DeferredRegister.create(NeoForgeRegistries.INGREDIENT_TYPES,"goety_nouveau");
    private static final Supplier<IngredientType<BrewIngredient>> TYPE=TYPES.register("brew_effect",()->new IngredientType<>(CODEC));
    public static void register(IEventBus bus) {TYPES.register(bus);}
    @Override public boolean test(ItemStack stack) {return BrewIngredients.test(effect,stack);}
    @Override public Stream<ItemStack> getItems() {return BrewIngredients.examples(effect);}
    @Override public boolean isSimple() {return false;}
    @Override public IngredientType<?> getType() {return TYPE.get();}
}
