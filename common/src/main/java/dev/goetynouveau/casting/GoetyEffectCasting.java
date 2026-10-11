package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.client.particles.SoulShockwaveParticleOption;
import com.Polarice3.Goety.common.entities.projectiles.EntangleVines;
import com.Polarice3.Goety.common.entities.projectiles.IceChunk;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.magic.spells.SoulHealSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.init.ModTags;
import com.Polarice3.Goety.utils.*;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Target adaptation of the upstream MIT spells. Native entities, healing particles and
 * SpellExplosion retain Goety's lifecycle, collision, damage and alliance rules.
 * See THIRD_PARTY_NOTICES.md for upstream copyright and license. */
public final class GoetyEffectCasting {
    private GoetyEffectCasting() {}
    private record Parameters(int potency, int duration, double radius) {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, SpellStats ars, dev.arsconflux.api.resource.ResourceCharge charge) {
        if (frame.target() == null || frame.target().getType() == HitResult.Type.MISS) return false;
        var caster = frame.caster();
        var spell = (Spell)focus.spell();
        var defaults = NativeSpellStats.defaults(spell);
        int potency = Math.max(0, defaults.potency() + (int)ars.getAmpMultiplier());
        int duration = Math.max(0, defaults.duration() + (int)ars.getDurationMultiplier());
        double radius = Math.max(0, defaults.radius() + ars.getAoeMultiplier());
        if (WandUtil.enchantedFocus(caster)) {
            // Frost Nova natively grants half a potency per enchantment level.
            if (focus.potency && focus != EffectFocus.FROST_NOVA) potency += WandUtil.getPotencyLevel(caster);
            if (focus.duration) duration += NativeEffectHooks.duration(caster);
            if (focus.radius && focus != EffectFocus.ENTANGLING)
                radius += NativeEffectHooks.radius(caster) * (focus == EffectFocus.HAUNTED_SKULL ? 0.5 : 1);
        }
        var parameters = new Parameters(potency, duration, radius);
        return switch (focus) {
            case BLINK -> GoetyBlinkCasting.apply(world, frame);
            case BANISH -> GoetyBanishCasting.apply(world, frame);
            case END_WALK -> GoetyEndWalkCasting.apply(world, frame, ars);
            case GRAPPLE -> GoetyGrappleCasting.apply(world, frame, ars, frame.ars());
            case TUNNEL -> GoetyTunnelCasting.apply(world, frame, ars);
            case PULVERIZE, SCATTER -> GoetyGeomancyCasting.apply(focus, world, frame, ars, charge);
            case ILLUMINATE -> GoetyIlluminateCasting.start(world, frame, ars, charge);
            case WATER_WHIP -> GoetyWaterWhipCasting.apply(world, frame, ars);
            case CYCLONE, TIDAL -> GoetyMovingAreaCasting.apply(focus, world, frame, ars);
            case TELEKINESIS -> GoetyTelekinesisCasting.apply(world, frame, ars, charge);
            case BURROWING -> GoetyBurrowingCasting.apply(world, frame, ars, charge);
            case FLYING, WHIRLWIND, SHOCKING -> GoetyWindStormCasting.apply(focus, world, frame, ars, charge);
            case VOID_SHOCK, QUAKING, WARDING -> GoetyGroundChannelCasting.apply(focus, world, frame, ars, charge);
            case WATER_JET -> GoetyWaterJetCasting.apply(world, frame, ars, charge);
            case PRISMA_BEAM -> GoetyPrismaBeamCasting.start(world, frame, ars, charge);
            case CORRUPTED_BEAM -> GoetyCorruptedBeamCasting.apply(world, frame, ars, charge);
            case THUNDERSTORM -> GoetyThunderstormCasting.apply(world, frame, ars, charge);
            case TRIDENT_STORM -> GoetyTridentCasting.apply(world, frame, spell, ars);
            case ARROW_RAIN, BLIZZARD, METEOR_SHOWER, BOMBARDMENT -> GoetyRainCasting.apply(focus, world, frame, ars, charge);
            case HOGGING -> GoetyHoggingCasting.apply(world, frame, (com.Polarice3.Goety.common.magic.SummonSpell)spell, defaults, ars);
            case MINISTROUS, BREEZE -> GoetyServantCasting.apply(focus, world, frame,
                    (com.Polarice3.Goety.common.magic.SummonSpell)spell, defaults, ars);
            case FIRE_BREATH, FROST_BREATH, SWARM -> GoetyBreathingCasting.apply(focus, world, frame, ars, charge);
            case ICE_CHUNK -> iceChunk(world, frame, spell, parameters);
            case ENTANGLING -> entangling(world, frame, spell, parameters);
            case FROST_NOVA -> frostNova(world, frame, spell, parameters);
            case SOUL_HEAL -> soulHeal(world, frame, (SoulHealSpell)spell, parameters);
            case HAUNTED_SKULL -> GoetySummonCasting.hauntedSkull(world, frame,
                    (com.Polarice3.Goety.common.magic.spells.necromancy.HauntedSkullSpell)spell,
                    potency, duration, radius, defaults.burning() + ars.getBuffCount(dev.goetynouveau.glyph.AugmentBurning.INSTANCE));
            case IRON_HIDE, CHILL_HIDE -> GoetySimpleEffects.hide(focus, world, frame, spell, potency, duration);
            case WEAKENING -> GoetySimpleEffects.weakening(world, frame, spell, potency, duration, (int)radius);
            case LIGHTNING -> GoetySimpleEffects.lightning(world, frame, spell, potency);
            case UPDRAFT -> GoetySimpleEffects.updraft(world, frame, spell, potency, radius);
            case SHOCKWAVE -> GoetySimpleEffects.shockwave(world, frame, spell, potency, radius);
            case CUSHION -> GoetySimpleEffects.cushion(world, frame, spell, duration, (int)radius);
            case FEAST -> GoetyFangCasting.feast(world, frame, spell, defaults, ars);
            case FANGS -> GoetyFangCasting.apply(world, frame, spell, defaults, ars);
            case IGNITE -> GoetyIgniteCasting.apply(world, frame,
                    (com.Polarice3.Goety.common.magic.spells.IgniteSpell)spell,
                    defaults.burning() + ars.getBuffCount(dev.goetynouveau.glyph.AugmentBurning.INSTANCE));
            case BULWARK -> GoetyCombatEffects.bulwark(world, frame, spell, defaults, ars);
            case DISCHARGE -> GoetyCombatEffects.discharge(world, frame, spell, defaults, ars);
            case FIRE_BLAST, SONIC_BOOM, FLAME_STRIKE, VOID_RIFT -> GoetyAreaEffects.apply(focus, world, frame, spell, defaults, ars);
            case THUNDERBOLT, HAIL, MONSOON -> GoetyWeatherEffects.apply(focus, world, frame, spell, defaults, ars);
            case ERUPTION, BIO_MINE, TEETH -> GoetyHazardEffects.apply(focus, world, frame, spell, defaults, ars);
            case WIND_BLAST, OVERGROWTH -> GoetyNatureEffects.apply(focus, world, frame, spell, defaults, ars);
            case SENSING, ROTATION, TREMBLING, CHARGE -> GoetyUtilityEffects.apply(focus, world, frame, spell, defaults, ars);
            case SPIKES -> GoetySpikeCasting.apply(world, frame, spell, defaults, ars);
            case KILLING, BOLTING, ILLUSION -> GoetyPersonalEffects.apply(focus, world, frame, spell, defaults, ars);
            case BARRICADE -> GoetyBarricadeCasting.apply(world, frame, spell, defaults, ars);
            case WATCHING, SNARING, BLASTING, BLAZING, GHASTLY, GUARDIAN, ZOMBIE, SKELETON, VEX, LEAPING, HUNTING, MAULING, REAPER, WRAITH, PHANTOM, SLIMY, CARRION, WHISPER, FROSTBORN, SPRITE, VANGUARD, BLACKGUARD -> GoetyServantCasting.apply(focus, world, frame, (com.Polarice3.Goety.common.magic.SummonSpell)spell, defaults, ars);
            case EARTH_FIST, GHOST_FIRE, LEECHING, LAUNCH -> GoetyLocationEffects.apply(focus, world, frame, spell, defaults, ars);
        };
    }
    private static LivingEntity target(CastContext frame) {
        return frame.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                && living.isAlive() ? living : null;
    }
    private static Vec3 center(CastContext frame) {
        var target = target(frame);
        return target != null ? target.position() : frame.target().getLocation();
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean iceChunk(ServerLevel world, CastContext frame, Spell spell, Parameters p) {
        var target = target(frame);
        var ice = new IceChunk(world, owner(frame), target);
        if (target == null) ice.setPos(center(frame).add(0, 4, 0));
        ice.setExtraDamage(p.potency());
        ice.setStaff(spell.rightStaff(GoetyResources.staff(frame.caster())));
        boolean added = world.addFreshEntity(ice);
        if (added) spell.playSound(world, frame.caster(), ModSounds.ICE_CHUNK_SUMMON.get());
        return added;
    }
    private static boolean entangling(ServerLevel world, CastContext frame, Spell spell, Parameters p) {
        var caster = frame.caster();
        var center = center(frame);
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        double area = staff ? 16 + p.radius() : p.radius();
        boolean applied = false;
        if (area > 0) {
            var targets = world.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(area),
                    entity -> entangleable(caster, entity));
            int amount = 0;
            for (var target : targets) {
                if (staff && amount >= SpellConfig.EntanglingStaffAmount.get()) break;
                if (addVines(world, frame, p, target, center)) { applied = true; ++amount; }
            }
            if (applied) return true;
        }
        var target = target(frame);
        if (target != null && !entangleable(caster, target)) return false;
        return addVines(world, frame, p, target, center);
    }
    private static boolean entangleable(LivingEntity caster, LivingEntity target) {
        return target.isAlive() && target != caster && !MobUtil.areAllies(caster, target)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)
                && !target.getType().is(ModTags.EntityTypes.UNTANGLEABLE);
    }
    private static boolean addVines(ServerLevel world, CastContext frame, Parameters p, LivingEntity target, Vec3 center) {
        var vines = target != null ? new EntangleVines(world, owner(frame), target)
                : new EntangleVines(world, owner(frame), BlockPos.containing(center));
        if (target != null) vines.setTarget(target);
        vines.setLifeSpan(vines.getLifeSpan() + MathHelper.secondsToTicks(p.duration()));
        if (target != null && CuriosFinder.hasWildRobe(frame.caster())) vines.setDamaging(true);
        return world.addFreshEntity(vines);
    }
    private static boolean soulHeal(ServerLevel world, CastContext frame, SoulHealSpell spell, Parameters p) {
        var target = target(frame);
        if (target == null || (target != frame.caster() && !MobUtil.areAllies(frame.caster(), target)
                && MobUtil.getOwner(target) != frame.caster())) return false;
        var recipients = new java.util.LinkedHashSet<LivingEntity>();
        recipients.add(target);
        if (p.radius() > 0) {
            for (var nearby : world.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(8 * p.radius()))) {
                if (MobUtil.areAllies(frame.caster(), nearby) || MobUtil.getOwner(nearby) == frame.caster())
                    recipients.add(nearby);
            }
        }
        if (recipients.stream().noneMatch(entity -> entity.isAlive() && entity.getHealth() < entity.getMaxHealth())) return false;
        int base = SpellConfig.SoulHealAmount.get();
        float heal = base + RandomUtil.nextInt(world.getRandom(), base * Math.max(0, p.potency() - 1) + 1);
        for (var recipient : recipients) {
            recipient.heal(heal);
            spell.healParticles(recipient, world);
        }
        world.sendParticles(new SoulShockwaveParticleOption(), target.getX(), target.getY() + 0.5, target.getZ(), 0, 0, 0, 0, 0);
        spell.playSound(world, target, ModSounds.SOUL_HEAL.get());
        return true;
    }
    private static boolean frostNova(ServerLevel world, CastContext frame, Spell spell, Parameters p) {
        var center = center(frame);
        var caster = frame.caster();
        var damageOwner = owner(frame);
        float potency = p.potency();
        if (WandUtil.enchantedFocus(caster)) potency += WandUtil.getPotencyLevel(caster) / 2.0F;
        float damage = SpellConfig.FrostNovaDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        float maxDamage = SpellConfig.FrostNovaMaxDamage.get().floatValue() * WandUtil.damageMultiply() + potency;
        float actual = damage + RandomUtil.nextInt(world.getRandom(), Math.max(0, (int)(maxDamage - damage)));
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        float radius = (float)p.radius() + (staff ? 0.5F : 0);
        frostParticles(world, center, (int)p.radius());
        world.sendParticles(new ShockwaveParticleOption(0, (float)p.radius() * 2, 1), center.x, center.y + 0.5, center.z, 0, 0, 0, 0, 0);
        new SpellExplosion(world, damageOwner, ModDamageSource.directFreeze(damageOwner),
                center.x, center.y, center.z, radius, actual) {
            @Override public void explodeHurt(Entity target, DamageSource source, double x, double y, double z, double seen, float amount) {
                if (target instanceof LivingEntity living && !MobUtil.areAllies(caster, living)
                        && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(living)) {
                    super.explodeHurt(target, source, x, y, z, seen, amount);
                    living.addEffect(NativeEffectHooks.freezing(MathHelper.secondsToTicks(5) * p.duration(), staff ? 1 : 0));
                }
            }
        };
        world.playSound(null, center.x, center.y, center.z, ModSounds.ICE_CHUNK_HIT.get(), spell.getSoundSource(), 1, 0.5F);
        return true;
    }
    private static void frostParticles(ServerLevel world, Vec3 center, int radius) {
        for (int i = -radius; i <= radius; ++i) for (int j = -radius; j <= radius; ++j)
            for (int k = -radius; k <= radius; ++k) {
                double x = j + (world.random.nextDouble() - world.random.nextDouble()) * 0.5;
                double y = i + (world.random.nextDouble() - world.random.nextDouble()) * 0.5;
                double z = k + (world.random.nextDouble() - world.random.nextDouble()) * 0.5;
                double length = Math.sqrt(x * x + y * y + z * z) / 0.5 + world.random.nextGaussian() * 0.05;
                if (Math.abs(length) > 1.0E-6)
                    world.sendParticles(ModParticleTypes.FROST_NOVA.get(), center.x, center.y, center.z, 0, x / length, y / length, z / length, 0.5);
                if (i != -radius && i != radius && j != -radius && j != radius) k += radius * 2 - 1;
            }
    }
}
