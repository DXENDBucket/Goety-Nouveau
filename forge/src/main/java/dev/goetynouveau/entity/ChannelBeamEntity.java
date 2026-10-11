package dev.goetynouveau.entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public final class ChannelBeamEntity extends GoetyBeamDisplay {
    public ChannelBeamEntity(EntityType<?> type,Level world) {super(type,world);}
    @Override protected void defineSynchedData() {
        entityData.define(STYLE,0);entityData.define(HOST,-1);entityData.define(TARGET,-1);
        entityData.define(DX,0f);entityData.define(DY,0f);entityData.define(DZ,0f);entityData.define(FLOW,.4f);
        entityData.define(START,0L);entityData.define(DURATION,1);
    }
    @Override public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket() {
        return net.minecraftforge.network.NetworkHooks.getEntitySpawningPacket(this);
    }
}
