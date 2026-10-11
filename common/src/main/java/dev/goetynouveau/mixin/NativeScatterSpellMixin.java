package dev.goetynouveau.mixin;
import com.Polarice3.Goety.common.magic.spells.geomancy.ScatterSpell;
import com.Polarice3.Goety.common.entities.projectiles.ScatterMine;
import com.Polarice3.Goety.utils.*;
import dev.goetynouveau.casting.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ScatterSpell.class,remap=false)
public abstract class NativeScatterSpellMixin {
 @Unique private LivingEntity goetyNouveau$gear(LivingEntity actor){var v=GoetyNativeGeometry.current();return v!=null && v.spell()==(Object)this && actor==v.host()?v.context().caster():actor;}
 @Unique private Vec3 goetyNouveau$point(Entity caster,Vec3 ordinary){var v=GoetyNativeGeometry.current();if(v==null || v.spell()!=(Object)this || caster!=v.context().caster())return ordinary;
  var offset=ordinary.subtract(caster.position());var incoming=v.context().incomingDirection();
  if(incoming.horizontalDistanceSqr()>1e-10){var old=MobUtil.getFrontPos(caster,1).subtract(caster.position());double oldAngle=Math.atan2(old.x,old.z),newAngle=Math.atan2(incoming.x,incoming.z);offset=offset.yRot((float)(newAngle-oldAngle));}
  var center=v.context().target() instanceof EntityHitResult hit?hit.getEntity().position():v.context().target().getLocation();return center.add(offset);
 }
 @ModifyVariable(method="useSpell",at=@At("HEAD"),argsOnly=true,index=2)
 private LivingEntity goetyNouveau$executor(LivingEntity caster){return GoetyNativeGeometry.spatial(this,caster);}
 @Redirect(method={"useSpell","SpellResult"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;enchantedFocus(Lnet/minecraft/world/entity/LivingEntity;)Z"))
 private boolean goetyNouveau$enchanted(LivingEntity caster){return WandUtil.enchantedFocus(goetyNouveau$gear(caster));}
 @Redirect(method={"useSpell","SpellResult"},at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/WandUtil;getPotencyLevel(Lnet/minecraft/world/entity/LivingEntity;)I"))
 private int goetyNouveau$potency(LivingEntity caster){return WandUtil.getPotencyLevel(goetyNouveau$gear(caster));}
 @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;getFrontPos(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/phys/Vec3;"))
 private Vec3 goetyNouveau$front(Entity caster,double distance){return goetyNouveau$point(caster,MobUtil.getFrontPos(caster,distance));}
 @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;getRightPos(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/phys/Vec3;"))
 private Vec3 goetyNouveau$right(Entity caster,double distance){return goetyNouveau$point(caster,MobUtil.getRightPos(caster,distance));}
 @Redirect(method="SpellResult",at=@At(value="INVOKE",target="Lcom/Polarice3/Goety/utils/MobUtil;getLeftPos(Lnet/minecraft/world/entity/Entity;D)Lnet/minecraft/world/phys/Vec3;"))
 private Vec3 goetyNouveau$left(Entity caster,double distance){return goetyNouveau$point(caster,MobUtil.getLeftPos(caster,distance));}
 @Redirect(method={"useSpell","SpellResult"},at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
 private boolean goetyNouveau$spawn(net.minecraft.server.level.ServerLevel world,Entity entity){
  var v=GoetyNativeGeometry.current();if(v!=null && v.spell()==(Object)this && entity instanceof ScatterMine mine){
   if(!world.isInWorldBounds(mine.blockPosition()) || !world.hasChunkAt(mine.blockPosition()))return false;
   mine.setOwner(v.context().caster());GoetyAreaProvenance.mark(mine,v.context());boolean added=world.addFreshEntity(mine);
   if(added)GoetyNativeGeometry.observe(this,v.context().caster(),new EntityHitResult(mine));return added;
  }return world.addFreshEntity(entity);
 }
}
