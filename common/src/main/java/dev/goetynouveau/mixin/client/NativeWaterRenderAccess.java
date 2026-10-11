package dev.goetynouveau.mixin.client;
import com.Polarice3.Goety.client.render.WaterJetRenderer;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;
@Mixin(value=WaterJetRenderer.class,remap=false)
public interface NativeWaterRenderAccess {
    @Accessor("JET_INNER") static ResourceLocation inner() {throw new AssertionError();}
    @Accessor("JET_OUTER") static ResourceLocation outer() {throw new AssertionError();}
    @Invoker("renderJetLayer") static void layer(VertexConsumer consumer,PoseStack poses,float radius,float length,float ratio,float offset) {throw new AssertionError();}
}
