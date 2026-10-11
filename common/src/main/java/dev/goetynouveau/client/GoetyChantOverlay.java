package dev.goetynouveau.client;
import com.Polarice3.Goety.config.MainConfig;
import com.Polarice3.Goety.client.gui.overlay.SoulEnergyGui;
import dev.arsconflux.api.projectile.CarrierRegistry;
import net.minecraft.client.gui.GuiGraphics;
/** Reuses the native soul HUD texture, position and progress region without modifying native cast data. */
public final class GoetyChantOverlay {
    private GoetyChantOverlay() {}
    public static void render(GuiGraphics graphics) {
        if (!ChantHudState.visible() || !MainConfig.SoulGuiShow.get()) return;
        int x = graphics.guiWidth() / 2 + MainConfig.SoulGuiHorizontal.get();
        int y = graphics.guiHeight() + MainConfig.SoulGuiVertical.get() - 9;
        var texture = CarrierRegistry.id("goety:textures/gui/soul_energy.png");
        if (!SoulEnergyGui.shouldDisplayBar()) graphics.blit(texture, x, y, 0f, 0f, 128, 9, 128, 90);
        int width = Math.max(0, Math.min(117, (int)(117 * ChantHudState.progress())));
        graphics.blit(texture, x + 9, y, 9f, 27f, width, 9, 128, 90);
    }
}
