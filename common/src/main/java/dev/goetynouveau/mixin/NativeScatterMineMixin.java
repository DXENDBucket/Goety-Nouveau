package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.entities.projectiles.ScatterMine;
import com.Polarice3.Goety.utils.MobUtil;
import dev.goetynouveau.casting.GoetyAreaProvenance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ScatterMine.class,remap=false)
public abstract class NativeScatterMineMixin {
 @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;areAllies(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z",remap=false),remap=true)
 private boolean goetyNouveau$tickFriends(Entity source,Entity target){return goetyNouveau$friends(source,target);}
 @Redirect(method="trigger",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;areAllies(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Z"))
 private boolean goetyNouveau$triggerFriends(Entity source,Entity target){return goetyNouveau$friends(source,target);}
 @org.spongepowered.asm.mixin.Unique private boolean goetyNouveau$friends(Entity source,Entity target){var mine=(ScatterMine)(Object)this;var owner=GoetyAreaProvenance.damageOwner(mine,null);return MobUtil.areAllies(source,target) || owner!=null && (owner==target || MobUtil.areAllies(owner,target));}
 @Redirect(method="explodeDamage",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;explosion(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
 private DamageSource goetyNouveau$damage(DamageSources sources,Entity direct,Entity indirect){var mine=(ScatterMine)(Object)this;var owner=GoetyAreaProvenance.damageOwner(mine,null);return owner!=null?sources.explosion(owner,owner):sources.explosion(direct,indirect);}
}
