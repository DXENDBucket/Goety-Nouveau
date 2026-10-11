package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
@GameTestHolder("goety_nouveau_recipes") @PrefixGameTestTemplate(false)
public final class GlyphRecipeGameTests {
    @GameTest(template="empty",timeoutTicks=360) public static void nativeCatalogAndActualScribe(GameTestHelper h) {
        var p=RapidFireGameTests.player(h);
        java.util.function.Function<String,GlyphRecipe> lookup=id -> {return (GlyphRecipe)h.getLevel().getRecipeManager().byKey(dev.arsconflux.api.projectile.CarrierRegistry.id(id)).orElseThrow();};
        GlyphRecipeCases.brewPredicates(h,lookup,ingredient->{
            var decoded=net.minecraft.world.item.crafting.Ingredient.fromJson(ingredient.toJson());
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {decoded.toNetwork(buffer);return net.minecraft.world.item.crafting.Ingredient.fromNetwork(buffer);}finally{buffer.release();}
        });
        GlyphRecipeCases.nativeCatalogAndActualScribe(h,p,lookup,(table,id) -> {table.setRecipe(lookup.apply(id),p);},part -> com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getPlayerDataCap(p).orElseThrow(IllegalStateException::new).knowsGlyph(part));
    }
}
