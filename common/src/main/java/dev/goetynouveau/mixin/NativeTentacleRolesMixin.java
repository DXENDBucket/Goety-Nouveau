package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.entities.neutral.GulfTentacle;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import dev.goetynouveau.casting.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.*;

@Mixin(value=GulfTentacle.class,remap=false)
public abstract class NativeTentacleRolesMixin {
    @Redirect(method="tick",remap=true,
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getYRot()F",remap=true))
    private float goetyNouveau$viewYaw(LivingEntity host) {
        return ((ArsTriggeredEntity)this).goetyNouveau$arsTriggered()?host.getViewYRot(1):host.getYRot();
    }
    @Redirect(method={"updatePositionAndRotation","damageEntities"},
            at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getYRot()F",remap=true))
    private float goetyNouveau$otherViewYaw(LivingEntity host){return goetyNouveau$viewYaw(host);}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true,remap=true)
    private void goetyNouveau$attachment(CallbackInfo ci){
        var area=(GulfTentacle)(Object)this;
        if(GoetyAreaProvenance.marked(area) && area.level() instanceof net.minecraft.server.level.ServerLevel world){
            var source=GoetyAreaProvenance.source(area,null);var host=area.getTrueOwner();
            if(host==null || !GoetyChannelCasting.eligible(world,source,host)){area.discard();ci.cancel();}
        }
    }
    @ModifyVariable(method="damageEntities",at=@At("HEAD"),argsOnly=true,index=1)
    private Set<LivingEntity> goetyNouveau$friends(Set<LivingEntity> targets){
        var area=(GulfTentacle)(Object)this;if(!GoetyAreaProvenance.marked(area))return targets;
        var owner=GoetyAreaProvenance.damageOwner(area,GoetyAreaProvenance.source(area,area.getTrueOwner()));
        var result=new HashSet<LivingEntity>();
        for(var target:targets)if(!GoetyAreaProvenance.friendly(area,target)
                && (owner==null || target!=owner && !MobUtil.areAllies(owner,target)))result.add(target);
        return result;
    }
    @Redirect(method="damageEntities",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/ModDamageSource;indirectDrench(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource goetyNouveau$damage(Entity direct,Entity nativeOwner){
        var area=(GulfTentacle)(Object)this;
        return ModDamageSource.indirectDrench(direct,GoetyAreaProvenance.marked(area)?GoetyAreaProvenance.damageOwner(area,GoetyAreaProvenance.source(area,area.getTrueOwner())):nativeOwner);
    }
    @Redirect(method="damageEntities",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$offspring(Level world,Entity extra){
        GoetyAreaProvenance.copy((Entity)(Object)this,extra);
        if(extra instanceof ArsTriggeredEntity child&&((ArsTriggeredEntity)this).goetyNouveau$arsTriggered())child.goetyNouveau$arsTriggered(true);
        return world.addFreshEntity(extra);
    }
}
