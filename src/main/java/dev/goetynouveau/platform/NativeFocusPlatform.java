package dev.goetynouveau.platform;
import com.google.gson.*;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.items.Glyph;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.List;
public final class NativeFocusPlatform {
    private static net.neoforged.fml.ModContainer container;
    public static void init(net.neoforged.fml.ModContainer owner){container=owner;}
    public static boolean ready(){return container!=null;}
    public static boolean modLoaded(String id){return net.neoforged.fml.ModList.get().isLoaded(id);}
    public static void item(AbstractSpellPart glyph){Registry.register(BuiltInRegistries.ITEM,glyph.getRegistryName(),glyph.getGlyph());}
    public static boolean itemRegistered(AbstractSpellPart glyph){return BuiltInRegistries.ITEM.containsKey(glyph.getRegistryName()) && BuiltInRegistries.ITEM.get(glyph.getRegistryName())==glyph.getGlyph();}
    public static void config(AbstractSpellPart glyph){
        var builder=new net.neoforged.neoforge.common.ModConfigSpec.Builder();glyph.buildConfig(builder);var spec=builder.build();glyph.CONFIG=spec;
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER,spec,"goety_nouveau/"+glyph.getRegistryName().getPath()+".toml");
    }
    public static void recipe(JsonObject json,ResourceLocation focus,ResourceLocation output){
        var inputs=new JsonArray();var first=new JsonObject();first.addProperty("item",focus.toString());inputs.add(first);var gem=new JsonObject();gem.addProperty("item","ars_nouveau:source_gem");inputs.add(gem);
        json.add("inputs",inputs);var result=new JsonObject();result.addProperty("id",output.toString());result.addProperty("count",1);json.add("output",result);
    }
    public static Glyph glyphItem(NativeFocusGlyph glyph){return new Glyph(glyph){
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){super.appendHoverText(stack,context,lines,flag);lines.add(Component.translatable("goety_nouveau.native_self.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));}
    };}
    public static void damage(com.Polarice3.Goety.api.magic.ISpell spell,Runnable action){com.Vivideru.Goety.common.magic.SpellTypeDamage.cast(spell,action);}
    public static void state(ItemStack wand,int lifetime,int shots){net.minecraft.world.item.component.CustomData.update(net.minecraft.core.component.DataComponents.CUSTOM_DATA,wand,tag->{tag.putInt("Cast Time",lifetime);tag.putInt("Shots",shots);});}
}
