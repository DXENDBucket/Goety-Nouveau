package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.CuriosFinder;
import com.Polarice3.Goety.utils.SoundUtil;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDampen;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.mixin.ProjectileHitAccess;
import dev.goetynouveau.platform.NativeBoltHooks;
import dev.goetynouveau.platform.NativeEffectHooks;
import dev.goetynouveau.api.BoltFocus;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentExtendTime;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDurationDown;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;

public final class GoetyBoltCasting {
    private GoetyBoltCasting() {}
    public static boolean replace(BoltFocus focus, EntityProjectileSpell carrier, CarrierProfile profile) {
        if (!(carrier.level() instanceof ServerLevel world)) return false;
        var resolver = ArsSpellAccess.resolver(carrier);
        var context = CastContext.of(resolver.spellContext, null, null);
        var inheritedAccount = context.account();
        var caster = context.caster();
        if (caster == null) return true;
        var spell = focus.spell();
        var augments = resolver.spell.getAugments(0, caster);
        int potency = spell.defaultStats().getPotency();
        potency += augments.stream().filter(a -> a == AugmentAmplify.INSTANCE).count();
        potency -= augments.stream().filter(a -> a == AugmentDampen.INSTANCE).count();
        if (WandUtil.enchantedFocus(caster)) potency += WandUtil.getPotencyLevel(caster);
        var direction = TriggerGeometry.direction(carrier);
        var origin = profile.arsDriven() ? carrier.position()
                // Ars already starts at eyeY - 0.1; both native bolts start at eyeY - 0.2.
                : carrier.position().add(direction.x / 2, -0.1, direction.z / 2);
        var projectile = create(focus, world, caster, origin, direction);
        projectile.setExtraDamage(Math.max(0, potency));
        if (projectile instanceof MagicBolt magic) {
            int duration = spell.defaultStats().getDuration();
            duration += augments.stream().filter(a -> a == AugmentExtendTime.INSTANCE).count();
            duration -= augments.stream().filter(a -> a == AugmentDurationDown.INSTANCE).count();
            if (WandUtil.enchantedFocus(caster)) duration += NativeEffectHooks.duration(caster);
            magic.setExtraDuration(Math.max(0, duration));
        } else {
            float velocity = spell.defaultStats().getVelocity();
            if (WandUtil.enchantedFocus(caster)) velocity += NativeBoltHooks.velocityLevel(caster);
            projectile.setBoltSpeed((int)velocity);
        }
        projectile.setOwner(context.damageOwner());
        var charge = GoetyResources.quote(resolver.spellContext, spell);
        context = context.withAccount(charge.account()).withIncomingDirection(carrier.getDeltaMovement());
        CastContexts.bind(resolver.spellContext, context);
        var continuation = resolver.clone();
        // Read Ars' acceleration statistic, not the jittered launch-vector length. Native speed
        // must be exactly unchanged with no speed modifiers, even when Ars adds shot inaccuracy.
        float speedMultiplier = Math.max(0.1F, 0.75F + resolver.getCastStats().getAccMultiplier() / 2) / 0.75F;
        var driver = new BoltExecution(focus, projectile, carrier, profile.arsDriven(), speedMultiplier);
        var session = new ExecutionSession<>(world, context, charge, Math.max(1, Math.min(200, carrier.getExpirationTime() + 1)), driver,
                hit -> {
                    var child = continuation.clone();
                    // The projectile's native bill is not a resource override for whatever
                    // provider owns the next effect in the Ars recipe.
                    CastContexts.bind(child.spellContext, CastContexts.current().withAccount(inheritedAccount));
                    child.onResolveEffect(world, hit);
                }, () -> (focus == BoltFocus.SOUL_BOLT ? dev.goetynouveau.GlyphCatalog.SOUL_BOLT
                        : dev.goetynouveau.GlyphCatalog.MAGIC_BOLT).isEnabled() && spell.conditionsMet(world, caster), ExecutionScope.DIRECT)
                .billing(action -> GoetyResources.activation(charge).trigger(action), profile.repeated());
        boolean started = ExecutionSessions.start(session);
        if (!started && caster instanceof Player player)
            player.displayClientMessage(Component.translatable("goety_nouveau.cast.unavailable"), true);
        return true;
    }
    /** Retain native directional acceleration and initially unset model rotation. Ars selects aim/origin. */
    private static SpellHurtingProjectile create(BoltFocus focus, ServerLevel world, LivingEntity caster, Vec3 p, Vec3 direction) {
        if (focus == BoltFocus.MAGIC_BOLT)
            return new MagicBolt(world, p.x, p.y, p.z, direction.x, direction.y, direction.z);
        var spell = focus.spell();
        var staff = GoetyResources.staff(caster);
        SpellHurtingProjectile bolt;
        if (spell.typeStaff(staff, SpellType.WILD)) bolt = new PoisonBolt(p.x, p.y, p.z, direction.x, direction.y, direction.z, world);
        else if (spell.typeStaff(staff, SpellType.NETHER) && CuriosFinder.hasNetherSet(caster))
            bolt = new WitherBolt(p.x, p.y, p.z, direction.x, direction.y, direction.z, world);
        else if (staff.is(ModItems.NAMELESS_STAFF.get())) bolt = new NecroBolt(p.x, p.y, p.z, direction.x, direction.y, direction.z, world);
        else {
            var soul = new SoulBolt(p.x, p.y, p.z, direction.x, direction.y, direction.z, world);
            soul.setNecro(spell.typeStaff(staff, SpellType.NECROMANCY));
            bolt = soul;
        }
        return bolt;
    }
    /** Called by the native collision loop, after the loader's original projectile impact event. */
    public static void nativeImpact(SpellHurtingProjectile bolt, HitResult hit) {
        var session = ((ManagedBolt)bolt).goetyNouveau$session();
        if (session != null) impact(session, bolt, hit, false);
    }
    private static void impact(ExecutionSession<?> session, SpellHurtingProjectile bolt, HitResult hit, boolean deferRemoval) {
        if (hit.getType() == HitResult.Type.MISS) return;
        if (hit instanceof EntityHitResult entity && !((ProjectileHitAccess)bolt).goetyNouveau$canHit(entity.getEntity())) return;
        if (!session.activate(() -> {
            var state = (ManagedBolt)bolt;
            // The native collision loop keeps its own impact position. Special carriers supply theirs.
            if (deferRemoval) bolt.setPos(hit.getLocation());
            state.goetyNouveau$impacting(deferRemoval);
            try { ((ProjectileHitAccess)bolt).goetyNouveau$hit(hit); }
            finally { state.goetyNouveau$impacting(false); }
            session.impact(hit);
            return true;
        })) session.finish(ExecutionSession.EndReason.DENIED);
        else if (!deferRemoval && bolt.isRemoved()) session.finish(ExecutionSession.EndReason.COMPLETED);
    }
    private static final class BoltExecution implements ExecutionDriver<ResourceCharge> {
        private final SpellHurtingProjectile bolt;
        private final BoltFocus focus;
        private final ArsTrajectory motion;
        private final EntityProjectileSpell carrier;
        private final float speedMultiplier;
        private BoltExecution(BoltFocus focus, SpellHurtingProjectile bolt, EntityProjectileSpell carrier, boolean arsDriven, float speedMultiplier) {
            this.focus = focus;
            this.bolt = bolt;
            this.carrier = carrier;
            this.speedMultiplier = speedMultiplier;
            motion = arsDriven ? new ArsTrajectory(bolt, carrier) : null;
        }
        @Override public Vec3 incomingDirection() { return motion != null ? motion.incomingDirection() : bolt.getDeltaMovement().normalize(); }
        public boolean start(ExecutionSession<ResourceCharge> session) {
            var state = (ManagedBolt)bolt;
            state.goetyNouveau$managed(true); state.goetyNouveau$arsDriven(motion != null);
            state.goetyNouveau$session(session); state.goetyNouveau$trajectory(motion != null ? carrier : null);
            state.goetyNouveau$speedMultiplier(speedMultiplier); state.goetyNouveau$nativeSpeed(bolt.boltSpeed);
            if (motion == null) bolt.setDeltaMovement(bolt.getDeltaMovement().scale(speedMultiplier));
            boolean started = motion != null
                    ? motion.start(session.world(), () -> {}, hit -> impact(session, bolt, hit, true))
                    : session.world().addFreshEntity(bolt);
            if (motion == null) carrier.discard();
            if (started) {
                if (bolt instanceof MagicBolt)
                    ((com.Polarice3.Goety.common.magic.Spell)focus.spell()).playSound(session.world(), session.caster(), 1.0F, 1.0F);
                else if (bolt instanceof NecroBolt) SoundUtil.playNecroBolt(session.caster());
                else if (bolt instanceof WitherBolt) {
                    var spell = (com.Polarice3.Goety.common.magic.Spell)focus.spell();
                    spell.playSound(session.world(), session.caster(), SoundEvents.WITHER_SHOOT, 0.5F, 0.25F);
                    spell.playSound(session.world(), session.caster(), ModSounds.HELL_BOLT_SHOOT.get());
                }
                else SoundUtil.playSoulBolt(session.caster());
            }
            return started;
        }
        public boolean tick(ExecutionSession<ResourceCharge> session) {
            return motion != null ? motion.tick(session.world()) : bolt.isRemoved() || !session.world().hasChunkAt(bolt.blockPosition());
        }
        public void close(ExecutionSession<ResourceCharge> session, ExecutionSession.EndReason reason) {
            var state = (ManagedBolt)bolt; state.goetyNouveau$session(null); state.goetyNouveau$trajectory(null);
            if (motion != null) motion.close();
            else { bolt.discard(); carrier.discard(); }
        }
    }
}
