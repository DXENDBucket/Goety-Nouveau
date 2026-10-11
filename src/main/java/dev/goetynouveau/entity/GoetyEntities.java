package dev.goetynouveau.entity;
import dev.goetynouveau.GoetyNouveau;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.registries.DeferredRegister;
public final class GoetyEntities {
    private static final DeferredRegister<EntityType<?>> TYPES=DeferredRegister.create(Registries.ENTITY_TYPE,GoetyNouveau.MOD_ID);
    public static final java.util.function.Supplier<EntityType<ChannelBeamEntity>> CHANNEL_BEAM=TYPES.register("channel_beam",()->
            EntityType.Builder.<ChannelBeamEntity>of(ChannelBeamEntity::new,MobCategory.MISC).sized(.1f,.1f)
                    .clientTrackingRange(8).updateInterval(1).noSave().noSummon().build("goety_nouveau:channel_beam"));
    public static void register(net.neoforged.bus.api.IEventBus bus) {TYPES.register(bus);}
    private GoetyEntities() {}
}
