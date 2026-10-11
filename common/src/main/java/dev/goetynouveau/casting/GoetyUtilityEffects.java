package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.*;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.spells.geomancy.RotationSpell;
import com.Polarice3.Goety.common.magic.spells.storm.ChargeSpell;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import java.util.HashMap;
import java.util.UUID;

/** Target adaptations of native sensing, rotation, wind horn and charge spells.
 * Native effect/event consumers and charge chaining remain in Goety. */
public final class GoetyUtilityEffects {
    private GoetyUtilityEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit=frame.target(); var caster=frame.caster();
        var point=hit instanceof EntityHitResult e ? e.getEntity().position() : hit.getLocation();
        if(!Double.isFinite(point.lengthSqr()) || !world.hasChunkAt(BlockPos.containing(point))) return false;
        LivingEntity target=hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l
                && l.isAlive() && l.level()==world ? l : null;
        var staff=GoetyResources.staff(caster);
        int potency=Math.max(0,defaults.potency()+(int)ars.getAmpMultiplier());
        int duration=Math.max(0,defaults.duration()+(int)ars.getDurationMultiplier());
        int radius=Math.max(0,(int)(defaults.radius()+ars.getAoeMultiplier()));
        if(WandUtil.enchantedFocus(caster)) {
            potency+=WandUtil.getPotencyLevel(caster);
            duration+=NativeEffectHooks.duration(caster);
            radius+=NativeEffectHooks.radius(caster);
        }
        return switch(focus) {
            case SENSING -> {
                if(target==null || duration<=0 || (target!=caster && !MobUtil.areAllies(caster,target))) yield false;
                boolean changed=target.addEffect(NativeEffectHooks.sensing(MathHelper.secondsToTicks(duration),potency));
                if(changed) spell.playSound(world,target,ModSounds.DIRT_DEBRIS.get());
                yield changed;
            }
            case ROTATION -> {
                if(!(hit instanceof BlockHitResult block)) yield false;
                if(caster instanceof net.minecraft.world.entity.player.Player player
                        && !world.mayInteract(player,block.getBlockPos())) yield false;
                boolean rotated=RotationSpell.blockCanRotate(world,block.getBlockPos(),block.getDirection());
                if(rotated) ((RotationSpell)spell).blockResult(world,caster,staff,block.getBlockPos(),block.getDirection(),defaults.nativeCopy());
                yield rotated;
            }
            case CHARGE -> {
                if(target==null) yield false;
                // Native chainCharge may affect nearby recipients rather than the primary target.
                // Observe its whole reachable region so a no-op does not commit a trigger charge.
                var recipients=spell.rightStaff(staff)
                        ? world.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(world.isThundering()?48:24))
                        : java.util.List.of(target);
                var before=new HashMap<UUID,ChargeState>();
                for(var entity:recipients) before.put(entity.getUUID(),ChargeState.of(entity));
                ((ChargeSpell)spell).touchResult(world,caster,target,staff,defaults.nativeCopy());
                yield recipients.stream().anyMatch(entity->!ChargeState.of(entity).equals(before.get(entity.getUUID())));
            }
            case TREMBLING -> trembling(world,frame,spell,point,potency,duration,spell.rightStaff(staff)?radius*2:radius);
            default -> false;
        };
    }
    private record ChargeState(float health,float absorption,int amplifier,int duration) {
        static ChargeState of(LivingEntity entity) {
            var effect=NativeEffectHooks.charged(entity);
            return new ChargeState(entity.getHealth(),entity.getAbsorptionAmount(),effect==null?-1:effect.getAmplifier(),effect==null?0:effect.getDuration());
        }
    }
    private static boolean trembling(ServerLevel world,CastContext frame,Spell spell,Vec3 point,int potency,int duration,int radius) {
        if(radius<=0 || duration<=0) return false;
        var caster=frame.caster(); var color=new ColorUtil(0xFFFFFF);
        world.sendParticles(new CircleExplodeParticleOption(color.red(),color.green(),color.blue(),radius*2,radius),point.x,point.y+.5,point.z,0,0,0,0,0);
        world.sendParticles(new SmashParticleOption(color,radius,10),point.x,point.y+1,point.z,1,0,0,0,0);
        var bounds=frame.target() instanceof EntityHitResult e ? e.getEntity().getBoundingBox() : new AABB(point,point);
        for(var enemy:world.getEntitiesOfClass(LivingEntity.class,bounds.inflate(radius))) {
            if(enemy==caster || MobUtil.areAllies(caster,enemy) || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(enemy)) continue;
            double dx=enemy.getX()-point.x,dz=enemy.getZ()-point.z,divisor=Math.max(dx*dx+dz*dz,.001);
            MobUtil.push(enemy,dx/divisor*(4+potency),.2,dz/divisor*(4+potency));
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,MathHelper.secondsToTicks(duration),potency));
            enemy.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,MathHelper.secondsToTicks(duration),potency));
        }
        world.playSound(null,point.x,point.y,point.z,ModSounds.WIND_HORN.get(),spell.getSoundSource(),3,1);
        return true;
    }
}
