package dev.goetynouveau.client;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=dev.goetynouveau.GoetyNouveau.MOD_ID,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class GoetyEntityRenderers {
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void register(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.goetynouveau.entity.GoetyEntities.CHANNEL_BEAM.get(),ChannelBeamRenderer::new);
    }
}
