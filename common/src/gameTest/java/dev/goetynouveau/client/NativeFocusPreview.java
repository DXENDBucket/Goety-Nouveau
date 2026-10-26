package dev.goetynouveau.client;
import dev.goetynouveau.compat.NativeFocusIndex;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
public final class NativeFocusPreview extends Screen {
    private static boolean opened;private int frames;
    private NativeFocusPreview(){
        super(Component.literal("Native focus glyphs"));var mc=Minecraft.getInstance();
        for(var glyph:NativeFocusIndex.all()){
            var model=mc.getItemRenderer().getModel(glyph.getGlyph().getDefaultInstance(),null,null,0);
            if(model==mc.getModelManager().getMissingModel())throw new AssertionError("Missing native glyph model: "+glyph.focusId());
            for(var quad:model.getQuads(null,null,RandomSource.create(1)))if(quad.getSprite().contents().name().equals(MissingTextureAtlasSprite.getLocation()))throw new AssertionError("Missing native glyph texture: "+glyph.focusId());
        }
        org.slf4j.LoggerFactory.getLogger("Goety native glyph preview").info("PASS: {} native focus models loaded",NativeFocusIndex.all().size());
    }
    public static void tryOpen(){var mc=Minecraft.getInstance();if(Boolean.getBoolean("goetynouveau.native_self_preview")&&!opened&&mc.screen!=null&&mc.getOverlay()==null){opened=true;mc.setScreen(new NativeFocusPreview());}}
    @Override public void render(GuiGraphics gui,int mx,int my,float partial){
        gui.fill(0,0,width,height,0xFF29272D);int row=0;
        for(var glyph:NativeFocusIndex.all()){
            gui.drawString(font,glyph.focusId().toString(),10,15+row*55,0xFFFFFFFF);
            gui.pose().pushPose();gui.pose().translate(190,10+row*55,100);gui.pose().scale(3,3,3);gui.renderItem(glyph.getGlyph().getDefaultInstance(),0,0);gui.renderItem(glyph.source(),20,0);gui.pose().popPose();++row;
        }
        gui.flush();if(++frames==20)Screenshot.grab(minecraft.gameDirectory,"native-focus-glyphs.png",minecraft.getMainRenderTarget(),msg->{});if(frames==45)minecraft.stop();
    }
}
