package dev.goetynouveau.casting;

import com.Polarice3.Goety.client.particles.ModParticleTypes;
import com.Polarice3.Goety.common.magic.spells.storm.BoltingSpell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.utils.*;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;

/** Native start/stop/collision timing, with an explicit damage owner for Ars executions.
 * The original MIT tick attack is adapted only to separate attribution from the moving caster. */
public final class GoetyDashTask extends BoltingSpell.BoltingDashTask {
    private final java.util.UUID damageOwner;
    private final dev.arsconflux.api.damage.SpellPower.Profile power;
    public GoetyDashTask(LivingEntity caster, LivingEntity damageOwner, ServerLevel world, boolean staff, float potency) {
        super(caster.getUUID(), world, staff, potency);
        this.damageOwner = damageOwner.getUUID();
        this.power=dev.arsconflux.api.damage.SpellPower.capture(caster);
    }
    @Override public void tickTask() {
        dev.arsconflux.api.school.SchoolDamage.during(power.schools(),()->dev.arsconflux.api.damage.SpellPower.scoped(power,()->{tickPowerTask();return null;}));
    }
    private void tickPowerTask() {
        ++ticks;
        if (!(level.getEntity(owner) instanceof LivingEntity caster)) return;
        var source = level.getEntity(damageOwner) instanceof LivingEntity living ? living : caster;
        caster.fallDistance = 0; caster.invulnerableTime = 20;
        for (int i = 0; i < 2; ++i) level.sendParticles(ModParticleTypes.BIG_ELECTRIC.get(),
                caster.getRandomX(1), caster.getRandomY(), caster.getRandomZ(1), 0,
                (2 * Math.random() - 1) * .2, (2 * Math.random() - 1) * .2, (2 * Math.random() - 1) * .2, .5);
        ServerParticleUtil.addParticlesAroundMiddleSelf(level, ModParticleTypes.ELECTRIC.get(), caster);
        for (var entity : level.getEntities(caster, caster.getBoundingBox().inflate(staff ? 1.25 : 1))) {
            if (!(entity instanceof LivingEntity target) || dev.arsconflux.api.targeting.Allies.areAllies(caster, target)
                    || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target) || !target.isAttackable()) continue;
            float base = SpellConfig.BoltingDamage.get().floatValue() * WandUtil.damageMultiply();
            if (!target.hurt(ModDamageSource.directShock(source), base + damage)) continue;
            float chance = staff ? .25F : .05F;
            float chainDamage = damage / 2;
            if (level.isThundering() && level.isRainingAt(target.blockPosition())) { chance += .25F; chainDamage = damage; }
            if (level.random.nextFloat() <= chance) target.addEffect(NativeEffectHooks.spasms(MathHelper.secondsToTicks(5)));
            if (staff) WandUtil.chainLightning(target, source, 2, chainDamage);
        }
    }
}
