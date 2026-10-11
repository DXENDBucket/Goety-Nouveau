package dev.goetynouveau.entity;

import net.minecraft.network.syncher.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.function.BooleanSupplier;

/** Tracked visuals only. Native spell callbacks own all damage and environmental interactions. */
public abstract class GoetyBeamDisplay extends Entity {
    public static final int WATER=0, PRISMA=1;
    public static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> HOST=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> TARGET=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> DX=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DY=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DZ=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> FLOW=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.LONG);
    public static final EntityDataAccessor<Integer> DURATION=SynchedEntityData.defineId(GoetyBeamDisplay.class,EntityDataSerializers.INT);
    private BooleanSupplier active;
    protected GoetyBeamDisplay(EntityType<?> type,Level world) {super(type,world);setNoGravity(true);}
    public void bind(LivingEntity host,int style,int ticks,float flow,BooleanSupplier active) {
        this.active=active;
        entityData.set(HOST,host.getId());entityData.set(STYLE,style);entityData.set(DURATION,ticks);
        entityData.set(START,level().getGameTime());entityData.set(FLOW,flow);setPos(host.getEyePosition());
    }
    public LivingEntity host() {return level().getEntity(entityData.get(HOST)) instanceof LivingEntity living?living:null;}
    public Entity target() {return level().getEntity(entityData.get(TARGET));}
    public int style() {return entityData.get(STYLE);}
    public float flow() {return entityData.get(FLOW);}
    public float elapsed(float partial) {return level().getGameTime()-entityData.get(START)+partial;}
    public float progress(float partial) {return Math.min(1,Math.max(0,elapsed(partial)/Math.max(1,entityData.get(DURATION))));}
    public Vec3 origin(float partial) {
        var host=host();return host!=null?host.getEyePosition(partial):new Vec3(
                net.minecraft.util.Mth.lerp(partial,xo,getX()),net.minecraft.util.Mth.lerp(partial,yo,getY()),net.minecraft.util.Mth.lerp(partial,zo,getZ()));
    }
    public Vec3 end(float partial) {
        var target=target();
        return target!=null?new Vec3(net.minecraft.util.Mth.lerp(partial,target.xo,target.getX()),
                net.minecraft.util.Mth.lerp(partial,target.yo,target.getY())+target.getBbHeight()/2,
                net.minecraft.util.Mth.lerp(partial,target.zo,target.getZ())):
                origin(partial).add(entityData.get(DX),entityData.get(DY),entityData.get(DZ));
    }
    public void observe(HitResult hit) {
        var host=host();if(host==null)return;
        entityData.set(TARGET,hit instanceof EntityHitResult e?e.getEntity().getId():-1);
        Vec3 end=hit!=null?hit.getLocation():host.getEyePosition();
        Vec3 delta=end.subtract(host.getEyePosition());
        entityData.set(DX,(float)delta.x);entityData.set(DY,(float)delta.y);entityData.set(DZ,(float)delta.z);
        setPos(host.getEyePosition());
    }
    @Override public void tick() {
        super.tick();
        if(!level().isClientSide && (active==null || !active.getAsBoolean())) {discard();return;}
        var host=host();if(host!=null)setPos(host.getEyePosition());
    }
    @Override public boolean shouldBeSaved() {return false;}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
}
