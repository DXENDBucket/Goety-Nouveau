package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.wind.WhirlwindSpell;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyNativeGeometry;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=WhirlwindSpell.class,remap=false)
public abstract class NativeWhirlwindLiftMixin {
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;forcePush(Lnet/minecraft/world/entity/Entity;DDD)V"))
    private void goetyNouveau$lift(Entity host,double x,double y,double z) {
        var view=GoetyNativeGeometry.current();
        if(view!=null && view.spell()==(Object)this && host==view.host() && view.stats()!=null && host.getDeltaMovement().y>=0)
            y+=view.stats().getPotency()/100.0;
        MobUtil.forcePush(host,x,y,z);
    }
}
