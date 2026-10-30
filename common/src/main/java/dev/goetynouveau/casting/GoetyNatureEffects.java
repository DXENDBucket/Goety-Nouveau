package dev.goetynouveau.casting;

import dev.arsconflux.api.execution.ExecutionAnchor;

import com.Polarice3.Goety.api.blocks.entities.IWindPowered;
import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.client.particles.*;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.neutral.AbstractMonolith;
import com.Polarice3.Goety.common.entities.projectiles.AbstractCyclone;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Ars target adaptations of the MIT WindBlast and Overgrowth implementations.
 * Native entities, particles, staff branches and utility geometry remain in Goety. */
public final class GoetyNatureEffects {
    private GoetyNatureEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit=frame.target();
        if (hit instanceof EntityHitResult e && (!e.getEntity().isAlive() || e.getEntity().level()!=world)) return false;
        var point=hit instanceof EntityHitResult e ? e.getEntity().position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !world.hasChunkAt(BlockPos.containing(point))) return false;
        return focus==EffectFocus.WIND_BLAST ? wind(world,frame,spell,defaults,ars,point)
                : overgrowth(world,frame,spell,defaults,ars,point);
    }
    private static Vec3 direction(CastContext frame) {
        var direction=frame.incomingDirection();
        return direction.lengthSqr()>1e-10 ? direction.normalize() : ExecutionAnchor.viewDirection(frame.caster());
    }
    private static boolean wind(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                 SpellStats ars, Vec3 point) {
        var caster=frame.caster(); var staff=GoetyResources.staff(caster); var direction=direction(frame);
        boolean self=frame.target() instanceof EntityHitResult e && e.getEntity()==caster;
        var origin=self ? caster.getEyePosition() : point;
        double knock=Math.max(0,defaults.potency()+ars.getAmpMultiplier());
        int range=Math.max(0,defaults.range()+(int)ars.getAoeMultiplier());
        if(spell.rightStaff(staff)) { range*=2; knock*=2; }
        if(WandUtil.enchantedFocus(caster)) { knock+=WandUtil.getPotencyLevel(caster)/4.0; range+=WandUtil.getRangeLevel(caster); }
        if(range<=0) return false;
        for(int i=0;i<16;i++) {
            double speed=(.3+range/10.0)*(1+world.random.nextDouble());
            var pos=origin.add(direction.scale(.9)).add(world.random.nextGaussian()/2,world.random.nextGaussian()/2,world.random.nextGaussian()/2);
            var velocity=direction.scale(speed);
            world.sendParticles(new WindBlowParticleOption(ColorUtil.WHITE,1+world.random.nextInt(4),world.random.nextFloat()*.5F),
                    pos.x,pos.y,pos.z,0,velocity.x,velocity.y,velocity.z,1);
        }
        for(int i=1;i<range;i++) {
            var pos=origin.add(direction.scale(i));
            if(spell.typeStaff(staff,SpellType.FROST)) world.sendParticles(ModParticleTypes.FROST.get(),pos.x,pos.y,pos.z,1,0,0,0,0);
            if(spell.typeStaff(staff,SpellType.WILD)) world.sendParticles(ModParticleTypes.FLY.get(),pos.x,pos.y,pos.z,1,0,0,0,0);
            if(spell.typeStaff(staff,SpellType.NETHER)) world.sendParticles(ParticleTypes.FLAME,pos.x,pos.y,pos.z,1,0,0,0,0);
        }
        var clip=world.clip(new ClipContext(origin,origin.add(direction.scale(range)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster));
        var block=frame.target() instanceof BlockHitResult b ? b.getBlockPos() : clip.getBlockPos();
        if((frame.target() instanceof BlockHitResult || clip.getType()!=HitResult.Type.MISS) && world.hasChunkAt(block) && world.getBlockEntity(block) instanceof IWindPowered powered) powered.activate(300);
        var area=(self ? caster.getBoundingBox() : new AABB(origin,origin)).inflate(1).expandTowards(direction.scale(range));
        for(var entity:world.getEntities(caster,area)) {
            if(dev.arsconflux.api.targeting.Allies.areAllies(caster,entity) || entity.getType().is(ModTags.EntityTypes.UNBLOWABLE_ENTITIES)
                    || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)) continue;
            var los=world.clip(new ClipContext(origin,entity.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,caster));
            if(los.getType()!=HitResult.Type.MISS) continue;
            // Same native resistance/motion rule, with the Ars origin instead of the distant caster.
            var from=self ? caster.position() : origin.subtract(direction);
            var away=entity.position().subtract(from).normalize();
            double resistance=entity instanceof LivingEntity living ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 0;
            entity.setDeltaMovement(entity.getDeltaMovement().add(away.x*2*knock,Math.max(away.y,.2*knock),away.z*2*knock).scale(Math.max(0,1-resistance)));
            entity.hasImpulse=true;
            if(entity instanceof Player player) { player.hurtMarked=true; player.setOnGround(false); }
            if(entity instanceof LivingEntity living) NativeEffectHooks.windStaff(spell,caster,staff,living);
            if(entity instanceof AbstractCyclone cyclone && (cyclone.getTrueOwner()==null || !dev.arsconflux.api.targeting.Allies.areAllies(caster,cyclone.getTrueOwner()))) cyclone.trueRemove();
        }
        world.playSound(null,origin.x,origin.y,origin.z,ModSounds.WIND_BLAST.get(),spell.getSoundSource(),.3F,1.3F);
        return true;
    }
    private static boolean overgrowth(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                       SpellStats ars, Vec3 point) {
        var caster=frame.caster(); var staff=GoetyResources.staff(caster);
        LivingEntity target=frame.target() instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l ? l : null;
        if(target!=null && (target==caster || dev.arsconflux.api.targeting.Allies.areAllies(caster,target) || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target))) return false;
        var pos=frame.target() instanceof BlockHitResult b ? b.getBlockPos() : BlockPos.containing(point);
        if(pos.getY()<world.getMinBuildHeight() || pos.getY()+8>=world.getMaxBuildHeight()) return false;
        // Native structures extend a few blocks from the selected point. Do not force remote chunks.
        for(int x=-8;x<=8;x+=8) for(int z=-8;z<=8;z+=8) if(!world.hasChunkAt(pos.offset(x,0,z))) return false;
        int potency=Math.max(0,defaults.potency()+(int)ars.getAmpMultiplier());
        int duration=Math.max(0,defaults.duration()+(int)ars.getDurationMultiplier());
        if(WandUtil.enchantedFocus(caster)) { potency+=WandUtil.getPotencyLevel(caster); duration+=NativeEffectHooks.duration(caster); }
        var area=new AABB(Vec3.atCenterOf(pos),Vec3.atCenterOf(pos)).inflate(16);
        Set<UUID> before=world.getEntitiesOfClass(AbstractMonolith.class,area).stream().map(Entity::getUUID).collect(Collectors.toSet());
        if(GoetyResources.sneaking(caster)) {
            if(target!=null) {
                var side=new Vec3(-direction(frame).z,0,direction(frame).x).normalize().scale(2);
                WandUtil.summonTurret(caster,pos.offset((int)side.x,0,(int)side.z),ModEntityType.POISON_QUILL_VINE.get(),target,duration,potency);
                if(spell.rightStaff(staff)) WandUtil.summonTurret(caster,pos.offset((int)-side.x,0,(int)-side.z),ModEntityType.POISON_QUILL_VINE.get(),target,duration,potency);
            } else WandUtil.summonTurret(caster,pos,ModEntityType.POISON_QUILL_VINE.get(),null,duration,potency);
        } else if(target==null) WandUtil.summonCubeTrap(caster,pos,ModEntityType.QUICK_GROWING_VINE.get(),duration);
        else {
            var type=ModEntityType.QUICK_GROWING_VINE.get(); var facing=Direction.fromYRot(target.getYHeadRot());
            switch(world.random.nextInt(5)) {
                case 0 -> WandUtil.summonMinorSquareTrap(caster,target,type,facing,duration);
                case 1 -> WandUtil.summonHallTrap(caster,target,type,duration);
                case 2 -> WandUtil.summonCubeTrap(caster,target,type,duration);
                case 3 -> WandUtil.summonCircleTrap(caster,target,type,facing,duration);
                default -> WandUtil.summonSurroundTrap(caster,target,type,duration);
            }
        }
        return world.getEntitiesOfClass(AbstractMonolith.class,area).stream().anyMatch(e->!before.contains(e.getUUID()));
    }
}
