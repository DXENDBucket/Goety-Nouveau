package dev.goetynouveau.client;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid="goety_nouveau",value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class NativeFocusPreviewEvents {
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent event){if(event.phase==net.minecraftforge.event.TickEvent.Phase.END)NativeFocusPreview.tryOpen();}
}
