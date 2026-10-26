package dev.goetynouveau;

import dev.goetynouveau.platform.GoetyConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

@Mod(GoetyNouveau.MOD_ID)
public final class GoetyNouveau {
    public static final String MOD_ID = "goety_nouveau";
    public GoetyNouveau(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, GoetyConfig.SPEC, "goety_nouveau-server.toml");
        bus.addListener(dev.goetynouveau.network.ChantStatePayload::register);
        dev.goetynouveau.entity.GoetyEntities.register(bus);
        dev.goetynouveau.platform.BrewIngredient.register(bus);
        dev.goetynouveau.platform.NativeFocusPlatform.init(container);
        if(Boolean.getBoolean("goetynouveau.native_self_test"))try{Class.forName("dev.goetynouveau.NativeFocusFixture").getMethod("register",net.neoforged.bus.api.IEventBus.class).invoke(null,bus);}catch(ReflectiveOperationException error){throw new IllegalStateException(error);}
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
