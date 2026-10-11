package dev.goetynouveau;

import dev.goetynouveau.platform.GoetyConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

@Mod(GoetyNouveau.MOD_ID)
public final class GoetyNouveau {
    public static final String MOD_ID = "goety_nouveau";
    public GoetyNouveau() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GoetyConfig.SPEC, "goety_nouveau-server.toml");
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        dev.goetynouveau.network.ForgeNetwork.register();
        dev.goetynouveau.entity.GoetyEntities.register(bus);
        dev.goetynouveau.platform.BrewIngredient.register(bus);
        GlyphCatalog.register();
        dev.goetynouveau.casting.GoetyActiveCastProvider.register();
        bus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(GlyphCatalog::configureCarriers));
        var tabs = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);
        tabs.register("glyphs", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.goety_nouveau"))
                .icon(() -> GlyphCatalog.SOUL_BOLT.getGlyph().getDefaultInstance())
                .displayItems((parameters, output) -> GlyphCatalog.ALL.forEach(glyph -> output.accept(glyph.getGlyph()))).build());
        tabs.register(bus);
    }
}
