package dev.goetynouveau.mixin;
import com.Polarice3.Goety.client.gui.overlay.SoulEnergyGui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = SoulEnergyGui.class, remap = false)
public abstract class NativeChantBarMixin {
    @Inject(method = "drawHUD", at = @At("RETURN"))
    private static void goetyNouveau$chant(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight, CallbackInfo ci) {
        dev.goetynouveau.client.GoetyChantOverlay.render(graphics);
    }
}
