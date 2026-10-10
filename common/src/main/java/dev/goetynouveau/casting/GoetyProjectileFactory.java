package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.BoltFocus;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.platform.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.*;
import java.util.*;

/** Adapts the original MIT Goety spell launches; the native entities own their full impacts.
 * Parameters are deliberately per-spell: native enchantments do not share one scaling rule.
 * See THIRD_PARTY_NOTICES.md. */
public final class GoetyProjectileFactory {
    private GoetyProjectileFactory() {}
    public static List<Projectile> create(BoltFocus focus, ServerLevel world, CastContext frame,
                                          Vec3 carrierOrigin, Vec3 direction, SpellStats ars, float speed) {
        var caster=frame.caster(); var spell=(Spell)focus.spell(); var defaults=spell.defaultStats();
        var staff=GoetyResources.staff(caster); boolean right=spell.rightStaff(staff), enchanted=WandUtil.enchantedFocus(caster);
        int potency=defaults.getPotency()+(int)ars.getAmpMultiplier();
        int duration=defaults.getDuration()+(int)ars.getDurationMultiplier();
        int burning=defaults.getBurning()+ars.getBuffCount(AugmentBurning.INSTANCE);
        double radius=defaults.getRadius()+ars.getAoeMultiplier(); float velocity=defaults.getVelocity();
        int gearPotency=WandUtil.getPotencyLevel(caster), gearRadius=NativeEffectHooks.radius(caster);
        int gearDuration=NativeEffectHooks.duration(caster), gearVelocity=NativeBoltHooks.velocityLevel(caster);
        var origin=carrierOrigin.add(direction.x/2,-.1,direction.z/2);
        var result=new ArrayList<Projectile>();
        switch(focus) {
            case ICE_SPIKE -> {
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity/3F; }
                IceSpike spike=right?new IceSpear(caster,world):new IceSpike(caster,world);
                // Native arrow constructors start at eyeY - 0.1, without a forward offset.
                spike.setPos(carrierOrigin); spike.setExtraDamage(Math.max(0,potency));
                spike.shoot(direction.x,direction.y,direction.z,Math.max(.1F,velocity)*(right?1.5F:1)*speed,0);
                result.add(spike);
            }
            case LAVABALL -> {
                if(enchanted) { potency+=gearPotency; burning+=NativeSummonHooks.burning(caster); radius+=gearRadius/2F; }
                for(int i=0;i<(right?3:1);i++) {
                    var p=i==0?origin:origin.add(world.random.nextGaussian(),0,world.random.nextGaussian());
                    AbstractHurtingProjectile projectile;
                    if(CuriosFinder.hasUnholySet(caster)) {
                        var hell=new HellBlast(p.x,p.y,p.z,direction.x,direction.y,direction.z,world);
                        hell.setExtraDamage(Math.max(0,potency)); hell.setFiery(Math.max(0,burning));
                        hell.setRadius(hell.getRadius()+(float)Math.max(0,radius)); projectile=hell;
                    } else {
                        var lava=new Lavaball(world,p.x,p.y,p.z,direction.x,direction.y,direction.z);
                        lava.setExtraDamage(Math.max(0,potency)); lava.setFiery(Math.max(0,burning));
                        lava.setDangerous(!GoetyResources.sneaking(caster)); lava.setUpgraded(right);
                        lava.setExplosionPower(lava.getExplosionPower()+(float)Math.max(0,radius)); projectile=lava;
                    }
                    if(!(projectile instanceof WaterHurtingProjectile)) NativeProjectileHooks.speed(projectile,speed);
                    else projectile.setDeltaMovement(projectile.getDeltaMovement().scale(speed));
                    result.add(projectile);
                }
            }
            case STEAM -> {
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity; }
                var steam=new SteamMissile(origin.x,origin.y,origin.z,direction.x,direction.y,direction.z,world);
                steam.setExtraDamage(Math.max(0,potency)); steam.setBoltSpeed((int)velocity);
                steam.setDeltaMovement(steam.getDeltaMovement().scale(speed)); result.add(steam);
            }
            case BOUNCY_BUBBLE -> {
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity; radius+=gearRadius; }
                for(int i=0;i<(right?3:1);i++) {
                    var p=i==0?origin:origin.add(world.random.nextGaussian(),0,world.random.nextGaussian());
                    var bubble=new BouncyBubble(p.x,p.y,p.z,direction.x,direction.y,direction.z,world);
                    bubble.shoot(direction); bubble.setExtraDamage(Math.max(0,potency)); bubble.setBoltSpeed((int)velocity);
                    bubble.setSize((int)Math.max(0,radius)); bubble.setDeltaMovement(bubble.getDeltaMovement().scale(speed)); result.add(bubble);
                }
            }
            case ELECTRO_ORB -> {
                if(enchanted) potency+=gearPotency;
                var target=target(world,caster,origin,direction,defaults.getRange()+(enchanted?WandUtil.getRangeLevel(caster):0));
                var orb=new ElectroOrb(world,caster,target); orb.setPos(origin);
                orb.setExtraDamage(Math.max(0,potency)); orb.setStaff(right);
                orb.shoot(direction.x,direction.y,direction.z,.66F*speed,0); result.add(orb);
            }
            case SURGING -> {
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity; }
                var p=origin.add(0,-.3,0);
                var orb=new SurgingOrb(p.x,p.y,p.z,direction.x,direction.y,direction.z,world);
                orb.setBoltSpeed((int)velocity); orb.setExtraDamage(Math.max(0,potency));
                orb.setOrange(spell.typeStaff(staff,SpellType.NETHER)); orb.setStaff(right);
                orb.setTarget(target(world,caster,p,direction,defaults.getRange()));
                orb.setDeltaMovement(orb.getDeltaMovement().scale(speed)); result.add(orb);
            }
            case SMACK_STONE -> {
                // Upstream reads initial fiery intensity from duration, not burning.
                burning=defaults.getDuration()+ars.getBuffCount(AugmentBurning.INSTANCE);
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity/3F; burning+=NativeSummonHooks.burning(caster); }
                for(int i=0;i<(right?3:1);i++) {
                    var stone=new SmackStone(caster,world); stone.setPos(carrierOrigin);
                    stone.setExtraDamage(Math.max(0,potency)); stone.setFiery(Math.max(0,burning));
                    stone.shoot(direction.x,direction.y,direction.z,Math.max(.1F,velocity)*speed,i==0?0:16); result.add(stone);
                }
            }
            case BLOSSOM, MAGMA -> {
                // These native spells read gear bonuses unconditionally.
                potency+=gearPotency; duration+=gearDuration; velocity+=gearVelocity/3F;
                radius+=gearRadius/2F+(right?(focus==BoltFocus.BLOSSOM?1.5F:1):0);
                SpellThrowableProjectile projectile;
                if(focus==BoltFocus.BLOSSOM) {
                    var blossom=new BlossomBall(caster,world); blossom.setRadius(Math.max(0,radius));
                    blossom.setDuration(Math.max(0,duration)); projectile=blossom;
                } else {
                    var magma=new MagmaBomb(caster,world); magma.setExplosionPower((float)Math.max(0,radius));
                    magma.setDuration(Math.max(0,duration)); projectile=magma;
                }
                projectile.setPos(carrierOrigin); projectile.setExtraDamage(Math.max(0,potency)); projectile.setStaff(right);
                projectile.shoot(direction.x,direction.y,direction.z,Math.max(.1F,velocity)*speed,0); result.add(projectile);
            }
            case VOID_BOMB -> {
                if(enchanted) { potency+=gearPotency; velocity+=gearVelocity/3F; }
                // Native offset is above the head; keep it relative to the Ars launch origin.
                var p=carrierOrigin.add(0,caster.getBbHeight()+1-(caster.getEyeY()-caster.getY())+.1,0);
                for(int i=0;i<(right?3:1);i++) {
                    var bomb=new VoidShockBomb(caster,world); bomb.setPos(p); bomb.setExtraDamage(Math.max(0,potency));
                    bomb.shoot(direction.x,direction.y,direction.z,Math.max(.1F,i==0?velocity:velocity/2+world.random.nextFloat())*speed,i==0?0:8);
                    result.add(bomb);
                }
            }
            case SWORD -> {
                var weapon=sword(caster); if(weapon.isEmpty()) return List.of();
                if(enchanted) velocity+=gearVelocity/3F;
                var sword=new SwordProjectile(caster,world,weapon); sword.setPos(carrierOrigin.add(0,caster.getBbHeight()/2-(caster.getEyeY()-caster.getY())+.1,0));
                sword.pickup=AbstractArrow.Pickup.CREATIVE_ONLY;
                sword.shoot(direction.x,direction.y,direction.z,Math.max(.1F,velocity)*speed,0); result.add(sword);
            }
            case RAZOR_WIND -> {
                if(right) radius+=.5F;
                if(enchanted) { radius+=gearRadius/4F; potency+=gearPotency; }
                SlashProjectile slash=spell.typeStaff(staff,SpellType.VOID)?new VoidSlash(world,caster):new RazorWind(world,caster);
                slash.setPos(carrierOrigin.add(0,.1,0)); slash.slash(direction,2F*speed);
                // Native Slash copies its caster's rotation and does not rotate in tick.
                // Scatter or a secondary Ars carrier may aim elsewhere than that caster.
                slash.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
                slash.setXRot((float)Math.toDegrees(Math.atan2(-direction.y,Math.sqrt(direction.x*direction.x+direction.z*direction.z))));
                slash.setRadius(.3F+(float)Math.max(0,radius)); slash.setMaxRadius(slash.getMaxRadius()+(float)Math.max(0,radius));
                slash.setDamage(Math.max(0,SpellConfig.RazorWindDamage.get().floatValue()*WandUtil.damageMultiply()+potency));
                slash.setMaxLifeSpan(40); result.add(slash);
            }
            default -> throw new IllegalArgumentException("Not an additional projectile: "+focus);
        }
        return result;
    }
    private static LivingEntity target(ServerLevel world, LivingEntity caster, Vec3 origin, Vec3 direction, int range) {
        var end=origin.add(direction.scale(Math.max(1,range)));
        var block=world.clip(new net.minecraft.world.level.ClipContext(origin,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,caster));
        if(block.getType()!=HitResult.Type.MISS) end=block.getLocation();
        var hit=ProjectileUtil.getEntityHitResult(world,caster,origin,end,new AABB(origin,end).inflate(1),
                e->e instanceof LivingEntity living && living.isAlive() && !living.isSpectator() && !MobUtil.areAllies(caster,living));
        return hit!=null && hit.getEntity() instanceof LivingEntity living?living:null;
    }
    public static ItemStack sword(LivingEntity caster) {
        for(var stack:List.of(caster.getMainHandItem(),caster.getOffhandItem()))
            if(stack.getItem() instanceof SwordItem || stack.is(ModTags.Items.MAGIC_SWORD_SHOOTABLE)) return stack;
        return ItemStack.EMPTY;
    }
    public static boolean wearSword(LivingEntity caster) {
        var stack=sword(caster); if(stack.isEmpty()) return false;
        if(MobUtil.validEntity(caster)) ItemHelper.hurtAndBreak(stack,10,caster);
        return true;
    }
    public static void sound(BoltFocus focus, ServerLevel world, LivingEntity caster) {
        var spell=(Spell)focus.spell();
        switch(focus) {
            case ICE_SPIKE -> spell.playSound(world,caster,ModSounds.ICE_SPIKE_CAST.get(),1,spell.projPitch(world.random));
            case LAVABALL -> spell.playSound(world,caster,SoundEvents.GHAST_SHOOT,2,spell.projPitch(world.random));
            case STEAM, BOUNCY_BUBBLE -> {} // Native result has no launch sound.
            case ELECTRO_ORB -> spell.playSound(world,caster,ModSounds.SHOCK_CAST.get());
            case SURGING -> spell.playSound(world,caster,ModSounds.SHOCK_CAST.get(),1,spell.projPitch(world.random));
            case SMACK_STONE -> spell.playSound(world,caster,SoundEvents.BASALT_PLACE,1,spell.projPitch(world.random));
            case BLOSSOM -> spell.playSound(world,caster,ModSounds.BLAST_FUNGUS_THROW.get(),1,.75F);
            case MAGMA -> spell.playSound(world,caster,ModSounds.CAST_SPELL.get());
            case VOID_BOMB -> { spell.playSound(world,caster,ModSounds.HEAVY_WOOSH.get(),3,caster.getVoicePitch()); spell.playSound(world,caster,ModSounds.TELEPORT_ORB_THROW.get(),3,caster.getVoicePitch()-.5F); }
            case SWORD -> spell.playSound(world,caster,1,1);
            case RAZOR_WIND -> spell.playSound(world,caster,ModSounds.WIND.get(),2,1.5F);
            default -> throw new IllegalArgumentException("Not an additional projectile: "+focus);
        }
    }
}
