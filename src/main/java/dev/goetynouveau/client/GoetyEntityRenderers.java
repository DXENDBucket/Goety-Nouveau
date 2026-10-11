package dev.goetynouveau.client;
@net.neoforged.fml.common.EventBusSubscriber(modid=dev.goetynouveau.GoetyNouveau.MOD_ID,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class GoetyEntityRenderers {
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.goetynouveau.entity.GoetyEntities.CHANNEL_BEAM.get(),ChannelBeamRenderer::new);
    }
}
