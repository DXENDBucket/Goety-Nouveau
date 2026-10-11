package dev.goetynouveau.client;

import com.Polarice3.Goety.client.render.ModRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.goetynouveau.entity.GoetyBeamDisplay;
import dev.goetynouveau.mixin.client.NativePrismaRenderAccess;
import dev.goetynouveau.mixin.client.NativeWaterRenderAccess;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.*;

/** Native textures and vertex routines, with real living executors rather than a fake held-wand player. */
public final class ChannelBeamRenderer<T extends GoetyBeamDisplay> extends EntityRenderer<T> {
    public ChannelBeamRenderer(EntityRendererProvider.Context context) {super(context);}
    @Override public boolean shouldRender(T beam,Frustum frustum,double x,double y,double z) {
        return frustum.isVisible(new AABB(beam.origin(1),beam.end(1)).inflate(1));
    }
    @Override public ResourceLocation getTextureLocation(T beam) {
        return beam.style()==GoetyBeamDisplay.WATER?NativeWaterRenderAccess.inner():
                dev.arsconflux.api.projectile.CarrierRegistry.id("minecraft:textures/entity/guardian_beam.png");
    }
    @Override public void render(T beam,float yaw,float partial,PoseStack poses,MultiBufferSource buffer,int light) {
        if(beam.style()==GoetyBeamDisplay.PRISMA && beam.target()==null)return;
        Vec3 origin=beam.origin(partial),delta=beam.end(partial).subtract(origin);
        if(!Double.isFinite(delta.lengthSqr()) || delta.lengthSqr()<1e-8)return;
        Vec3 entityPosition=new Vec3(Mth.lerp(partial,beam.xo,beam.getX()),Mth.lerp(partial,beam.yo,beam.getY()),Mth.lerp(partial,beam.zo,beam.getZ()));
        Vec3 shift=origin.subtract(entityPosition),unit=delta.normalize();
        poses.pushPose();
        poses.translate(shift.x,shift.y,shift.z);
        poses.mulPose(Axis.YP.rotationDegrees((float)((Math.PI/2-Math.atan2(unit.z,unit.x))*180/Math.PI)));
        poses.mulPose(Axis.XP.rotationDegrees((float)(Math.acos(Mth.clamp(unit.y,-1,1))*180/Math.PI)));
        if(beam.style()==GoetyBeamDisplay.WATER) {
            float age=beam.level().getGameTime()+partial;
            NativeWaterRenderAccess.layer(buffer.getBuffer(ModRenderType.magicBeam(NativeWaterRenderAccess.inner())),poses,.04f,(float)delta.length(),16,-age*beam.flow());
            NativeWaterRenderAccess.layer(buffer.getBuffer(ModRenderType.magicBeam(NativeWaterRenderAccess.outer())),poses,.05f,(float)delta.length(),128,-age*beam.flow()*.2f);
        } else prisma(beam,partial,poses,buffer.getBuffer(NativePrismaRenderAccess.type()),(float)delta.length()+1);
        poses.popPose();
        // The world renderer owns flushing; do not flush every display entity's shared buffer.
    }
    /** Original guardian-style rotating beam, charge colors and alternating end quad. */
    private void prisma(T beam,float partial,PoseStack poses,VertexConsumer vertices,float length) {
        float age=beam.elapsed(partial),offset=age*.5f%1,phase=age*.05f*-1.5f,strength=beam.progress(partial);
        int red=64+(int)(strength*strength*191),green=32+(int)(strength*strength*191),blue=128-(int)(strength*strength*64);
        float lower=-1+offset,upper=length*2.5f+lower;
        float[] angle={phase+Mth.PI,phase,phase+Mth.HALF_PI,phase+Mth.PI*1.5f};
        float[] x=new float[4],z=new float[4];
        for(int i=0;i<4;i++){x[i]=Mth.cos(angle[i])*.2f;z[i]=Mth.sin(angle[i])*.2f;}
        for(int start:new int[]{0,2}) {
            vertex(vertices,poses,x[start],length,z[start],red,green,blue,.4999f,upper);
            vertex(vertices,poses,x[start],0,z[start],red,green,blue,.4999f,lower);
            vertex(vertices,poses,x[start+1],0,z[start+1],red,green,blue,0,lower);
            vertex(vertices,poses,x[start+1],length,z[start+1],red,green,blue,0,upper);
        }
        float v=(beam.level().getGameTime()%2==0)? .5f:0;
        float[] corners={phase+2.3561945f,phase+.7853982f,phase+5.4977875f,phase+3.926991f};
        float[] us={.5f,1,1,.5f},vs={v+.5f,v+.5f,v,v};
        for(int i=0;i<4;i++)vertex(vertices,poses,Mth.cos(corners[i])*.282f,length,Mth.sin(corners[i])*.282f,red,green,blue,us[i],vs[i]);
    }
    private void vertex(VertexConsumer vertices,PoseStack poses,float x,float y,float z,int red,int green,int blue,float u,float v) {
        NativePrismaRenderAccess.vertex(vertices,poses.last().pose(),poses.last().normal(),x,y,z,red,green,blue,u,v);
    }
}
