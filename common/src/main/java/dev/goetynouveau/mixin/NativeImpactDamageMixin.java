package dev.goetynouveau.mixin;
import dev.arsconflux.api.projectile.ImpactTriggers;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
@Pseudo
@Mixin(targets={
    "com.Polarice3.Goety.common.entities.projectiles.ModFireball",
    "com.Polarice3.Goety.common.entities.projectiles.ModWitherSkull",
    "com.Polarice3.Goety.common.entities.projectiles.SoulBolt",
    "com.Polarice3.Goety.common.entities.projectiles.PoisonBolt",
    "com.Polarice3.Goety.common.entities.projectiles.WitherBolt",
    "com.Polarice3.Goety.common.entities.projectiles.NecroBolt",
    "com.Polarice3.Goety.common.entities.projectiles.MagicBolt",
    "com.Polarice3.Goety.common.entities.projectiles.HellBolt",
    "com.Polarice3.Goety.common.entities.projectiles.HellBlast",
    "com.Polarice3.Goety.common.entities.projectiles.PoisonQuill",
    "com.Polarice3.Goety.common.entities.projectiles.IceSpike",
    "com.Polarice3.Goety.common.entities.projectiles.IceSpear",
    "com.Polarice3.Goety.common.entities.projectiles.Lavaball",
    "com.Polarice3.Goety.common.entities.projectiles.SteamMissile",
    "com.Polarice3.Goety.common.entities.projectiles.BouncyBubble",
    "com.Polarice3.Goety.common.entities.projectiles.ElectroOrb",
    "com.Polarice3.Goety.common.entities.projectiles.SmackStone",
    "com.Polarice3.Goety.common.entities.projectiles.BlossomBall",
    "com.Polarice3.Goety.common.entities.projectiles.MagmaBomb",
    "com.Polarice3.Goety.common.entities.projectiles.VoidShockBomb",
    "com.Polarice3.Goety.common.entities.projectiles.SwordProjectile",
    "com.Polarice3.Goety.common.entities.projectiles.SlashProjectile",
    "com.Polarice3.Goety.common.entities.projectiles.RazorWind",
    "com.Polarice3.Goety.common.entities.projectiles.VoidSlash",
    "com.Polarice3.Goety.common.entities.projectiles.BlastFungus",
    "com.Polarice3.Goety.utils.SpellExplosion",
    "com.Polarice3.Goety.utils.FungusExplosion"
},remap=false)
public abstract class NativeImpactDamageMixin {
 @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
 private boolean goetyNouveau$entity(Entity target,DamageSource source,float amount,Operation<Boolean> original) {
  return ImpactTriggers.damage(target,()->original.call(target,source,amount));
 }
 @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true),require=0)
 private boolean goetyNouveau$living(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original) {
  return ImpactTriggers.damage(target,()->original.call(target,source,amount));
 }
}
