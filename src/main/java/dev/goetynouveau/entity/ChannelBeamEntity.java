package dev.goetynouveau.entity;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
public final class ChannelBeamEntity extends GoetyBeamDisplay {
    public ChannelBeamEntity(EntityType<?> type,Level world) {super(type,world);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STYLE,0);builder.define(HOST,-1);builder.define(TARGET,-1);
        builder.define(DX,0f);builder.define(DY,0f);builder.define(DZ,0f);builder.define(FLOW,.4f);
        builder.define(START,0L);builder.define(DURATION,1);
    }
}
