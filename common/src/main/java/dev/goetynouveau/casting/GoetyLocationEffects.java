package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.AbsorbTrailParticleOption;
import com.Polarice3.Goety.client.particles.GatherTrailParticleOption;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.EarthFist;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeEffectHooks;
import dev.goetynouveau.platform.NativeLocationHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Target adaptations of the original MIT EarthFist, IceBouquet, Leeching and Launch spells.
 * Native entities and damage events retain animations, expiry, freezing and leech healing.
 * See THIRD_PARTY_NOTICES.md. The actual Ars caster supplies equipment, not the recipient. */
public final class GoetyLocationEffects {
    private GoetyLocationEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit = frame.target();
        if (hit instanceof EntityHitResult entity && (!(entity.getEntity() instanceof LivingEntity living)
                || !valid(world, living))) return false;
        var selected = hit instanceof EntityHitResult entity ? (LivingEntity)entity.getEntity() : null;
        var point = selected != null ? selected.position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !world.hasChunkAt(BlockPos.containing(point))) return false;
        return switch (focus) {
            case EARTH_FIST -> earthFist(world, frame, spell, defaults, ars, point);
            case GHOST_FIRE -> ghostFire(world, frame, spell, defaults, ars, point);
            case LEECHING -> leech(world, frame, spell, defaults, ars, selected);
            case LAUNCH -> launch(world, frame, spell, defaults, ars, selected);
            default -> false;
        };
    }
    private static boolean valid(ServerLevel world, LivingEntity target) {
        return target.isAlive() && !target.isRemoved() && target.level() == world;
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean enemy(CastContext frame, LivingEntity target) {
        return target != null && target != frame.caster() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
                && !MobUtil.areAllies(frame.caster(), target);
    }
    private static boolean earthFist(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                      SpellStats ars, Vec3 point) {
        var caster = frame.caster();
        float potency = Math.max(0, defaults.potency() + (float)ars.getAmpMultiplier());
        double radius = Math.max(0, defaults.radius() + ars.getAoeMultiplier());
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            radius *= NativeEffectHooks.radius(caster) / 2.0 + 1;
        }
        boolean added = false;
        if (spell.rightStaff(GoetyResources.staff(caster))) {
            var area = new AABB(point, point.add(0, -4, 0)).inflate(radius);
            for (var target : world.getEntitiesOfClass(LivingEntity.class, area,
                    entity -> valid(world, entity) && enemy(frame, entity)))
                added |= fist(world, frame, spell, target.position(), potency);
            if (added) return true;
        }
        return fist(world, frame, spell, point, potency);
    }
    private static boolean fist(ServerLevel world, CastContext frame, Spell spell, Vec3 point, float potency) {
        var fist = new EarthFist(world, point, owner(frame));
        fist.setExtraDamage(potency); fist.setWarmupDelayTicks(10);
        var direction = point.subtract(frame.caster().position());
        if (direction.horizontalDistanceSqr() < 1.0e-10) direction = frame.incomingDirection();
        if (direction.horizontalDistanceSqr() < 1.0e-10) direction = frame.caster().getLookAngle();
        fist.setYRot((float)Math.toDegrees(Math.atan2(direction.z, direction.x)));
        if (!world.addFreshEntity(fist)) return false;
        world.playSound(null, point.x, point.y, point.z, ModSounds.RUMBLE.get(), spell.getSoundSource(), 1, 1);
        return true;
    }
    private static boolean ghostFire(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                      SpellStats ars, Vec3 point) {
        var caster = frame.caster();
        float potency = Math.max(0, defaults.potency() + (float)ars.getAmpMultiplier());
        int duration = Math.max(0, defaults.duration() + (int)ars.getDurationMultiplier());
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster); duration += NativeEffectHooks.duration(caster);
        }
        var staff = GoetyResources.staff(caster);
        var shape = new ArrayList<Vec3>(); shape.add(Vec3.ZERO);
        if (NativeLocationHooks.ghostFireTower(staff)) {
            square(shape);
            for (var offset : List.of(new Vec3(0,0,3), new Vec3(0,0,-3), new Vec3(3,0,0), new Vec3(-3,0,0))) {
                shape.add(offset);
                shape.add(offset.add(offset.x == 0 ? 1.5 : 0,0,offset.z == 0 ? 1.5 : 0));
                shape.add(offset.add(offset.x == 0 ? -1.5 : 0,0,offset.z == 0 ? -1.5 : 0));
            }
        } else if (spell.rightStaff(staff)) {
            if (world.random.nextFloat() <= .05F) {
                shape.addAll(List.of(new Vec3(1.5,0,0),new Vec3(-1.5,0,0),new Vec3(0,0,1.5),new Vec3(0,0,-1.5)));
            } else square(shape);
        } else {
            // Native four-flower shape chooses one of four quadrants.
            int quadrant = world.random.nextInt(4);
            double x = quadrant < 2 ? 1.5 : -1.5, z = quadrant % 2 == 0 ? 1.5 : -1.5;
            shape.add(new Vec3(0,0,z)); shape.add(new Vec3(x,0,0)); shape.add(new Vec3(x,0,z));
        }
        boolean added = false;
        for (int i = 0; i < shape.size(); i++) {
            var position = point.add(shape.get(i)).add(0,.5,0);
            if (!world.hasChunkAt(BlockPos.containing(position))) continue;
            var flower = ModEntityType.ICE_BOUQUET.get().create(world);
            if (flower == null) continue;
            flower.setOwner(owner(frame)); flower.setPos(position);
            flower.setExtraDamage(potency); flower.addLifeSpan(MathHelper.secondsToTicks(duration));
            flower.setCenter(i == 0); MobUtil.moveDownToGround(flower);
            added |= world.addFreshEntity(flower);
        }
        if (added) SoundUtil.playNecromancerSummon(caster);
        return added;
    }
    private static void square(List<Vec3> shape) {
        // Native nine-flower cluster, retaining its center and 1.5-block spacing.
        for (double x : new double[]{-1.5,0,1.5}) for (double z : new double[]{-1.5,0,1.5})
            if (x != 0 || z != 0) shape.add(new Vec3(x,0,z));
    }
    private static boolean leech(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                 SpellStats ars, LivingEntity target) {
        if (!enemy(frame, target)) return false;
        var caster = frame.caster();
        float damage = SpellConfig.LeechingDamage.get().floatValue() * WandUtil.damageMultiply()
                + defaults.potency() + (float)ars.getAmpMultiplier();
        if (WandUtil.enchantedFocus(caster)) damage += WandUtil.getPotencyLevel(caster) / 2.0F;
        if (damage <= 0) return false;
        var color = new ColorUtil(ChatFormatting.DARK_RED);
        var destination = new Vec3(caster.getRandomX(1),caster.getEyeY(),caster.getRandomZ(1));
        var start = target.position().add(0,target.getBbHeight()/2.0,0);
        world.sendParticles(new GatherTrailParticleOption(color,destination),start.x,start.y,start.z,0,0,0,0,.5);
        for (int i=0;i<8;i++) world.sendParticles(new AbsorbTrailParticleOption(destination,0xaa0000,10),
                target.getRandomX(1),target.getRandomY(),target.getRandomZ(1),1,0,0,0,0);
        // The native LIFE_LEECH damage event owns healing and its configured percentage.
        if (!target.hurt(ModDamageSource.lifeLeech(owner(frame),owner(frame)),damage)) return false;
        if (spell.rightStaff(GoetyResources.staff(caster))) target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));
        spell.playSound(world,caster,ModSounds.SOUL_EAT.get());
        return true;
    }
    private static boolean launch(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                  SpellStats ars, LivingEntity target) {
        var caster = frame.caster();
        if (target == null || (target != caster && !MobUtil.areAllies(caster,target) && MobUtil.getOwner(target) != caster)) return false;
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        double potency = defaults.potency() + ars.getAmpMultiplier();
        if (WandUtil.enchantedFocus(caster)) potency += NativeLocationHooks.launchPotency(caster,staff);
        double power = NativeLocationHooks.launchPower(staff) + potency/2;
        if (!Double.isFinite(power) || power <= 0) return false;
        target.hurtMarked = true; target.setOnGround(false);
        target.setDeltaMovement(target.getLookAngle().scale(power)); target.hasImpulse = true; target.fallDistance = 0;
        spell.playSound(world,target,ModSounds.WIND.get(),2,1);
        return true;
    }
}
