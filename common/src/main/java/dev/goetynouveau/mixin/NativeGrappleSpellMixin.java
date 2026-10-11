package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.wild.GrappleSpell;
import com.Polarice3.Goety.common.entities.projectiles.VineHook;
import com.Polarice3.Goety.utils.SEHelper;
import dev.goetynouveau.api.GoetyGrappleAccess;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=GrappleSpell.class,remap=false)
public abstract class NativeGrappleSpellMixin {
    @ModifyVariable(method="SpellResult",at=@At("HEAD"),argsOnly=true,index=2)
    private LivingEntity goetyNouveau$executor(LivingEntity caster){return GoetyNativeGeometry.spatial(this,caster);}
    @Redirect(method="soulCost",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/SEHelper;getGrappling(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile goetyNouveau$quote(Player player){var v=GoetyNativeGeometry.current();return SEHelper.getGrappling(v!=null && v.spell()==(Object)this && player==v.context().caster() && v.host() instanceof Player host?host:player);}
    @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean goetyNouveau$spawn(ServerLevel world,Entity entity){
        var v=GoetyNativeGeometry.current();
        if(v!=null && v.spell()==(Object)this && entity instanceof VineHook hook){
            ((GoetyGrappleAccess)hook).goetyNouveau$hand(GoetyGrappleCasting.hand(v));GoetyAreaProvenance.mark(hook,v.context());
            if(!world.isInWorldBounds(hook.blockPosition()) || !world.hasChunkAt(hook.blockPosition()) || !world.addFreshEntity(hook)){hook.discard();return false;}
            return true;
        }return world.addFreshEntity(entity);
    }
}
