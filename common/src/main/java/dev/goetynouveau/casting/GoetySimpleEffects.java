package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.SpellLightningBolt;
import com.Polarice3.Goety.common.entities.util.UpdraftBlast;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.WandUtil;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** Location adapters for the original MIT Goety spells. The original caster supplies gear,
 * ownership and alliance rules; a selected recipient never becomes a surrogate caster.
 * Native lightning/updraft entities retain their renderer, tick, damage and lifecycle.
 * See THIRD_PARTY_NOTICES.md. */
public final class GoetySimpleEffects {
    private GoetySimpleEffects() {}
    private static LivingEntity target(CastContext frame) {
        return frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living.isAlive() ? living : null;
    }
    private static Vec3 center(CastContext frame) {
        var entity = target(frame);
        return entity != null ? entity.position() : frame.target().getLocation();
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean ally(CastContext frame, LivingEntity entity) {
        return entity == frame.caster() || MobUtil.areAllies(frame.caster(), entity)
                || MobUtil.getOwner(entity) == frame.caster();
    }
    public static boolean hide(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell, int potency, int duration) {
        var recipient = target(frame);
        if (recipient == null || !ally(frame, recipient) || duration <= 0) return false;
        boolean iron = focus == EffectFocus.IRON_HIDE;
        int ticks = iron ? MathHelper.minutesToTicks(duration) : MathHelper.secondsToTicks(45 * duration);
        boolean applied = recipient.addEffect(iron ? NativeEffectHooks.ironHide(ticks, potency)
                : NativeEffectHooks.chillHide(ticks, potency));
        if (!iron && spell.rightStaff(GoetyResources.staff(frame.caster()))) {
            for (var nearby : world.getEntitiesOfClass(LivingEntity.class, recipient.getBoundingBox().inflate(4))) {
                if (nearby != recipient && nearby.isAlive() && ally(frame, nearby))
                    applied |= nearby.addEffect(NativeEffectHooks.chillHide(ticks, potency));
            }
        }
        if (applied) {
            if (iron) spell.playSound(world, recipient, ModSounds.IRON_HIDE.get());
            else spell.playSound(world, recipient, ModSounds.ICE_SPIKE_HIT.get(), 1.0F, 0.5F);
        }
        return applied;
    }
    public static boolean weakening(ServerLevel world, CastContext frame, Spell spell, int potency, int duration, int radius) {
        if (duration <= 0 || radius <= 0) return false;
        var point = center(frame);
        var color = new ColorUtil(ChatFormatting.LIGHT_PURPLE);
        world.sendParticles(new ShockwaveParticleOption(color.red, color.green, color.blue, radius, 1, true),
                point.x, point.y + 0.25, point.z, 0, 0, 0, 0, 0);
        for (int x = -radius; x < radius; ++x) for (int z = -radius; z < radius; ++z) {
            var pos = BlockPos.containing(point).offset(x, 0, z);
            if (world.random.nextFloat() <= 0.25F)
                world.sendParticles(ModParticleTypes.SPELL_SQUARE.get(), pos.getX(), pos.getY(), pos.getZ(),
                        0, color.red(), color.green(), color.blue(), 0.5F);
        }
        var recipient = target(frame);
        AABB area = recipient != null ? recipient.getBoundingBox().inflate(radius) : new AABB(point, point).inflate(radius);
        for (var entity : world.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity.isAlive() && !ally(frame, entity)) {
                entity.addEffect(NativeEffectHooks.sapped(MathHelper.secondsToTicks(duration), potency + 4));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, MathHelper.secondsToTicks(duration), potency));
                entity.playSound(ModSounds.WEAKEN_CURSE.get());
            }
        }
        // Native cast sound is centered on the selected location, even if the area is empty.
        if (spell.CastingSound() != null)
            world.playSound(null, point.x, point.y, point.z, spell.CastingSound(), spell.getSoundSource(), 2.0F, 1.0F);
        return true;
    }
    public static boolean lightning(ServerLevel world, CastContext frame, Spell spell, int potency) {
        var point = center(frame);
        var rod = BlockFinder.findLightningRod(world, BlockPos.containing(point));
        if (rod.isPresent() && !spell.rightStaff(GoetyResources.staff(frame.caster()))) point = Vec3.atBottomCenterOf(rod.get());
        else if (target(frame) == null && frame.target() instanceof BlockHitResult block)
            point = Vec3.atBottomCenterOf(block.getBlockPos());
        var lightning = new SpellLightningBolt(ModEntityType.SPELL_LIGHTNING_BOLT.get(), world);
        lightning.setDamage(SpellConfig.LightningDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
        lightning.setOwner(owner(frame));
        lightning.setPos(point);
        return world.addFreshEntity(lightning);
    }
    public static boolean updraft(ServerLevel world, CastContext frame, Spell spell, int potency, double radius) {
        var point = center(frame);
        if (target(frame) == null && frame.target() instanceof BlockHitResult block) {
            var pos = block.getBlockPos();
            point = new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        }
        var blast = new UpdraftBlast(ModEntityType.UPDRAFT_BLAST.get(), world);
        blast.setOwner(owner(frame));
        blast.setDamage(SpellConfig.UpdraftBlastDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
        blast.setAreaOfEffect((float)(radius + (spell.rightStaff(GoetyResources.staff(frame.caster())) ? 0.5 : 0)));
        blast.setPos(point);
        return world.addFreshEntity(blast);
    }
    public static boolean shockwave(ServerLevel world, CastContext frame, Spell spell, int potency, double radius) {
        if (radius <= 0) return false;
        var point = center(frame);
        float minimum = SpellConfig.ShockwaveDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        float maximum = SpellConfig.ShockwaveMaxDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        float damage = Math.max(minimum, Math.min(maximum, minimum + com.Polarice3.Goety.utils.RandomUtil.nextInt(
                world.getRandom(), Math.max(0, (int)(maximum - minimum)))));
        for (int i = 0; i < 8; ++i) {
            var particle = point.add(0, 0.5, 0).offsetRandom(world.random, (float)radius);
            world.sendParticles(ModParticleTypes.SOUL_EXPLODE.get(), particle.x, particle.y, particle.z, 1, 0, 0, 0, 0);
        }
        var color = new ColorUtil(0x2ac9cf);
        world.sendParticles(new com.Polarice3.Goety.client.particles.CircleExplodeParticleOption(
                color.red(), color.green(), color.blue(), (float)radius, 1), point.x, point.y + 0.5, point.z, 0, 0, 0, 0, 0);
        world.sendParticles(new com.Polarice3.Goety.client.particles.SphereExplodeParticleOption(
                color.red(), color.green(), color.blue(), (float)radius, 1), point.x, point.y + 0.5, point.z, 1, 0, 0, 0, 0);
        var damageOwner = owner(frame);
        MobUtil.explosionDamage(world, damageOwner, world.damageSources().indirectMagic(damageOwner, damageOwner),
                point.x, point.y, point.z, (float)radius, damage);
        world.playSound(null, point.x, point.y, point.z, ModSounds.SOUL_EXPLODE.get(), spell.getSoundSource(), 2, 1);
        return true;
    }
    public static boolean cushion(ServerLevel world, CastContext frame, Spell spell, int duration, int radius) {
        if (duration < 0 || radius <= 0) return false;
        var point = center(frame);
        if (target(frame) == null && frame.target() instanceof BlockHitResult block)
            point = Vec3.atBottomCenterOf(block.getBlockPos()).add(0, 1, 0);
        var cushion = new com.Polarice3.Goety.common.entities.util.Cushion(ModEntityType.CUSHION.get(), world);
        cushion.setRadius(radius);
        cushion.increaseLifeSpan(duration);
        cushion.setPos(point);
        boolean added = world.addFreshEntity(cushion);
        if (added) world.playSound(null, point.x, point.y, point.z, net.minecraft.sounds.SoundEvents.EVOKER_CAST_SPELL,
                spell.getSoundSource(), 1, 1);
        return added;
    }
}
