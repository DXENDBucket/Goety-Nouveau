package dev.goetynouveau.casting;

import com.Polarice3.Goety.api.magic.SpellType;
import com.Polarice3.Goety.common.entities.projectiles.*;
import com.Polarice3.Goety.common.items.ModItems;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;

public final class SoulBoltCasting {
    private SoulBoltCasting() {}
    public static boolean replace(EntityProjectileSpell carrier, CarrierProfile profile) {
        var resolver = ArsSpellAccess.resolver(carrier);
        var context = CastContext.of(resolver.spellContext, null, null);
        if (!(carrier.level() instanceof ServerLevel world)) return true;
        var caster = context.caster();
        if (caster == null) return true;
        var spell = GoetyResources.spell();
        var augments = resolver.spell.getAugments(0, caster);
        int potency = spell.defaultStats().getPotency();
        potency += augments.stream().filter(a -> a == AugmentAmplify.INSTANCE).count();
        potency -= augments.stream().filter(a -> a == AugmentDampen.INSTANCE).count();
        if (WandUtil.enchantedFocus(caster)) potency += WandUtil.getPotencyLevel(caster);
        var projectile = create(world, caster);
        projectile.setExtraDamage(Math.max(0, potency));
        projectile.setOwner(context.damageOwner());
        var charge = GoetyResources.quote(resolver.spellContext);
        context = context.withAccount(charge.account()).withIncomingDirection(carrier.getDeltaMovement());
        CastContexts.bind(resolver.spellContext, context);
        var continuation = resolver.clone();
        var driver = new BoltExecution(projectile, carrier);
        var session = new ExecutionSession<>(world, context, charge, Math.max(1, Math.min(200, carrier.getExpirationTime() + 1)), driver,
                hit -> {
                    var child = continuation.clone();
                    CastContexts.bind(child.spellContext, CastContexts.current());
                    child.onResolveEffect(world, hit);
                }, () -> dev.goetynouveau.GlyphCatalog.SOUL_BOLT.isEnabled() && spell.conditionsMet(world, caster), ExecutionScope.DIRECT)
                .billing(action -> GoetyResources.activation(charge).trigger(action), profile.repeated());
        boolean started = ExecutionSessions.start(session);
        if (!started && caster instanceof Player player)
            player.displayClientMessage(Component.translatable("goety_nouveau.cast.unavailable"), true);
        return true;
    }
    /** Match SoulBoltSpell's native staff variants; geometry always comes from the Ars carrier. */
    private static SpellHurtingProjectile create(ServerLevel world, LivingEntity caster) {
        var spell = GoetyResources.spell();
        var staff = GoetyResources.staff(caster);
        var p = caster.getEyePosition();
        SpellHurtingProjectile bolt;
        if (spell.typeStaff(staff, SpellType.WILD)) bolt = new PoisonBolt(p.x, p.y, p.z, 0, 0, 0, world);
        else if (spell.typeStaff(staff, SpellType.NETHER) && CuriosFinder.hasNetherSet(caster))
            bolt = new WitherBolt(p.x, p.y, p.z, 0, 0, 0, world);
        else if (staff.is(ModItems.NAMELESS_STAFF.get())) bolt = new NecroBolt(p.x, p.y, p.z, 0, 0, 0, world);
        else {
            var soul = new SoulBolt(p.x, p.y, p.z, 0, 0, 0, world);
            soul.setNecro(spell.typeStaff(staff, SpellType.NECROMANCY));
            bolt = soul;
        }
        return bolt;
    }
    private static final class BoltExecution implements ExecutionDriver<ResourceCharge> {
        private final SpellHurtingProjectile bolt;
        private final ArsTrajectory motion;
        private BoltExecution(SpellHurtingProjectile bolt, EntityProjectileSpell carrier) {
            this.bolt = bolt;
            motion = new ArsTrajectory(bolt, carrier);
        }
        @Override public net.minecraft.world.phys.Vec3 incomingDirection() { return motion.incomingDirection(); }
        public boolean start(ExecutionSession<ResourceCharge> session) {
            boolean started = motion.start(session.world(), () -> ((ManagedBolt)bolt).goetyNouveau$managed(true),
                    hit -> impact(session, hit));
            if (started) {
                if (bolt instanceof NecroBolt) SoundUtil.playNecroBolt(session.caster());
                else SoundUtil.playSoulBolt(session.caster());
            }
            return started;
        }
        private void impact(ExecutionSession<ResourceCharge> session, HitResult hit) {
            if (hit.getType() == HitResult.Type.MISS) return;
            if (hit instanceof EntityHitResult entity && !((ProjectileHitAccess)bolt).goetyNouveau$canHit(entity.getEntity())) return;
            if (!session.activate(() -> {
                // Native impacts discard their projectile. A carrier such as orbit/pierce may
                // continue; defer that discard until Conflux's trajectory actually ends.
                var state = (ManagedBolt)bolt;
                bolt.setPos(hit.getLocation());
                state.goetyNouveau$impacting(true);
                try { ((ProjectileHitAccess)bolt).goetyNouveau$hit(hit); }
                finally { state.goetyNouveau$impacting(false); }
                session.impact(hit);
                return true;
            })) session.finish(ExecutionSession.EndReason.DENIED);
        }
        public boolean tick(ExecutionSession<ResourceCharge> session) { return motion.tick(session.world()); }
        public void close(ExecutionSession<ResourceCharge> session, ExecutionSession.EndReason reason) { motion.close(); }
    }
}
