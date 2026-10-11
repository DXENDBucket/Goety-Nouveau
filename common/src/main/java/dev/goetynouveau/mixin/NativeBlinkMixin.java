package dev.goetynouveau.mixin;

import com.Polarice3.Goety.common.magic.spells.void_spells.BlinkSpell;
import com.Polarice3.Goety.utils.WandUtil;
import dev.goetynouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=BlinkSpell.class,remap=false)
public abstract class NativeBlinkMixin {
    @ModifyVariable(method="SpellResult",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$host(LivingEntity source){return GoetyNativeGeometry.spatial(this,source);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean goetyNouveau$sourceRange(LivingEntity actor){
        var view=GoetyNativeGeometry.current();return view!=null && view.spell()==(Object)this && actor==view.host()?false:WandUtil.enchantedFocus(actor);
    }
    @Inject(method="enderTeleportEvent",at=@At("HEAD"),cancellable=true)
    private static void goetyNouveau$loadedPoint(LivingEntity actor,BlockPos target,CallbackInfo ci){
        if(GoetyBlinkCasting.managed(actor) && !GoetyBlinkCasting.destination(actor,target.getX(),target.getY(),target.getZ()))ci.cancel();
    }
    @Redirect(method="enderTeleportEvent",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;teleportTo(DDD)V",remap=true))
    private static void goetyNouveau$actualEventResult(LivingEntity actor,double x,double y,double z){
        if(GoetyBlinkCasting.managed(actor) && !GoetyBlinkCasting.destination(actor,x,y,z))return;
        actor.teleportTo(x,y,z);GoetyBlinkCasting.moved(actor);
    }
}
