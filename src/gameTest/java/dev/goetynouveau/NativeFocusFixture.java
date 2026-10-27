package dev.goetynouveau;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
public final class NativeFocusFixture {
 public static void register(IEventBus bus){
  var items=DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM,"native_focus_fixture");
  items.register("warmup",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Sample()));
  items.register("charge",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Charge()));
  items.register("ever",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Ever()));
  items.register(bus);
  var ars=(net.neoforged.fml.javafmlmod.FMLModContainer)net.neoforged.fml.ModList.get().getModContainerById("ars_nouveau").orElseThrow();
  ars.getEventBus().addListener(net.neoforged.bus.api.EventPriority.HIGHEST,(net.neoforged.neoforge.registries.RegisterEvent event)->{
   if(event.getRegistryKey().equals(net.minecraft.core.registries.Registries.ITEM))
    event.register(net.minecraft.core.registries.Registries.ITEM,helper->helper.register(dev.arsconflux.api.projectile.CarrierRegistry.id("native_focus_fixture:early"),new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Sample())));
  });
 }
}
