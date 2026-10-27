package dev.goetynouveau.platform;
import com.google.gson.*;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.List;
public final class NativeFocusPlatform {
    private static net.minecraftforge.fml.ModContainer container;
    public static void init(net.minecraftforge.fml.ModContainer owner){container=owner;}
    public static boolean ready(){return container!=null;}
    public static void item(AbstractSpellPart glyph){net.minecraftforge.registries.ForgeRegistries.ITEMS.register(glyph.getRegistryName(),glyph.getGlyph());}
    public static boolean itemRegistered(AbstractSpellPart glyph){return net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(glyph.getRegistryName())==glyph.getGlyph();}
    public static void config(AbstractSpellPart glyph){
        var builder=new net.minecraftforge.common.ForgeConfigSpec.Builder();glyph.buildConfig(builder);var spec=builder.build();glyph.CONFIG=spec;
        new net.minecraftforge.fml.config.ModConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER,spec,container,"goety_nouveau/"+glyph.getRegistryName().getPath()+".toml");
    }
    public static void recipe(JsonObject json,ResourceLocation focus,ResourceLocation output){
        var inputs=new JsonArray();var first=new JsonObject();first.addProperty("item",focus.toString());var entry=new JsonObject();entry.add("item",first);inputs.add(entry);
        var gem=new JsonObject();gem.addProperty("item","ars_nouveau:source_gem");var other=new JsonObject();other.add("item",gem);inputs.add(other);
        json.add("inputItems",inputs);json.addProperty("output",output.toString());json.addProperty("count",1);
    }
    public static Glyph glyphItem(NativeFocusGlyph glyph){return new Glyph(glyph){
        @Override public void appendHoverText(ItemStack stack,net.minecraft.world.level.Level level,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,level,lines,flag);lines.add(Component.translatable("goety_nouveau.native_self.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));}
    };}
    public static void damage(com.Polarice3.Goety.api.magic.ISpell spell,Runnable action){action.run();}
    public static void state(ItemStack wand,int lifetime,int shots){var tag=wand.getOrCreateTag();tag.putInt("Cast Time",lifetime);tag.putInt("Shots",shots);}
}
