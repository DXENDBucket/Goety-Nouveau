package dev.goetynouveau.platform;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.crafting.BrewIngredients;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import java.util.Objects;

public final class BrewIngredient extends AbstractIngredient {
    private final String effect;
    private static final IIngredientSerializer<BrewIngredient> SERIALIZER=new IIngredientSerializer<>() {
        public BrewIngredient parse(FriendlyByteBuf buffer) {return new BrewIngredient(buffer.readUtf());}
        public BrewIngredient parse(JsonObject json) {return new BrewIngredient(GsonHelper.getAsString(json,"effect"));}
        public void write(FriendlyByteBuf buffer,BrewIngredient ingredient) {buffer.writeUtf(ingredient.effect);}
    };
    private BrewIngredient(String effect) {
        super(BrewIngredients.examples(BrewIngredients.validate(effect)).map(Ingredient.ItemValue::new));
        this.effect=effect;
    }
    public static void register(IEventBus bus) {
        bus.addListener((FMLCommonSetupEvent event)->event.enqueueWork(()->CraftingHelper.register(CarrierRegistry.id("goety_nouveau:brew_effect"),SERIALIZER)));
    }
    @Override public boolean test(ItemStack stack) {return BrewIngredients.test(effect,stack);}
    @Override public boolean isSimple() {return false;}
    @Override public IIngredientSerializer<? extends Ingredient> getSerializer() {return SERIALIZER;}
    @Override public JsonElement toJson() {
        var json=new JsonObject();json.addProperty("type","goety_nouveau:brew_effect");json.addProperty("effect",effect);return json;
    }
    @Override public boolean equals(Object other) {return other instanceof BrewIngredient ingredient && effect.equals(ingredient.effect);}
    @Override public int hashCode() {return Objects.hash(effect);}
}
