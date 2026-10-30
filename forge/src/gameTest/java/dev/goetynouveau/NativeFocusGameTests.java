package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("goety_nouveau_native_self") @PrefixGameTestTemplate(false)
public final class NativeFocusGameTests {
 @GameTest(template="empty",timeoutTicks=30) public static void universalPreparation(GameTestHelper h){
    var p=player(h);p.getAttribute(dev.arsconflux.platform.TimingAttributes.CAST_TIME_REDUCTION.get()).setBaseValue(.5);
    NativeFocusChecks.universalPreparation(h,p);
 }
 @GameTest(template="empty",timeoutTicks=30) public static void universalCooldown(GameTestHelper h){
    var p=player(h);p.getAttribute(dev.arsconflux.platform.TimingAttributes.COOLDOWN_REDUCTION.get()).setBaseValue(.25);
    TimingIntegrationCases.cooldowns(h,p);
 }
 private static net.minecraft.server.level.ServerPlayer player(GameTestHelper h){
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"));
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        return player;
 }
 @GameTest(template="empty",timeoutTicks=80) public static void catalogue(GameTestHelper h){NativeFocusChecks.catalogue(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void warmupStaffAndScope(GameTestHelper h){NativeFocusChecks.warmupStaffAndScope(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void chargingBilling(GameTestHelper h){NativeFocusChecks.chargingBilling(h,player(h));}
 @GameTest(template="empty",timeoutTicks=80) public static void emptyAccountAndInvalidTarget(GameTestHelper h){NativeFocusChecks.emptyAccountAndInvalidTarget(h,player(h));}
 @GameTest(template="empty",batch="native_ars",timeoutTicks=80) public static void arsAlternativeAndCreative(GameTestHelper h){var p=player(h);NativeFocusChecks.prepare(h,p);var mana=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(p).orElseThrow(IllegalStateException::new);mana.setMaxMana(1000);mana.setMana(1000);NativeFocusChecks.arsAlternativeAndCreative(h,p,new com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster(p),()->(int)mana.getCurrentMana());}
}
