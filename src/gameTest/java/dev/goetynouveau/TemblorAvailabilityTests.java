package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_temblor") @PrefixGameTestTemplate(false)
public final class TemblorAvailabilityTests {
 private static void inventory(){
  var covered=new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
  for(var part:GlyphCatalog.ALL){if(part instanceof dev.goetynouveau.glyph.GoetyEffectGlyph g)covered.add(g.focus().focusId());if(part instanceof dev.goetynouveau.glyph.GoetyBoltGlyph g)covered.add(dev.arsconflux.api.projectile.CarrierRegistry.id("goety:"+g.focus().focus));}
  var missing=new java.util.TreeMap<String,String>();int registered=0;
  for(var item:net.minecraft.core.registries.BuiltInRegistries.ITEM){var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);if(!id.getNamespace().equals("goety"))continue;
   var spell=dev.goetynouveau.api.FocusSpells.read(new net.minecraft.world.item.ItemStack(item));if(spell.isPresent()){registered++;if(!covered.contains(id))missing.put(id.toString(),spell.get().getClass().getName());}}
  System.out.println("GOETY_NATIVE_FOCUS_INVENTORY registered="+registered+" missing="+missing);
 }

 @GameTest(template="empty",timeoutTicks=40) public static void pinnedNeoDoesNotRegisterMissingForgeFocus(GameTestHelper h){inventory();
  h.assertTrue(!dev.goetynouveau.platform.NativeAreaHooks.temblorSupported() && GlyphCatalog.TEMBLOR_CLAP==null && GlyphCatalog.BREEZE!=null,"Neo glyph construction follows real pinned native availability");
  h.assertTrue(GlyphCatalog.ALL.stream().noneMatch(p->p.getRegistryName().getPath().equals("glyph_temblor_clap")),"Absent Forge native effect never enters Neo registry/spell school");h.succeed();
 }
}
