package dev.goetynouveau;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("goety_nouveau_native_casting") @PrefixGameTestTemplate(false)
public final class NativePreparationGameTests {
    @GameTest(template="empty",timeoutTicks=220)
    public static void nativeGoetyPreparationRelease(GameTestHelper h) {
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        ActiveCastingCases.nativeGoetyPreparationRelease(h,player,recipe -> new com.hollingsworth.arsnouveau.api.spell.SpellCaster().castSpell(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,null,recipe));
    }
    @GameTest(template="empty",timeoutTicks=220,batch="mixed_native")
    public static void mixedNativeRecipeCommitsBothCooldowns(GameTestHelper h) throws Exception {
        if (!net.neoforged.fml.ModList.get().isLoaded("irons_nouveau")) { h.succeed(); return; }
        var cookie=net.minecraft.server.network.CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "CastStudent"),false);
        var player=new net.minecraft.server.level.ServerPlayer(h.getLevel().getServer(),h.getLevel(),cookie.gameProfile(),cookie.clientInformation());
        var connection=new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),connection,player,cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(2,3,2)));
        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.STICK));
        player.setHealth(1);
        var cd=Class.forName("io.redspace.ironsspellbooks.config.ServerConfigs").getField("CREATIVE_COOLDOWN").get(null);
        var get=cd.getClass().getMethod("get"); var set=cd.getClass().getMethod("set",Object.class);
        Object previous=get.invoke(cd); set.invoke(cd,true);
        var iron=com.hollingsworth.arsnouveau.api.registry.GlyphRegistry.getSpellPart(dev.arsconflux.api.projectile.CarrierRegistry.id("irons_nouveau:glyph_heal"));
        h.assertTrue(iron!=null,"Optional Iron glyph is registered independently");
        var recipe=new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodSelf.INSTANCE,GlyphCatalog.IRON_HIDE,iron);
        int ticks=dev.goetynouveau.casting.GoetyActiveCastProvider.preparationTicks(GlyphCatalog.IRON_HIDE.focus().spell(),player,net.minecraft.world.item.ItemStack.EMPTY);
        new com.hollingsworth.arsnouveau.api.spell.SpellCaster().castSpell(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,null,recipe);
        h.assertTrue(dev.arsconflux.api.casting.ActiveCasts.isChanting(player) && player.getHealth()==1,"Mixed native effects enter one preparation");
        h.runAfterDelay(ticks+3,()-> {
            try {
                h.assertTrue(!dev.arsconflux.api.casting.ActiveCasts.isChanting(player) && player.getHealth()>1
                        && player.hasEffect(dev.goetynouveau.platform.NativeEffectHooks.ironHide(1,0).getEffect()),"Both providers release from the same active recipe");
                h.assertTrue(!dev.arsconflux.api.casting.ActiveCasts.allowed(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodSelf.INSTANCE,iron),player,dev.arsconflux.api.casting.ActiveCastSource.CASTER_TOOL),"Iron-only follow-up sees its native cooldown");
                h.assertTrue(!dev.arsconflux.api.casting.ActiveCasts.allowed(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodSelf.INSTANCE,GlyphCatalog.IRON_HIDE),player,dev.arsconflux.api.casting.ActiveCastSource.CASTER_TOOL),"Goety-only follow-up sees its native cooldown");
                h.succeed();
            } finally { try { set.invoke(cd,previous); } catch (Exception e) { throw new RuntimeException(e); } player.discard(); }
        });
    }

}
