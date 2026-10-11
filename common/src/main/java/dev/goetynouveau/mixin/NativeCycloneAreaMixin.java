package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.AbstractCyclone;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(value=AbstractCyclone.class,remap=false)
public abstract class NativeCycloneAreaMixin {
    @Inject(method="getTarget",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$selectedTarget(CallbackInfoReturnable<LivingEntity> cir){
        var area=(AbstractCyclone)(Object)this;
        if(GoetyAreaProvenance.marked(area) && area.level() instanceof net.minecraft.server.level.ServerLevel world){
            var id=area.getTargetId();var target=id!=null && world.getEntity(id) instanceof LivingEntity living && living.isAlive()?living:null;
            if(target!=null && (GoetyAreaProvenance.friendly(area,target) || area.getTrueOwner()!=null && com.Polarice3.Goety.utils.MobUtil.areAllies(area.getTrueOwner(),target)))target=null;
            cir.setReturnValue(target);
        }
    }
    @Inject(method="suckInMobs",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$friends(LivingEntity target,CallbackInfo ci){if(GoetyAreaProvenance.friendly((Entity)(Object)this,target))ci.cancel();}
}
