package dev.goetynouveau;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
public final class NativeFocusFixture {
 public static void register(IEventBus bus){
  var items=DeferredRegister.create(net.minecraft.core.registries.Registries.ITEM,"native_focus_fixture");
  items.register("warmup",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Sample()));
  items.register("charge",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Charge()));
  items.register("ever",()->new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Ever()));
  items.register(bus);
  net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<net.minecraft.world.item.Item>> catalogueKey=net.minecraft.resources.ResourceKey.createRegistryKey(dev.arsconflux.api.projectile.CarrierRegistry.id("native_focus_fixture:focus_catalogue"));
  bus.addListener((net.minecraftforge.registries.NewRegistryEvent event)->event.create(new net.minecraftforge.registries.RegistryBuilder<net.minecraft.world.item.Item>().setName(catalogueKey.location()).disableSaving().disableSync()));
  bus.addListener((net.minecraftforge.registries.RegisterEvent event)->{
   if(event.getRegistryKey().equals(catalogueKey)){
    var focus=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(dev.arsconflux.api.projectile.CarrierRegistry.id("native_focus_fixture:warmup"));
    if(!(focus instanceof com.Polarice3.Goety.api.items.magic.IFocus))throw new IllegalStateException("Custom catalogue fixture requires an already registered focus");
    event.register(catalogueKey,helper->helper.register(dev.arsconflux.api.projectile.CarrierRegistry.id("native_focus_fixture:catalogue_alias"),focus));
   }
  });
  var ars=(net.minecraftforge.fml.javafmlmod.FMLModContainer)net.minecraftforge.fml.ModList.get().getModContainerById("ars_nouveau").orElseThrow();
  ars.getEventBus().addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,(net.minecraftforge.registries.RegisterEvent event)->{
   if(event.getRegistryKey().equals(net.minecraft.core.registries.Registries.ITEM))
    event.register(net.minecraft.core.registries.Registries.ITEM,helper->helper.register(dev.arsconflux.api.projectile.CarrierRegistry.id("native_focus_fixture:early"),new com.Polarice3.Goety.common.items.magic.MagicFocus(new NativeFocusSamples.Sample())));
  });
 }
}
