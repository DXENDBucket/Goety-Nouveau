package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.SphereExplodeParticleOption;
import com.Polarice3.Goety.common.entities.util.FirePillar;
import com.Polarice3.Goety.common.entities.util.VoidRift;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.platform.NativeAreaHooks;
import dev.goetynouveau.platform.NativeEffectHooks;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** MIT native area spells adapted to the Ars hit. Native entities retain their complete
 * active/closing lifecycle; preparation belongs to Conflux, not a second native cast.
 * See THIRD_PARTY_NOTICES.md. */
public final class GoetyAreaEffects {
    private GoetyAreaEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit = frame.target();
        if (hit == null || hit.getType() == HitResult.Type.MISS) return false;
        if (hit instanceof EntityHitResult entity && (!entity.getEntity().isAlive()
                || entity.getEntity().level() != world)) return false;
        var point = hit instanceof EntityHitResult entity ? entity.getEntity().position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !loaded(world, point)) return false;
        return switch (focus) {
            case FIRE_BLAST -> fireBlast(world, frame, spell, defaults, ars, point);
            case SONIC_BOOM -> sonicBoom(world, frame, spell, defaults, ars, point);
            case FLAME_STRIKE -> flameStrike(world, frame, spell, defaults, ars, point);
            case VOID_RIFT -> voidRift(world, frame, spell, defaults, ars, point);
            default -> false;
        };
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean loaded(ServerLevel world, Vec3 point) {
        var pos = BlockPos.containing(point);
        return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos) && world.getWorldBorder().isWithinBounds(pos);
    }
    private static boolean hostile(CastContext frame, LivingEntity target) {
        return target != frame.caster() && target != owner(frame) && target.isAlive()
                && !dev.arsconflux.api.targeting.Allies.areAllies(frame.caster(), target) && !dev.arsconflux.api.targeting.Allies.areAllies(owner(frame), target)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target);
    }
    private static boolean fireBlast(ServerLevel world, CastContext frame, Spell spell,
                                     NativeSpellStats defaults, SpellStats ars, Vec3 point) {
        var caster = frame.caster(); var owner = owner(frame);
        float potency = defaults.potency() + (float)ars.getAmpMultiplier();
        double area = defaults.radius() + ars.getAoeMultiplier();
        int burning = defaults.burning() + ars.getBuffCount(AugmentBurning.INSTANCE);
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster) / 2F;
            area += NativeEffectHooks.radius(caster);
            burning += NativeSummonHooks.burning(caster);
        }
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        float radius = (float)area + (staff ? .5F : 0);
        float minimum = SpellConfig.FireBlastDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        float maximum = Math.max(minimum, SpellConfig.FireBlastMaxDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
        float damage = Math.min(maximum, minimum + RandomUtil.nextInt(world.random, Math.max(0, (int)(maximum-minimum))));
        if (!Float.isFinite(radius) || radius <= 0 || !Float.isFinite(damage) || damage <= 0) return false;
        var color = new ColorUtil(14523414);
        NativeAreaHooks.fireBlastParticles(world, point, radius, staff, color);
        world.sendParticles(new SphereExplodeParticleOption(color.red(), color.green(), color.blue(), radius, 1),
                point.x, point.y+.5, point.z, 1, 0, 0, 0, 0);
        DamageSource source = ModDamageSource.fireBreath(owner, owner);
        if (CuriosFinder.hasNetherRobe(caster) || (MobUtil.getOwner(caster) != null
                && CuriosFinder.hasNetherRobe(MobUtil.getOwner(caster)))) source = ModDamageSource.magicFireBreath(owner, owner);
        if (CuriosFinder.hasUnholySet(caster)) source = ModDamageSource.hellfire(owner, owner);
        int seconds = 5 * (Math.max(0, burning) + 1);
        new SpellExplosion(world, owner, source, point.x, point.y, point.z, radius, damage) {
            @Override public void explodeHurt(Entity target, DamageSource source, double x, double y, double z, double seen, float actual) {
                if (!(target instanceof LivingEntity living) || !hostile(frame, living)) return;
                super.explodeHurt(target, source, x, y, z, seen, actual);
                if (!living.fireImmune()) NativeAreaHooks.ignite(living, seconds);
            }
        };
        world.playSound(null, point.x, point.y, point.z, NativeAreaHooks.explosionSound(), spell.getSoundSource(), 2, 1);
        world.playSound(null, point.x, point.y, point.z, ModSounds.HELL_BLAST_IMPACT.get(), spell.getSoundSource(), 2, 1);
        return true;
    }
    private static boolean sonicBoom(ServerLevel world, CastContext frame, Spell spell,
                                     NativeSpellStats defaults, SpellStats ars, Vec3 point) {
        var caster = frame.caster(); var owner = owner(frame);
        LivingEntity target = frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living != caster ? living : null;
        if (target != null && !hostile(frame, target)) return false;
        float potency = defaults.potency() + (float)ars.getAmpMultiplier();
        if (WandUtil.enchantedFocus(caster)) potency += WandUtil.getPotencyLevel(caster);
        float damage = SpellConfig.SonicBoomDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        if (!Float.isFinite(damage) || damage <= 0) return false;
        var start = caster.getEyePosition();
        Vec3 delta;
        if (frame.target() instanceof EntityHitResult hit && hit.getEntity() == caster) {
            var direction = frame.incomingDirection();
            if (direction == null || direction.lengthSqr() < 1.0E-8) direction = caster.getViewVector(1);
            delta = direction.normalize().scale(Math.max(1, defaults.range() + WandUtil.getRangeLevel(caster)));
        } else delta = (target != null ? target.getEyePosition() : point).subtract(start);
        if (!Double.isFinite(delta.lengthSqr())) return false;
        var direction = delta.lengthSqr() > 1.0E-8 ? delta.normalize() : caster.getViewVector(1);
        // Relative distance, not the native empty-target branch's absolute world position.
        int length = (int)Math.ceil(delta.length()) + 6;
        for (int i = 1; i <= length; i++) {
            var pos = start.add(direction.scale(i));
            if (!loaded(world, pos)) break;
            world.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
        spell.playSound(world, caster, SoundEvents.WARDEN_SONIC_BOOM, 3, 1);
        if (target == null) return true; // Location triggers still emit the native beam; never re-select a view target.
        if (!target.hurt(world.damageSources().sonicBoom(owner), damage)) return false;
        double resistance = 1 - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        MobUtil.push(target, direction.x * 2.5 * resistance, direction.y * .5 * resistance, direction.z * 2.5 * resistance);
        return true;
    }
    private static int duration(NativeSpellStats defaults, SpellStats ars, LivingEntity caster) {
        double ticks = defaults.duration() * (1 + ars.getDurationMultiplier()) * (1L + NativeEffectHooks.duration(caster));
        return Double.isFinite(ticks) && ticks > 0 ? (int)Math.min(Integer.MAX_VALUE, ticks) : 0;
    }
    private static float potency(NativeSpellStats defaults, SpellStats ars, LivingEntity caster) {
        return Math.max(0, defaults.potency() + (float)ars.getAmpMultiplier() + WandUtil.getPotencyLevel(caster));
    }
    private static boolean flameStrike(ServerLevel world, CastContext frame, Spell spell,
                                       NativeSpellStats defaults, SpellStats ars, Vec3 point) {
        int duration = duration(defaults, ars, frame.caster());
        double radius = defaults.radius() + ars.getAoeMultiplier();
        if (duration <= 0 || !Double.isFinite(radius) || radius <= 0) return false;
        boolean staff = spell.rightStaff(GoetyResources.staff(frame.caster()));
        float potency = potency(defaults, ars, frame.caster());
        boolean added = false;
        if (frame.target() instanceof EntityHitResult hit && hit.getEntity() != frame.caster()
                && !GoetyResources.sneaking(frame.caster())) {
            for (var pos : BlockFinder.buildOuterBlockCircle(point, radius)) added |= pillar(world, frame, pos, duration, potency);
            if (staff) for (var pos : BlockFinder.buildOuterBlockCircle(point, radius*2))
                added |= pillar(world, frame, pos, duration, potency);
        } else {
            var direction = frame.incomingDirection();
            if (direction == null || direction.lengthSqr() < 1.0E-8) direction = frame.caster().getViewVector(1);
            double angle = Math.atan2(direction.z, direction.x), scale = radius / defaults.radius();
            int[] rings = staff ? new int[]{1,3,6,9} : new int[]{1,3};
            for (int ring : rings) for (int k=0; k<(ring == 9 ? 16 : 8); k++) {
                double a = angle + k*Math.PI*.25 + ring;
                added |= pillar(world, frame, point.add(Math.cos(a)*ring*scale,0,Math.sin(a)*ring*scale), duration, potency);
            }
        }
        return added;
    }
    private static boolean pillar(ServerLevel world, CastContext frame, Vec3 point, int duration, float potency) {
        if (!loaded(world, point)) return false;
        var block = BlockPos.containing(point).above();
        // Loaded column only; native ground placement, including fractional collision height.
        for (int y=block.getY(); y>world.getMinBuildHeight(); y--) {
            var pos = new BlockPos(block.getX(),y,block.getZ());
            var floor = world.getBlockState(pos.below()).getCollisionShape(world,pos.below());
            if (floor.isEmpty()) continue;
            double top = y-1 + floor.max(net.minecraft.core.Direction.Axis.Y);
            var entity = new FirePillar(world, point.x, top, point.z);
            if (!world.getWorldBorder().isWithinBounds(entity.getBoundingBox())
                    || world.getBlockCollisions(entity,entity.getBoundingBox()).iterator().hasNext()) return false;
            entity.setOwner(owner(frame)); entity.setDuration(duration); entity.setWarmUp(0); entity.setExtraDamage(potency);
            return world.addFreshEntity(entity);
        }
        return false;
    }
    private static boolean voidRift(ServerLevel world, CastContext frame, Spell spell,
                                    NativeSpellStats defaults, SpellStats ars, Vec3 point) {
        int duration = duration(defaults, ars, frame.caster());
        double radius = defaults.radius() + ars.getAoeMultiplier() + NativeEffectHooks.radius(frame.caster());
        if (duration <= 0 || !Double.isFinite(radius) || radius < 0) return false;
        var entity = new VoidRift(world, point.x, point.y, point.z);
        entity.setOwner(owner(frame)); entity.setDuration(duration); entity.setWarmUp(0);
        entity.setSize((float)radius); entity.setExtraDamage(potency(defaults, ars, frame.caster()));
        entity.setStaff(spell.rightStaff(GoetyResources.staff(frame.caster())));
        ((RiftVisualState)entity).goetyNouveau$visualPhase(RiftVisualState.ACTIVE);
        return world.addFreshEntity(entity);
    }
}
