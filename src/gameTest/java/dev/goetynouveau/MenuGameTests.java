package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_menus") @PrefixGameTestTemplate(false)
public final class MenuGameTests {
    private static MenuCases.Fixture player(GameTestHelper h, boolean denied) {
        var profile=new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MenuStudent");
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(profile,false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,cookie.clientInformation()) {
            @Override public java.util.OptionalInt openMenu(net.minecraft.world.MenuProvider provider) {
                return denied ? java.util.OptionalInt.empty() : super.openMenu(provider);
            }
        };
        var packets=new java.util.ArrayList<net.minecraft.network.protocol.Packet<?>>();
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {packets.add(packet);}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {packets.add(packet);}
        };
        return new MenuCases.Fixture(player,packets);
    }
    @GameTest(template="empty",batch="menus_0",timeoutTicks=60) public static void nativeCraftingAndClose(GameTestHelper h) {MenuCases.nativeCraftingAndClose(h,player(h,false));}
    @GameTest(template="empty",batch="menus_1",timeoutTicks=60) public static void personalEnderChestAndSourceIsolation(GameTestHelper h) {MenuCases.personalEnderChestAndSourceIsolation(h,player(h,false),player(h,false));}
    @GameTest(template="empty",batch="menus_2",timeoutTicks=60) public static void invalidResourceCreativeAndDeniedOpen(GameTestHelper h) {MenuCases.invalidResourceCreativeAndDeniedOpen(h,player(h,false),player(h,true),net.neoforged.neoforge.common.util.FakePlayerFactory.get(h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(),"MenuFake")));}
}
