package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.*;
import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.Doppelganger;
import com.Polarice3.Goety.common.events.TimedEvents;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SThunderBoltPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.*;
import dev.goetynouveau.platform.NativeBoltHooks;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import java.util.ArrayList;

/** Ars target adaptations of the original MIT Killing, Bolting and Illusion spells.
 * Native damage events, dash lifecycle and Doppelganger AI/rendering remain in Goety. */
public final class GoetyPersonalEffects {
    public static final String CLONE_MARKER = "goety_nouveau:ars_illusion";
    private GoetyPersonalEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit = frame.target();
        LivingEntity target = hit instanceof EntityHitResult e && e.getEntity() instanceof LivingEntity l ? l : null;
        if (hit instanceof EntityHitResult e && (!e.getEntity().isAlive() || e.getEntity().isRemoved()
                || e.getEntity().level() != world)) return false;
        var point = target != null ? target.position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !world.hasChunkAt(BlockPos.containing(point))) return false;
        return switch (focus) {
            case KILLING -> killing(world, frame, spell, target);
            case BOLTING -> bolting(world, frame, spell, defaults, ars, point);
            case ILLUSION -> illusion(world, frame, spell, target, point);
            default -> false;
        };
    }
    private static LivingEntity owner(CastContext frame) { return frame.damageOwner() != null ? frame.damageOwner() : frame.caster(); }
    private static boolean enemy(LivingEntity caster, LivingEntity target) {
        return target != null && target != caster && target.isAttackable()
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) && !dev.arsconflux.api.targeting.Allies.areAllies(caster, target);
    }
    private static boolean killing(ServerLevel world, CastContext frame, Spell spell, LivingEntity target) {
        var caster = frame.caster();
        if (!enemy(caster, target)) return false;
        var feedbackSource = ModDamageSource.deathCurse(caster);
        float damage = target.getHealth();
        float feedback = damage * SpellConfig.KillingFeedback.get().floatValue();
        boolean mortal = EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(caster);
        if (caster.getHealth() - MobUtil.hurtCalculation(caster, feedbackSource, feedback) <= 0 && mortal) {
            damage = caster.getHealth() - 1;
            feedback = damage;
        }
        if (damage <= 0 || !Float.isFinite(damage) || !Float.isFinite(feedback)) return false;
        boolean paidFeedback = caster.hurt(feedbackSource, feedback);
        if (!paidFeedback && mortal) return false;
        if (!target.hurt(ModDamageSource.deathCurse(owner(frame)), damage)) return paidFeedback;
        var color = new ColorUtil(14226710);
        for (int i = 0; i < 8; ++i) {
            var start = target.getEyePosition();
            world.sendParticles(new GatherTrailParticleOption(color, start.offsetRandom(target.getRandom(), 8)),
                    start.x, start.y, start.z, 0, 0, 0, 0, .5);
            ServerParticleUtil.windParticle(world, color, 1, 0, target.getId(), target.position());
        }
        var end = target.position().add(0, target.getBbHeight() / 2.0, 0);
        var packet = new SThunderBoltPacket(caster.getEyePosition(), end, color, 10);
        for (var observer : world.players()) if (observer.distanceToSqr(target) <= 128 * 128 || observer.distanceToSqr(caster) <= 128 * 128)
            ModNetwork.sendTo(observer, packet);
        spell.playSound(world, caster, ModSounds.THUNDERBOLT.get(), 3, .75F);
        spell.playSound(world, caster, SoundEvents.LIGHTNING_BOLT_IMPACT, 3, .75F);
        return true;
    }
    private static boolean bolting(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                    SpellStats ars, Vec3 point) {
        var caster = frame.caster();
        boolean self = frame.target() instanceof EntityHitResult e && e.getEntity() == caster;
        var direction = self ? caster.getLookAngle() : point.subtract(caster.position());
        if (direction.lengthSqr() < 1.0e-10) direction = caster.getLookAngle();
        direction = direction.normalize();
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        int potency = defaults.potency();
        double velocity = defaults.velocity();
        if (WandUtil.enchantedFocus(caster)) {
            // Native enchantments replace these defaults, rather than adding twice.
            potency = WandUtil.getPotencyLevel(caster);
            velocity = NativeBoltHooks.velocityLevel(caster);
        }
        potency = Math.max(0, potency + (int)ars.getAmpMultiplier());
        double speed = ((staff ? 3.5 : 2.5) + velocity / 4)
                * (Math.max(.1, .75 + ars.getAccMultiplier() / 2) / .75);
        if (!Double.isFinite(speed) || speed <= 0 || !Double.isFinite(direction.lengthSqr())) return false;
        caster.hurtMarked = true; caster.setOnGround(false);
        caster.setDeltaMovement(direction.scale(speed)); caster.hasImpulse = true; caster.fallDistance = 0;
        TimedEvents.submitTask("goety:bolting", new GoetyDashTask(caster, owner(frame), world, staff, potency));
        spell.playSound(world, caster, ModSounds.HEAVY_WOOSH.get(), 3, 1);
        spell.playSound(world, caster, ModSounds.REDSTONE_EXPLODE.get(), 3, 1);
        return true;
    }
    private static boolean illusion(ServerLevel world, CastContext frame, Spell spell, LivingEntity target, Vec3 point) {
        var caster = frame.caster();
        var old = new ArrayList<Doppelganger>();
        for (var entity : world.getAllEntities()) if (entity instanceof Doppelganger clone && clone.isAlive()
                && clone.getTrueOwner() == caster && clone.tickCount > 10) old.add(clone);
        if (GoetyResources.sneaking(caster)) {
            if (old.isEmpty()) return false;
            old.forEach(Doppelganger::dismiss);
            return true;
        }
        var staff = GoetyResources.staff(caster);
        boolean nameless = staff.is(ModItems.NAMELESS_STAFF.get());
        boolean undead = nameless && CuriosFinder.hasNamelessSet(caster);
        boolean upgraded = CuriosFinder.hasIllusionRobe(caster);
        var clones = new ArrayList<Doppelganger>();
        var anchor = BlockPos.containing(point);
        for (int x : new int[]{-5, 5}) for (int z : new int[]{-5, 5}) if (!world.hasChunkAt(anchor.offset(x, 0, z))) return false;
        for (int i = 0; i < (nameless ? 8 : 4); ++i) {
            var clone = ModEntityType.DOPPELGANGER.get().create(world);
            if (clone == null) continue;
            clone.setTrueOwner(NativeSummonHooks.owner(caster));
            clone.getPersistentData().putBoolean(CLONE_MARKER, true);
            var position = BlockFinder.SummonRadius(anchor, clone, world).getCenter();
            clone.setPos(position); clone.setUndeadClone(undead);
            clone.setLimitedLife(undead ? MathHelper.secondsToTicks(2.875F) : 1200);
            clone.setPersistenceRequired(); clone.setUpgraded(upgraded);
            if (!world.hasChunkAt(clone.blockPosition())) { clone.discard(); continue; }
            NativeSummonHooks.finalizeSpawn(world, clone, clone.blockPosition());
            if (enemy(caster, target)) {
                double dx = target.getX() - clone.getX(), dz = target.getZ() - clone.getZ();
                clone.setYRot(-(float)Math.toDegrees(Math.atan2(dx, dz)));
                clone.setTarget(target);
                if (undead) {
                    float angle = (float)Math.atan2(dz, dx) + i * (float)Math.PI * .25F + 4;
                    clone.setPos(target.position().add(net.minecraft.util.Mth.cos(angle) * 4, 0, net.minecraft.util.Mth.sin(angle) * 4));
                }
            }
            if (!world.hasChunkAt(clone.blockPosition()) || clone.getY() < world.getMinBuildHeight()
                    || clone.getBoundingBox().maxY > world.getMaxBuildHeight()) { clone.discard(); continue; }
            MobUtil.moveDownToGround(clone);
            if (world.getBlockCollisions(clone, clone.getBoundingBox()).iterator().hasNext()) { clone.discard(); continue; }
            clones.add(clone);
        }
        boolean added = false;
        for (var clone : clones) {
            if (world.addFreshEntity(clone)) { added = true; cloneParticles(world, caster, clone); }
            else clone.discard();
        }
        if (!added) return false;
        old.forEach(Doppelganger::dismiss);
        if (upgraded) {
            caster.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
            if (enemy(caster, target)) target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400));
        }
        spell.playSound(world, caster, undead ? ModSounds.LICH_TELEPORT_IN.get() : SoundEvents.ILLUSIONER_MIRROR_MOVE);
        int color = undead ? LichdomHelper.lichModeColor(caster) : -1;
        for (int i = 0, count = world.random.nextInt(35) + 10; i < count; ++i) {
            if (color > -1) coloredLich(world, caster.getEyePosition(), color);
            else world.sendParticles(undead ? ModParticleTypes.LICH.get() : ParticleTypes.POOF,
                    caster.getX(), caster.getEyeY(), caster.getZ(), 0, 0, 0, 0, 0);
        }
        return true;
    }
    private static void coloredLich(ServerLevel world, Vec3 point, int value) {
        var color = new ColorUtil(value);
        world.sendParticles(ModParticleTypes.LICH_COLORED.get(), point.x, point.y, point.z, 0,
                color.red(), color.green(), color.blue(), 1);
    }
    private static void cloneParticles(ServerLevel world, LivingEntity caster, Doppelganger clone) {
        int value = clone.isUndeadClone() ? LichdomHelper.lichModeColor(caster) : -1;
        if (clone.isUndeadClone()) {
            var color = new ColorUtil(value > -1 ? value : 3597334);
            world.sendParticles(new ReverseShockwaveParticleOption(color, 2, .5F, 1), clone.getX(), clone.getY() + .5, clone.getZ(), 0, 0, 0, 0, .5);
        }
        for (int i = 0, count = world.random.nextInt(10) + 10; i < count; ++i) {
            if (value > -1) coloredLich(world, caster.getEyePosition(), value);
            else ServerParticleUtil.smokeParticles((ParticleOptions)(clone.isUndeadClone() ? ModParticleTypes.LICH.get() : ParticleTypes.POOF),
                    clone.getX(), clone.getY(), clone.getZ(), world);
        }
    }
}
