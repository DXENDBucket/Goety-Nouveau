package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.client.particles.SphereExplodeParticleOption;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SLightningPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Target adaptations of Goety's MIT BulwarkSpell and DischargeSpell. Native capability,
 * shield events, rendering, electrical packets, explosions and chaining remain authoritative.
 * See THIRD_PARTY_NOTICES.md for upstream attribution. */
public final class GoetyCombatEffects {
    private GoetyCombatEffects() {}
    public static boolean bulwark(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults, SpellStats ars) {
        if (!(frame.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                || !target.isAlive() || target.isRemoved() || target.level() != world
                || !world.hasChunkAt(target.blockPosition())) return false;
        var caster = frame.caster();
        if (target != caster && !dev.arsconflux.api.targeting.Allies.areAllies(caster, target) && MobUtil.getOwner(target) != caster) return false;
        // Ars chooses the recipient: the native "no existing shields" condition belongs to it.
        if (MiscCapHelper.getShields(target) > 0) return false;
        long amount = SpellConfig.BulwarkShieldAmount.get() + (long)defaults.potency() + (int)ars.getAmpMultiplier();
        long duration = SpellConfig.BulwarkShieldTime.get();
        if (WandUtil.enchantedFocus(caster)) {
            amount += WandUtil.getPotencyLevel(caster);
            duration *= Math.min(4, NativeEffectHooks.duration(caster) + 1);
        }
        // Native SpellStat.duration is a tick override, not the usual seconds/unit parameter.
        if (defaults.duration() > 0) duration = defaults.duration();
        double ticks = duration * Math.max(0, 1 + ars.getDurationMultiplier());
        if (amount <= 0 || !Double.isFinite(ticks) || ticks < 1) return false;
        MiscCapHelper.setShields(target, (int)Math.min(Integer.MAX_VALUE, amount));
        MiscCapHelper.setShieldTime(target, (int)Math.min(Integer.MAX_VALUE, ticks));
        spell.playSound(world, target, ModSounds.SHIELD_UP.get(), 3, target.getVoicePitch());
        return true;
    }
    public static boolean discharge(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults, SpellStats ars) {
        var caster = frame.caster();
        var owner = frame.damageOwner() != null ? frame.damageOwner() : caster;
        var selected = frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living ? living : null;
        if (selected != null && (!selected.isAlive() || selected.isRemoved() || selected.level() != world)) return false;
        Vec3 point = selected != null ? selected.position() : frame.target().getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !world.hasChunkAt(net.minecraft.core.BlockPos.containing(point))) return false;
        int radius = (int)Math.max(0, defaults.radius() + ars.getAoeMultiplier());
        float potency = defaults.potency() + (float)ars.getAmpMultiplier();
        if (WandUtil.enchantedFocus(caster)) {
            radius += NativeEffectHooks.radius(caster);
            potency += WandUtil.getPotencyLevel(caster) / 2.0F;
        }
        if (radius <= 0) return false;
        float minimum = Math.max(0, SpellConfig.DischargeDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
        float maximum = Math.max(minimum, SpellConfig.DischargeMaxDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
        float damage = Math.min(maximum, minimum + RandomUtil.nextInt(world.random, Math.max(0, (int)(maximum - minimum))));
        // Native SpellExplosion uses a zero damage argument as an auto-damage sentinel.
        if (damage <= 0) return false;
        var staff = GoetyResources.staff(caster);
        boolean chaining = spell.rightStaff(staff);
        var color = new ColorUtil(staff.is(ModItems.NAMELESS_STAFF.get()) ? 0xa7fc3e : 0xfef597);
        world.sendParticles(new ShockwaveParticleOption(color.red(), color.green(), color.blue()),
                point.x, point.y + .5, point.z, 0, 0, 0, 0, 0);
        world.sendParticles(new SphereExplodeParticleOption(color.red(), color.green(), color.blue(), radius, 1),
                point.x, point.y + .5, point.z, 1, 0, 0, 0, 0);
        for (int i = 0; i < 16; i++) {
            var end = point.add(world.random.nextDouble() * world.random.nextIntBetweenInclusive(-4, 4),
                    world.random.nextDouble() * world.random.nextIntBetweenInclusive(-4, 4),
                    world.random.nextDouble() * world.random.nextIntBetweenInclusive(-4, 4));
            var packet = new SLightningPacket(point, end, color, 12);
            // Reuse native packet/rendering, scoped to observers in this dimension.
            for (var player : world.players()) if (player.distanceToSqr(point) <= 128 * 128) ModNetwork.sendTo(player, packet);
        }
        new SpellExplosion(world, owner, ModDamageSource.directShock(owner), point.x, point.y, point.z, radius, damage) {
            @Override public void explodeHurt(Entity target, DamageSource source, double x, double y, double z, double seen, float actual) {
                if (!(target instanceof LivingEntity living) || living == caster || dev.arsconflux.api.targeting.Allies.areAllies(caster, living)) return;
                super.explodeHurt(target, source, x, y, z, seen, actual);
                float chance = chaining ? .25F : .05F;
                float chainDamage = actual / 2;
                if (world.isThundering() && world.isRainingAt(living.blockPosition())) { chance += .25F; chainDamage = actual; }
                if (world.random.nextFloat() <= chance) living.addEffect(NativeEffectHooks.spasms(MathHelper.secondsToTicks(5)));
                if (chaining) WandUtil.chainLightning(living, owner, 6, chainDamage);
            }
        };
        world.playSound(null, point.x, point.y, point.z, ModSounds.REDSTONE_EXPLODE.get(), spell.getSoundSource(), 2, 1);
        return true;
    }
}
