package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_charms") @PrefixGameTestTemplate(false)
public final class CharmActivationGameTests {
    private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"CharmStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        return player;
    }
    private static boolean present(){return dev.goetynouveau.platform.NativeFocusPlatform.modLoaded("goety_charms");}
    @GameTest(template="empty") public static void optionalRegistration(GameTestHelper h) {
        var id=dev.arsconflux.api.projectile.CarrierRegistry.id("goety_nouveau:glyph_activate_charm");
        h.assertTrue(com.hollingsworth.arsnouveau.api.registry.GlyphRegistry.getSpellpartMap().containsKey(id)==present(),"Glyph registration follows optional mod presence");h.succeed();
    }
    @GameTest(template="empty") public static void inventoryOrder(GameTestHelper h){if(!present()){h.succeed();return;}CharmActivationCases.inventoryOrder(h,player(h));}
    @GameTest(template="empty") public static void wrongTargetNoFallback(GameTestHelper h){if(!present()){h.succeed();return;}CharmActivationCases.wrongTargetNoFallback(h,player(h));}
    @GameTest(template="empty") public static void selectedLocationAndCreative(GameTestHelper h){if(!present()){h.succeed();return;}CharmActivationCases.selectedLocationAndCreative(h,player(h));}
    @GameTest(template="empty") public static void selectedConversion(GameTestHelper h){if(!present()){h.succeed();return;}CharmActivationCases.selectedConversion(h,player(h));}
}
