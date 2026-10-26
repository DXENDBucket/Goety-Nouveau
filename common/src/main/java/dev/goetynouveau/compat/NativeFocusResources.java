package dev.goetynouveau.compat;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import java.util.Map;

public final class NativeFocusResources {
    private NativeFocusResources(){}
    public static String model(ResourceLocation id){
        if(!id.getNamespace().equals("goety_nouveau")||!id.getPath().startsWith("item/glyph_native/"))return null;
        var glyph=NativeFocusIndex.glyph(dev.arsconflux.api.projectile.CarrierRegistry.id("goety_nouveau:"+id.getPath().substring(5)));
        return glyph==null?null:"{\"parent\":\""+glyph.focusId().getNamespace()+":item/"+glyph.focusId().getPath()+"\"}";
    }
    public static void recipes(Map<ResourceLocation,JsonElement> recipes){
        for(var glyph:NativeFocusIndex.all()){
            var json=new JsonObject();json.addProperty("type","ars_nouveau:glyph");json.addProperty("exp",27);
            dev.goetynouveau.platform.NativeFocusPlatform.recipe(json,glyph.focusId(),glyph.getRegistryName());
            recipes.putIfAbsent(glyph.getRegistryName(),json);
        }
    }
}
