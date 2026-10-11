package dev.goetynouveau.mixin.client;
import com.Polarice3.Goety.client.render.PrismaBeamRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
@Mixin(value=PrismaBeamRenderer.class,remap=false)
public interface NativePrismaRenderAccess {
    @Accessor("BEAM_RENDER_TYPE") static RenderType type() {throw new AssertionError();}
    @Invoker("vertex") static void vertex(VertexConsumer consumer,Matrix4f pose,Matrix3f normal,float x,float y,float z,int red,int green,int blue,float u,float v) {throw new AssertionError();}
}
