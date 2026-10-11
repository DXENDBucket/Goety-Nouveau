package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.block.ScribesBlock;
import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.block.ThreePartBlock;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.glyph.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import java.util.function.*;

public final class GlyphRecipeCases {
    private static Item item(String id) { return BuiltInRegistries.ITEM.get(CarrierRegistry.id(id)); }
    private static Item material(AbstractSpellPart part) {
        String glyph=part.getRegistryName().getPath().substring(6);
        switch(glyph) {
            case "snareling_shot" -> { return item("goety:snaring_focus"); }
            case "snareling_trap" -> { return item("goety:snaring_focus"); }
            case "snap_fungus" -> { return item("goety:snap_fungus"); }
            case "blast_fungus" -> { return item("goety:blast_fungus"); }
            case "berserk_fungus" -> { return item("goety:berserk_fungus"); }
            case "berserk_cloud" -> { return item("goety:berserk_fungus"); }
            case "bone_pull" -> { return item("goety:bonehead_hammer"); }
            case "leaf_shell" -> { return item("minecraft:peony"); }
            case "thorn_trap" -> { return item("minecraft:rose_bush"); }
            case "transpose" -> { return item("minecraft:popped_chorus_fruit"); }
            case "corrosion" -> { return item("minecraft:lily_of_the_valley"); }
            default -> {}
        }
        if (part instanceof GoetyBoltGlyph bolt) return item("goety:" + bolt.focus().focus);
        if (part == GlyphCatalog.WARDING) return item("goety:cursed_ingot");
        if (part instanceof GoetyEffectGlyph effect) return item("goety:" + effect.focus().focus);
        if (part == AugmentBurning.INSTANCE) return Items.BLAZE_POWDER;
        if (part == AugmentGoety.EXTENSION) return item("goety:savage_tooth");
        if (part == AugmentGoety.LIFE_ABSORB) return item("goety:ectoplasm");
        if (part == AugmentGoety.SOUL_EATER) return item("goety:soul_emerald");
        throw new IllegalArgumentException("Missing material for " + part);
    }
    public static void nativeCatalogAndActualScribe(GameTestHelper h, ServerPlayer p,
            Function<String, GlyphRecipe> lookup, BiConsumer<ScribesTile, String> select,
            Function<AbstractSpellPart, Boolean> knows) {
        p.setGameMode(GameType.SURVIVAL); p.giveExperiencePoints(100);
        p.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(10, 2, 10)));
        try {
            h.assertTrue(GlyphCatalog.ALL.size() == 135, "All current registered glyphs are audited");
            for (var part : GlyphCatalog.ALL) {
                String id = BuiltInRegistries.ITEM.getKey(part.getGlyph()).toString();
                var recipe = lookup.apply(id);
                var required = material(part);
                h.assertTrue(recipe != null && recipe.getSpellPart() == part && recipe.output.getCount() == 1,
                        "Native Ars recipe produces exactly this registered glyph: " + id);
                h.assertTrue(required != Items.AIR && recipe.inputs.size() == 2
                        && recipe.inputs.get(0).test(new ItemStack(required))
                        && recipe.inputs.get(1).test(new ItemStack(item("ars_nouveau:source_gem"))) && recipe.exp == 27,
                        "Native recipe resolves real materials and source gem: " + id);
            }
            var foot = new BlockPos(3, 1, 3);
            var block = com.hollingsworth.arsnouveau.setup.registry.BlockRegistry.SCRIBES_BLOCK.get();
            var state = block.defaultBlockState().setValue(ScribesBlock.PART, ThreePartBlock.FOOT);
            h.setBlock(foot,state);
            // Native placement creates the head and maintains the real two-block table.
            block.setPlacedBy(h.getLevel(),h.absolutePos(foot),state,p,new ItemStack(block));
            var pos = foot.relative(state.getValue(com.hollingsworth.arsnouveau.common.block.TableBlock.FACING));
            var table = (ScribesTile)h.getBlockEntity(pos);
            // Exercise the original tile ticker directly: headless worlds have no
            // ordinary player's block simulation distance on either loader.
            h.onEachTick(() -> { if (h.getLevel().getBlockEntity(table.getBlockPos()) == table) table.tick(); });
            Runnable cleanup = p::discard;
            h.assertTrue(!knows.apply(GlyphCatalog.FIREBALL), "Fixture starts without the Goety glyph");
            craft(h,p,table,GlyphCatalog.FIREBALL,select,knows,cleanup,() ->
                    craft(h,p,table,AugmentGoety.LIFE_ABSORB,select,knows,cleanup,() -> {cleanup.run();h.succeed();}));
        } catch (Throwable error) { p.discard(); throw error; }
    }
    private static void craft(GameTestHelper h, ServerPlayer p, ScribesTile table, AbstractSpellPart part,
            BiConsumer<ScribesTile,String> select, Function<AbstractSpellPart,Boolean> knows, Runnable cleanup, Runnable next) {
        String id = BuiltInRegistries.ITEM.getKey(part.getGlyph()).toString();
        int before = ScribesTile.getTotalPlayerExperience(p);
        select.accept(table,id);
        h.assertTrue(table.recipe != null && ScribesTile.getTotalPlayerExperience(p) == before - 27,
                "Real Scribe accepts the recipe and charges native XP: " + id);
        var block = table.getBlockPos();
        var input = new ItemEntity(h.getLevel(),block.getX()+0.5,block.getY()+1.2,block.getZ()+0.5,new ItemStack(material(part)));
        var gem = new ItemEntity(h.getLevel(),block.getX()+0.5,block.getY()+1.2,block.getZ()+0.5,new ItemStack(item("ars_nouveau:source_gem")));
        input.setNoGravity(true);gem.setNoGravity(true);
        h.getLevel().addFreshEntity(input);h.getLevel().addFreshEntity(gem);
        h.runAfterDelay(150, () -> {
            try {
                var output = h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(block).inflate(4),e -> e.getItem().is(part.getGlyph()));
                h.assertTrue((input.isRemoved() || input.getItem().isEmpty()) && (gem.isRemoved() || gem.getItem().isEmpty())
                        && output.size()==1 && output.get(0).getItem().getCount()==1,
                        "Dropped ingredients are consumed and native table emits one glyph: " + id+", input="+input.getItem()+", gem="+gem.getItem()+", outputs="+output.size()+", crafting="+table.crafting+", ticks="+table.craftingTicks+", present="+(h.getLevel().getBlockEntity(block)==table));
                p.setItemInHand(InteractionHand.MAIN_HAND,output.get(0).getItem().copy());output.get(0).discard();
                part.getGlyph().use(h.getLevel(),p,InteractionHand.MAIN_HAND);
                h.assertTrue(knows.apply(part), "Crafted glyph learns through ordinary Ars right-click: " + id);
                next.run();
            } catch(Throwable error) {cleanup.run();throw error;}
        });
    }
}
