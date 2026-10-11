package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
@GameTestHolder("goety_nouveau_recipes") @PrefixGameTestTemplate(false)
public final class GlyphRecipeGameTests {
    @GameTest(template="empty",timeoutTicks=360) public static void nativeCatalogAndActualScribe(GameTestHelper h) {
        var p=RapidFireGameTests.player(h);
        java.util.function.Function<String,GlyphRecipe> lookup=id -> {var holder=h.getLevel().getRecipeManager().byKey(dev.arsconflux.api.projectile.CarrierRegistry.id(id)).orElseThrow(); return (GlyphRecipe)holder.value();};
        GlyphRecipeCases.brewPredicates(h,lookup,ingredient->{
            var codec=net.minecraft.world.item.crafting.Ingredient.CODEC;
            var json=codec.encodeStart(com.mojang.serialization.JsonOps.INSTANCE,ingredient).getOrThrow();
            var decoded=codec.parse(com.mojang.serialization.JsonOps.INSTANCE,json).getOrThrow();
            var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
            try {net.minecraft.world.item.crafting.Ingredient.CONTENTS_STREAM_CODEC.encode(buffer,decoded);return net.minecraft.world.item.crafting.Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);}finally{buffer.release();}
        });
        GlyphRecipeCases.nativeCatalogAndActualScribe(h,p,lookup,(table,id) -> {var raw=h.getLevel().getRecipeManager().byKey(dev.arsconflux.api.projectile.CarrierRegistry.id(id)).orElseThrow(); @SuppressWarnings("unchecked") var recipe=(net.minecraft.world.item.crafting.RecipeHolder<GlyphRecipe>)(Object)raw; table.setRecipe(recipe,p);},part -> com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getPlayerDataCap(p).knowsGlyph(part));
    }
}
