package dev.goetynouveau.mixin.client;
import com.Polarice3.Goety.client.render.VineHookRenderer;
import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import com.Polarice3.Goety.common.magic.spells.wild.GrappleSpell;
import com.Polarice3.Goety.api.magic.ISpell;
import com.Polarice3.Goety.utils.*;
import dev.goetynouveau.api.GoetyGrappleAccess;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=VineHookRenderer.class,remap=false)
public abstract class NativeGrappleHandMixin {
    @Redirect(method="getPlayerHandPos",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getSpellOnHand(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;)Lcom/Polarice3/Goety/api/magic/ISpell;"))
    private ISpell goetyNouveau$hand(LivingEntity actor,InteractionHand hand){
        if(actor instanceof Player player && SEHelper.getGrappling(player) instanceof VineHook hook
                && ((GoetyGrappleAccess)hook).goetyNouveau$managed())return ((GoetyGrappleAccess)hook).goetyNouveau$hand()==0?new GrappleSpell():null;
        return WandUtil.getSpellOnHand(actor,hand);
    }
}
