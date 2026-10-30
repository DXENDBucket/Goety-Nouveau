package dev.goetynouveau;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.utils.SEHelper;
import dev.goetynouveau.casting.*;
import dev.goetynouveau.glyph.NativeFocusGlyph;
import dev.arsconflux.api.projectile.CarrierRegistry;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

public final class TimingIntegrationCases {
    public static void cooldowns(GameTestHelper h,ServerPlayer player) {
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        var focus=ModItems.SOUL_BOLT_FOCUS.get();
        GoetyFocusCooldowns.atLeast(player,focus,80);
        h.assertTrue(GoetyFocusCooldowns.remaining(player,focus)==60,"Native-effective cooldown is reduced once in the shared Goety writer");
        var original=ModItems.IRON_HIDE_FOCUS.get();
        SEHelper.addCooldown(player,original,80);
        h.assertTrue(GoetyFocusCooldowns.remaining(player,original)==80,"Ordinary Goety casts outside a bridge scope remain native");
        var glyph=new NativeFocusGlyph(CarrierRegistry.id("goety:soul_bolt_focus"),focus);
        var state=new NativeFocusScope.State(glyph,player);
        NativeFocusScope.scoped(state,()->{
            var custom=ModItems.ICE_SPIKE_FOCUS.get();
            SEHelper.addCooldown(player,custom,80);
            h.assertTrue(GoetyFocusCooldowns.remaining(player,custom)==60,"Opaque native callbacks also use the final reduction");
            var other=ModItems.FIREBALL_FOCUS.get();
            GoetyFocusCooldowns.atLeast(player,other,80);
            h.assertTrue(GoetyFocusCooldowns.remaining(player,other)==60,"Our writer inside a native scope is not reduced twice");
            return null;
        });
        player.discard();h.succeed();
    }
}
