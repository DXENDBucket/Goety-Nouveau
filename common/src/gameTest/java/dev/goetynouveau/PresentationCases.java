package dev.goetynouveau;

import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.AbstractEffect;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIgnite;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import dev.arsconflux.api.glyph.GlyphPresentation;
import dev.arsconflux.api.projectile.CarrierRegistry;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PresentationCases {
    public static void check(GameTestHelper h, Comparator<AbstractSpellPart> registryOrder) {
        var values = new ArrayList<AbstractSpellPart>(GlyphCatalog.ALL);
        values.addAll(List.of(EffectIgnite.INSTANCE, EffectHarm.INSTANCE));
        for (var name : List.of("heal", "oakskin", "firebolt")) {
            var iron = GlyphRegistry.getSpellPart(CarrierRegistry.id("irons_nouveau:glyph_" + name));
            if (iron != null) values.add(iron);
        }
        for (var part : GlyphCatalog.ALL)
            h.assertTrue(part == GlyphCatalog.SOUL_BOLT || part instanceof AbstractEffect,
                    "Effect glyphs retain their actual category, independently of sorting");
        values.sort(registryOrder);
        verify(h, values);
        var clientOrder = GlyphPresentation.withinCategories(values,
                Comparator.comparing(part -> part.getRegistryName().toString()),
                part -> part instanceof AbstractEffect ? 1 : 2,
                AbstractSpellPart::getTypeIndex, category -> category);
        values.sort(clientOrder);
        verify(h, values);
        h.succeed();
    }
    private static void verify(GameTestHelper h, List<AbstractSpellPart> values) {
        String provider = "";
        int category = Integer.MIN_VALUE;
        for (var part : values) {
            int nextCategory = part.getTypeIndex();
            if (nextCategory != category) { category = nextCategory; provider = ""; }
            String namespace = part.getRegistryName().getNamespace();
            String next = namespace.equals("goety_nouveau") || namespace.equals("irons_nouveau") ? namespace : "";
            h.assertTrue(provider.compareTo(next) <= 0,
                    "Ordinary Ars/addon glyphs precede contiguous Goety and Iron groups within each category");
            provider = next;
        }
    }
}
