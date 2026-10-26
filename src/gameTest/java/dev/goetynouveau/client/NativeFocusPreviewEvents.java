package dev.goetynouveau.client;
@net.neoforged.fml.common.EventBusSubscriber(modid="goety_nouveau",value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class NativeFocusPreviewEvents {
 @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event){NativeFocusPreview.tryOpen();}
}
