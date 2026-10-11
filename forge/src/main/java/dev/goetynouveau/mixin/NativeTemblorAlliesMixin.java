package dev.goetynouveau.mixin;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import com.Polarice3.Goety.common.magic.spells.geomancy.TemblorClapSpell;
import com.Polarice3.Goety.utils.MobUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Native damage attribution can differ from source allegiance; protect both role sets. */
@Mixin(targets="com.Polarice3.Goety.common.magic.spells.geomancy.TemblorClapSpell$1",remap=false)
public abstract class NativeTemblorAlliesMixin {
    @Inject(method="explodeHurt",at=@At("HEAD"),cancellable=true)
    private void goetyNouveau$protect(Entity target,DamageSource damage,double x,double y,double z,double distance,float amount,CallbackInfo ci){
        var v=GoetyNativeGeometry.current();if(v==null || !(v.spell() instanceof TemblorClapSpell))return;
        var source=v.context().caster();var owner=v.context().damageOwner();
        if(target==source || target==owner || MobUtil.areAllies(source,target) || owner!=null && MobUtil.areAllies(owner,target))ci.cancel();
    }
}
