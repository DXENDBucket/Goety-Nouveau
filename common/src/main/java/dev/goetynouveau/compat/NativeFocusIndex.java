package dev.goetynouveau.compat;

import com.Polarice3.Goety.api.items.magic.IFocus;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import dev.goetynouveau.platform.NativeFocusPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import java.util.function.Supplier;
import java.util.*;

/** Observe actual item registrations, including addons registered after our own item event. */
public final class NativeFocusIndex {
    private static final Map<ResourceLocation,NativeFocusGlyph> GLYPHS=new LinkedHashMap<>();
    private NativeFocusIndex() {}
    public static synchronized void registered(ResourceLocation id,Object item) {
        if(!NativeFocusPlatform.ready() || !(item instanceof Item source) || !(item instanceof IFocus focus)
                || Set.of("goety","goety_nouveau","minecraft","ars_nouveau").contains(id.getNamespace()) || GLYPHS.containsKey(id))return;
        try{if(focus.getSpell()==null)return;}catch(RuntimeException error){org.slf4j.LoggerFactory.getLogger(NativeFocusIndex.class).warn("Cannot read addon focus {}; skipping native glyph",id,error);return;}
        var glyph=new NativeFocusGlyph(id,source);
        NativeFocusPlatform.config(glyph);
        GlyphRegistry.getSpellpartMap().put(glyph.getRegistryName(),glyph);
        GlyphRegistry.getGlyphItemMap().put(glyph.getRegistryName(),glyph::getGlyph);
        GLYPHS.put(id,glyph);GlyphCatalog.ALL.add(glyph);
        NativeFocusPlatform.item(glyph);
    }
    public static ResourceLocation id(ResourceLocation focus) {return CarrierRegistry.id("goety_nouveau:glyph_native/"+focus.getNamespace()+"/"+focus.getPath());}
    public static synchronized List<NativeFocusGlyph> all() {return List.copyOf(GLYPHS.values());}
    /** Ars also registers its glyph map. Early addon focuses already registered their exact item;
     * omit only that second insertion, leaving the live catalogue and unrelated collisions intact. */
    public static synchronized Map<ResourceLocation,Supplier<Glyph>> itemsForArsRegistration(Map<ResourceLocation,Supplier<Glyph>> items) {
        var remaining=new LinkedHashMap<>(items);
        for(var glyph:GLYPHS.values()) {
            var supplier=remaining.get(glyph.getRegistryName());
            if(supplier!=null && NativeFocusPlatform.itemRegistered(glyph) && supplier.get()==glyph.getGlyph())
                remaining.remove(glyph.getRegistryName());
        }
        return remaining;
    }
    public static NativeFocusGlyph glyph(ResourceLocation glyph) {return all().stream().filter(g->g.getRegistryName().equals(glyph)).findFirst().orElse(null);}
}
