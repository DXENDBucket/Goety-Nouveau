package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.GlyphCatalog;
import dev.goetynouveau.api.BoltFocus;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.mixin.*;
import dev.goetynouveau.platform.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

/** Goety Arrow/SmallFireball/WitherSkull/WaterHurtingProjectile payloads. Ordinary forms use native ticks;
 * special Ars forms drive one native batch along their trajectory. Native virtual impacts
 * preserve equipment, damage, particles, poison and persistent fire. See THIRD_PARTY_NOTICES.md. */
public final class GoetyNativeProjectileCasting {
    private GoetyNativeProjectileCasting() {}
    public static boolean replace(BoltFocus focus, EntityProjectileSpell carrier, CarrierProfile profile) {
        if (!(carrier.level() instanceof ServerLevel world)) return false;
        var resolver = ArsSpellAccess.resolver(carrier);
        var frame = CastContext.of(resolver.spellContext, null, null);
        var caster = frame.caster();
        if (caster == null) return true;
        var inheritedAccount = frame.account();
        var spell = (Spell)focus.spell();
        var augments = resolver.spell.getAugments(0, caster);
        if (!dev.goetynouveau.glyph.GoetyAugmentValidation.validBolt(focus, augments)) return true;
        var ars = resolver.getCastStats();
        var defaults = spell.defaultStats();
        int potency = defaults.getPotency() + (int)ars.getAmpMultiplier();
        int duration = defaults.getDuration() + (int)ars.getDurationMultiplier();
        int burning = defaults.getBurning() + (int)augments.stream().filter(a -> a == AugmentBurning.INSTANCE).count();
        float velocity = defaults.getVelocity();
        if (focus == BoltFocus.WITHER_SKULL) {
            // Native WitherSkullSpell reads these unconditionally, unlike Fireball.
            potency += WandUtil.getPotencyLevel(caster);
            burning += NativeSummonHooks.burning(caster);
        } else if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            if (focus == BoltFocus.FIREBALL) burning += NativeSummonHooks.burning(caster);
            else { duration += NativeEffectHooks.duration(caster); velocity += NativeBoltHooks.velocityLevel(caster) / 3.0F; }
        }
        potency = Math.max(0, potency); duration = Math.max(0, duration); burning = Math.max(0, burning);
        float multiplier = Math.max(0.1F, 0.75F + ars.getAccMultiplier() / 2) / 0.75F;
        var direction = TriggerGeometry.direction(carrier);
        var payloads = new ArrayList<Projectile>();
        if (focus.additional()) {
            payloads.addAll(GoetyProjectileFactory.create(focus, world, frame, carrier.position(), direction, ars, multiplier));
            if (payloads.isEmpty()) return true;
        } else if (focus == BoltFocus.FIREBALL) {
            boolean staff = spell.rightStaff(GoetyResources.staff(caster));
            for (var launch : NativeProjectileHooks.fireball(caster, carrier.position(), direction, staff)) {
                var p = launch.origin(); var d = launch.direction();
                AbstractHurtingProjectile fire;
                if (CuriosFinder.hasUnholySet(caster)) {
                    var hell = new HellBolt(p.x, p.y, p.z, d.x, d.y, d.z, world);
                    hell.setDamage(SpellConfig.FireballDamage.get().floatValue() * WandUtil.damageMultiply() + potency);
                    hell.setFiery(burning); fire = hell;
                } else {
                    var normal = new ModFireball(world, p.x, p.y, p.z, d.x, d.y, d.z);
                    normal.setExtraDamage(potency); normal.setFiery(burning);
                    normal.setDangerous(!GoetyResources.sneaking(caster)); fire = normal;
                }
                // WaterHurtingProjectile is scaled by its native travel adapter, not vanilla acceleration.
                if (!(fire instanceof WaterHurtingProjectile)) NativeProjectileHooks.speed(fire, multiplier);
                else fire.setDeltaMovement(fire.getDeltaMovement().scale(multiplier));
                payloads.add(fire);
            }
        } else if (focus == BoltFocus.WITHER_SKULL) {
            float blast = (float)Math.max(0, defaults.getRadius() + ars.getAoeMultiplier())
                    + NativeEffectHooks.radius(caster) / 2.0F;
            boolean staff = spell.rightStaff(GoetyResources.staff(caster));
            var origin = carrier.position().add(direction.x / 2, -0.1, direction.z / 2);
            var main = witherSkull(world, caster, spell, origin, direction, potency, burning);
            main.setExplosionPower(main.getExplosionPower() + blast);
            NativeProjectileHooks.speed(main, multiplier); payloads.add(main);
            if (staff) for (int i = 0; i < 2; ++i) {
                var side = witherSkull(world, caster, spell,
                        origin.add(world.random.nextGaussian(), 0, world.random.nextGaussian()), direction, potency, burning);
                // Preserve the upstream staff branch's extra radius application to side shots.
                side.setExplosionPower(main.getExplosionPower() + blast);
                NativeProjectileHooks.speed(side, multiplier); payloads.add(side);
            }
        } else {
            var quill = new PoisonQuill(world, caster);
            quill.setPos(carrier.position());
            quill.setSpear(spell.rightStaff(GoetyResources.staff(caster)), potency + 1);
            // Carrier direction already includes Ars' launch inaccuracy: do not add a second jitter.
            quill.shoot(direction.x, direction.y, direction.z, Math.max(0.1F, velocity) * multiplier, 0);
            quill.setExtraDamage(potency); quill.setDuration(duration);
            quill.setAqua(caster.isUnderWater()); payloads.add(quill);
        }
        payloads.forEach(p -> {
            var owner = frame.damageOwner() != null ? frame.damageOwner() : caster;
            p.setOwner(owner);
            // This native family overloads setOwner(LivingEntity) and resolves its own synced UUID.
            // Updating vanilla Projectile.owner alone would leave native damage/ally checks on the old caster.
            if (p instanceof SpellTargetProjectile targeted && (Entity)owner instanceof net.minecraft.world.entity.LivingEntity living) targeted.setOwner(living);
        });
        var quote = GoetyResources.quote(resolver.spellContext, spell, augments);
        var selected = frame.withAccount(quote.account()).withIncomingDirection(direction);
        CastContexts.bind(resolver.spellContext, selected);
        var continuation = resolver.clone();
        var parameters = new Batch(focus, payloads, quote, direction);
        var driver = new NativeFlight(parameters, carrier, profile.arsDriven(), multiplier);
        int lifetime = focus == BoltFocus.POISON_DART || focus == BoltFocus.BOUNCY_BUBBLE ? 100
                : focus == BoltFocus.RAZOR_WIND ? 41 : 200;
        var session = new ExecutionSession<>(world, selected, parameters,
                Math.max(1, Math.min(lifetime, carrier.getExpirationTime() + 1)), driver,
                hit -> {
                    var child = continuation.clone();
                    CastContexts.bind(child.spellContext, CastContexts.current().withAccount(inheritedAccount));
                    child.onResolveEffect(world, hit);
                }, () -> GlyphCatalog.projectile(focus).isEnabled()
                        && (focus == BoltFocus.SWORD || spell.conditionsMet(world, caster)), ExecutionScope.DIRECT)
                .billing(action -> GoetyResources.activation(quote).trigger(action), profile.repeated());
        if (!ExecutionSessions.start(session) && caster instanceof Player player)
            player.displayClientMessage(Component.translatable("goety_nouveau.cast.unavailable"), true);
        return true;
    }
    private static ModWitherSkull witherSkull(ServerLevel world, net.minecraft.world.entity.LivingEntity caster,
                                              Spell spell, Vec3 origin, Vec3 direction, int potency, int burning) {
        var skull = new ModWitherSkull(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, world);
        skull.setDangerous(GoetyResources.sneaking(caster));
        skull.setExtraDamage(potency); skull.setFiery(burning);
        return skull;
    }
    public static boolean manages(Projectile projectile) {
        return projectile instanceof ManagedBolt state && state.goetyNouveau$session() != null
                && state.goetyNouveau$session().parameters() instanceof Batch;
    }
    /** Called after the vanilla/native projectile impact cancellation hook. */
    public static void nativeImpact(Projectile projectile, HitResult hit) {
        if (projectile.level().isClientSide || projectile.isRemoved()) return;
        var state = (ManagedBolt)projectile;
        var session = state.goetyNouveau$session();
        if (session == null || !(session.parameters() instanceof Batch batch)) return;
        if (!valid(projectile, hit) || batch.settled.contains(projectile)) return;
        batch.direction = projectile.getDeltaMovement().normalize();
        if (!session.activate(() -> fullImpact(session, projectile, hit, false)))
            session.finish(ExecutionSession.EndReason.DENIED);
        else if (batch.projectiles.stream().allMatch(Entity::isRemoved))
            session.finish(ExecutionSession.EndReason.COMPLETED);
    }
    private static boolean valid(Projectile projectile, HitResult hit) {
        return hit.getType() != HitResult.Type.MISS && (!(hit instanceof EntityHitResult entity)
                || ((ProjectileHitAccess)projectile).goetyNouveau$canHit(entity.getEntity()));
    }
    private static boolean fullImpact(ExecutionSession<?> session, Projectile projectile, HitResult hit, boolean repeated) {
        var state = (ManagedBolt)projectile;
        var velocity = projectile.getDeltaMovement();
        var batch = (Batch)session.parameters();
        if (repeated && batch.focus == BoltFocus.SWORD && GoetyProjectileFactory.sword(session.caster()).isEmpty()) return false;
        state.goetyNouveau$handlingHit(true); state.goetyNouveau$impacting(repeated);
        try {
            if (repeated) {
                projectile.setPos(hit.getLocation());
                resetPiercing(projectile);
                if (projectile instanceof VoidShockBomb) ((dev.goetynouveau.mixin.NativeVoidBombAccess)projectile).goetyNouveau$growTick(0);
            }
            if (projectile instanceof SlashProjectile slash && hit instanceof EntityHitResult entity) slash.damageEntity(entity.getEntity());
            else ((ProjectileHitAccess)projectile).goetyNouveau$hit(hit);
            if (!repeated && projectile instanceof VoidShockBomb) batch.settled.add(projectile);
            if (repeated && projectile instanceof VoidShockBomb) ((dev.goetynouveau.mixin.NativeVoidBombAccess)projectile).goetyNouveau$growTick(0);
            if (repeated && projectile instanceof BouncyBubble bubble && ((dev.goetynouveau.mixin.NativeBubbleAccess)bubble).goetyNouveau$exploding()) {
                ((dev.goetynouveau.mixin.NativeBubbleAccess)bubble).goetyNouveau$exploding(false); bubble.setBounceTimes(0);
            }
            // Arrow block hits normally latch into the ground. Ars special carriers own that decision.
            if (repeated && projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow) {
                ((ArrowGroundAccess)projectile).goetyNouveau$inGround(false);
                projectile.setDeltaMovement(velocity);
            }
        } finally { state.goetyNouveau$impacting(false); state.goetyNouveau$handlingHit(false); }
        session.impact(hit);
        if (repeated && batch.focus == BoltFocus.SWORD) GoetyProjectileFactory.wearSword(session.caster());
        return true;
    }
    private static void resetPiercing(Projectile projectile) {
        if (projectile instanceof PoisonQuill) ((QuillPiercingAccess)projectile).goetyNouveau$resetPiercing();
        if (projectile instanceof IceSpear) ((dev.goetynouveau.mixin.IceSpearPiercingAccess)projectile).goetyNouveau$resetPiercing();
    }
    private static final class Batch {
        final BoltFocus focus;
        final List<Projectile> projectiles;
        final ResourceCharge charge;
        Vec3 direction;
        final java.util.Set<Projectile> settled = java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        Batch(BoltFocus focus, List<Projectile> projectiles, ResourceCharge charge, Vec3 direction) {
            this.focus = focus; this.projectiles = List.copyOf(projectiles); this.charge = charge; this.direction = direction;
        }
    }
    private static final class NativeFlight implements ExecutionDriver<Batch> {
        private final Batch batch;
        private final EntityProjectileSpell carrier;
        private final ArsTrajectory motion;
        private final List<Vec3> offsets;
        private final float speed;
        NativeFlight(Batch batch, EntityProjectileSpell carrier, boolean driven, float speed) {
            this.batch = batch; this.carrier = carrier; this.speed = speed;
            var origin = batch.projectiles.get(0).position();
            offsets = batch.projectiles.stream().map(p -> p.position().subtract(origin)).toList();
            motion = driven ? new ArsTrajectory(batch.projectiles.get(0), carrier) : null;
        }
        public Vec3 incomingDirection() { return batch.direction; }
        public boolean start(ExecutionSession<Batch> session) {
            for (var p : batch.projectiles) {
                var state = (ManagedBolt)p;
                state.goetyNouveau$managed(true); state.goetyNouveau$arsDriven(motion != null);
                state.goetyNouveau$speedMultiplier(speed); state.goetyNouveau$session(session);
                if (p instanceof SpellHurtingProjectile spell) state.goetyNouveau$nativeSpeed(spell.boltSpeed);
                state.goetyNouveau$trajectory(motion != null ? carrier : null);
            }
            boolean added = false;
            if (motion != null) {
                added = motion.start(session.world(), () -> {}, hit -> {
                    // The staff's native salvo is one paid activation of this Ars trajectory.
                    resetPiercing(batch.projectiles.get(0));
                    if (!valid(batch.projectiles.get(0), hit)) return;
                    batch.direction = motion.incomingDirection();
                    if (!session.activate(() -> {
                        boolean success = false;
                        for (var p : batch.projectiles) if (!p.isRemoved() && valid(p, hit)) success |= fullImpact(session, p, hit, true);
                        return success;
                    })) session.finish(ExecutionSession.EndReason.DENIED);
                });
                if (added) for (int i = 1; i < batch.projectiles.size(); ++i) {
                    batch.projectiles.get(i).setPos(carrier.position().add(offsets.get(i)));
                    session.world().addFreshEntity(batch.projectiles.get(i));
                }
            } else {
                for (var p : batch.projectiles) added |= session.world().addFreshEntity(p);
                carrier.discard();
            }
            if (added) {
                if (batch.focus == BoltFocus.SWORD && motion == null) GoetyProjectileFactory.wearSword(session.caster());
                var spell = (Spell)batch.focus.spell();
                if (batch.focus.additional()) GoetyProjectileFactory.sound(batch.focus, session.world(), session.caster());
                else if (batch.focus == BoltFocus.POISON_DART)
                    spell.playSound(session.world(), session.caster(), ModSounds.POISON_QUILL_VINE_SHOOT.get(), 1, spell.projPitch(session.world().random));
                else if (batch.focus == BoltFocus.WITHER_SKULL)
                    spell.playSound(session.world(), session.caster(), SoundEvents.WITHER_SHOOT, 2,
                            (session.caster().getRandom().nextFloat() - session.caster().getRandom().nextFloat()) * .2F + 1);
                else spell.playSound(session.world(), session.caster(), batch.projectiles.get(0) instanceof HellBolt
                        ? ModSounds.HELL_BOLT_SHOOT.get() : SoundEvents.BLAZE_SHOOT, 2, spell.projPitch(session.world().random));
            }
            return added;
        }
        public boolean tick(ExecutionSession<Batch> session) {
            if (motion == null) return batch.projectiles.stream().allMatch(p -> p.isRemoved() || !session.world().hasChunkAt(p.blockPosition()));
            boolean ended = motion.tick(session.world());
            var main = batch.projectiles.get(0);
            for (int i = 1; i < batch.projectiles.size(); ++i) {
                var p = batch.projectiles.get(i);
                p.setPos(main.position().add(offsets.get(i))); p.setDeltaMovement(main.getDeltaMovement());
                p.setYRot(main.getYRot()); p.setXRot(main.getXRot()); p.hasImpulse = true;
            }
            return ended;
        }
        public void close(ExecutionSession<Batch> session, ExecutionSession.EndReason reason) {
            for (var p : batch.projectiles) {
                var state = (ManagedBolt)p;
                state.goetyNouveau$session(null); state.goetyNouveau$trajectory(null); p.discard();
            }
            if (motion != null) motion.close(); else carrier.discard();
        }
    }
}
