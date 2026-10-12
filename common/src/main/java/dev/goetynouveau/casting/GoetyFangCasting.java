package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.Fangs;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.glyph.AugmentGoety;
import dev.goetynouveau.platform.NativeEffectHooks;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Adapt the upstream MIT FangSpell/WandUtil geometry to the Ars hit point. The real native
 * entity retains warmup, animation, damage, alliance checks, global lifesteal and soul gains.
 * See THIRD_PARTY_NOTICES.md. No temporary focus enchantments or duplicate damage handlers. */
public final class GoetyFangCasting {
    private GoetyFangCasting() {}
    private record Parameters(int range, int potency, int burning, int soulEater, boolean absorbing) {}
    public static boolean apply(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults, SpellStats ars) {
        var caster = frame.caster();
        var parameters = parameters(caster, defaults, ars);
        var center = frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target
                ? target.position() : frame.target().getLocation();
        var direction = frame.incomingDirection();
        if (direction.horizontalDistanceSqr() < 1.0e-10) direction = caster.getViewVector(1);
        float angle = (float)Math.atan2(direction.z, direction.x);
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        boolean added = false;
        if (!spell.isShifting(caster)) {
            var origin = center.subtract(new Vec3(Mth.cos(angle) * 1.25, 0, Mth.sin(angle) * 1.25));
            for (int index = 0; index < parameters.range(); ++index) {
                // The first fang is on the Ars-selected target, rather than behind/after it.
                added |= line(world, frame, origin, angle, index, parameters);
                if (staff) {
                    added |= line(world, frame, origin, angle + .2F, index, parameters);
                    added |= line(world, frame, origin, angle - .2F, index, parameters);
                }
            }
        } else {
            added |= ring(world, frame, center, angle, 5, 1.5, (float)Math.PI * .4F, 0, 0, parameters);
            added |= ring(world, frame, center, angle, 8, 2.5, (float)Math.PI * 2 / 8, 1.2566371F, 3, parameters);
            if (staff) {
                added |= ring(world, frame, center, angle, 11, 3.5, (float)Math.PI * 4 / 16, 2.5133462F, 6, parameters);
                added |= ring(world, frame, center, angle, 14, 4.5, (float)Math.PI * 8 / 32, 5.0266924F, 9, parameters);
            }
        }
        if (added) world.playSound(null, center.x, center.y, center.z, SoundEvents.EVOKER_CAST_SPELL, spell.getSoundSource(), 1, 1);
        return added;
    }
    private static Parameters parameters(LivingEntity caster, NativeSpellStats defaults, SpellStats ars) {
        int range = defaults.range() + ars.getBuffCount(AugmentGoety.EXTENSION);
        int potency = Math.max(0, defaults.potency() + (int)ars.getAmpMultiplier());
        int burning = defaults.burning() + ars.getBuffCount(AugmentBurning.INSTANCE);
        int soulEater = ars.getBuffCount(AugmentGoety.SOUL_EATER);
        boolean absorbing = ars.getBuffCount(AugmentGoety.LIFE_ABSORB) > 0;
        if (WandUtil.enchantedFocus(caster)) {
            range += WandUtil.getRangeLevel(caster);
            potency += WandUtil.getPotencyLevel(caster);
            burning += NativeSummonHooks.burning(caster);
            soulEater += NativeEffectHooks.soulEater(caster);
            absorbing |= NativeEffectHooks.absorb(caster) > 0;
        }
        // Absorb is natively incompatible with Potency. Equipment cannot bypass this rule.
        if (absorbing) potency = 0;
        return new Parameters(Math.max(0, range), Math.max(0, potency), Math.max(0, burning),
                Mth.clamp(soulEater, 0, 10), absorbing);
    }
    /** Native Feast selection and staff rings, centered on the Ars-selected region. */
    public static boolean feast(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults, SpellStats ars) {
        var caster = frame.caster();
        var p = parameters(caster, defaults, ars);
        var center = frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity target
                ? target.position() : frame.target().getLocation();
        double radius = Math.max(0, defaults.radius() + ars.getAoeMultiplier());
        if (WandUtil.enchantedFocus(caster)) radius *= NativeEffectHooks.radius(caster) / 2.0D + 1;
        if (radius <= 0) return false;
        var area = new net.minecraft.world.phys.AABB(center, center.add(0, -4, 0)).inflate(radius);
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        boolean added = false;
        for (var enemy : world.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!enemy.isAlive() || enemy == caster || dev.arsconflux.api.targeting.Allies.areAllies(enemy, caster)) continue;
            var point = enemy.position();
            float angle = (float)Math.atan2(point.z - caster.getZ(), point.x - caster.getX());
            boolean targeted = spawn(world, frame, point, angle, 1, p);
            if (staff) targeted |= ring(world, frame, point, angle, 5, 1.5, (float)Math.PI * .4F, 0, 0, p);
            if (targeted) {
                var block = BlockPos.containing(point.add(0, -1, 0));
                var dust = new net.minecraft.core.particles.BlockParticleOption(
                        net.minecraft.core.particles.ParticleTypes.BLOCK, world.getBlockState(block));
                for (int i = 0; i < 4; ++i) com.Polarice3.Goety.utils.ServerParticleUtil.circularParticles(
                        world, dust, point.x, point.y + .25, point.z, staff ? 3 : 1);
                added = true;
            }
        }
        if (added) {
            for (int i = 0, count = world.random.nextInt(35) + 10; i < count; ++i)
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                        center.x, center.y + caster.getEyeHeight(), center.z, 1, 0, 0, 0, 0);
        }
        return added;
    }
    private static boolean line(ServerLevel world, CastContext frame, Vec3 center, float angle, int index, Parameters p) {
        double distance = 1.25 * (index + 1);
        return spawn(world, frame, center.add(Mth.cos(angle) * distance, 0, Mth.sin(angle) * distance), angle, index, p);
    }
    private static boolean ring(ServerLevel world, CastContext frame, Vec3 center, float angle, int count,
                                double radius, float step, float offset, int warmup, Parameters p) {
        boolean added = false;
        for (int index = 0; index < count; ++index) {
            float rotation = angle + index * step + offset;
            added |= spawn(world, frame, center.add(Mth.cos(rotation) * radius, 0, Mth.sin(rotation) * radius), rotation, warmup, p);
        }
        return added;
    }
    private static boolean spawn(ServerLevel world, CastContext frame, Vec3 point, float angle, int warmup, Parameters p) {
        var position = BlockPos.containing(point.x, point.y + 1, point.z);
        int bottom = Mth.floor(point.y) - 1;
        while (position.getY() >= bottom) {
            if (!world.hasChunkAt(position)) return false;
            var below = position.below();
            if (world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) {
                var shape = world.getBlockState(position).getCollisionShape(world, position);
                double surface = shape.isEmpty() ? 0 : shape.max(Direction.Axis.Y);
                var owner = frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
                // The shorter constructor is essential: the totem variant disables normal soul gains.
                var fang = new Fangs(world, point.x, position.getY() + surface, point.z, angle, warmup, owner);
                fang.setDamage(p.potency()); fang.setBurning(p.burning());
                fang.setAbsorbing(p.absorbing()); fang.setSoulEater(p.soulEater());
                return world.addFreshEntity(fang);
            }
            position = position.below();
        }
        return false;
    }
}
