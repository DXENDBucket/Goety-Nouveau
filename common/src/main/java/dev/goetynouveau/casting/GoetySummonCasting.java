package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.ally.undead.HauntedSkull;
import com.Polarice3.Goety.common.magic.spells.necromancy.HauntedSkullSpell;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.SEHelper;
import com.Polarice3.Goety.utils.WandUtil;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import java.util.ArrayList;

/** Native summon target/position adaptation. The Goety entity owns AI, saving and expiry.
 * Derived from Polarice3's MIT HauntedSkullSpell; see THIRD_PARTY_NOTICES.md. */
public final class GoetySummonCasting {
    private GoetySummonCasting() {}
    public static boolean hauntedSkull(ServerLevel world, CastContext frame, HauntedSkullSpell spell,
                                       int potency, int duration, double radius) {
        var caster = frame.caster();
        var owner = NativeSummonHooks.owner(caster);
        var owned = new ArrayList<HauntedSkull>();
        for (var entity : world.getAllEntities())
            if (entity instanceof HauntedSkull skull && skull.isAlive() && owner.getUUID().equals(skull.getOwnerId()))
                owned.add(skull);
        if (spell.isShifting(caster)) {
            // Native shift-casting recalls existing servants rather than creating another batch.
            if (owned.stream().noneMatch(skull -> skull.getTrueOwner() == caster && !SEHelper.isGrounded(caster, skull))) return false;
            spell.commonResult(world, caster);
            return true;
        }
        var staff = GoetyResources.staff(caster);
        int amount = Math.min(NativeSummonHooks.count(spell, staff), Math.max(0, spell.summonLimit() - owned.size()));
        if (amount == 0) return false;
        int burning = NativeSpellStats.defaults(spell).burning();
        if (WandUtil.enchantedFocus(caster)) {
            burning += NativeSummonHooks.burning(caster);
            duration += NativeSummonHooks.additionalDuration(caster);
        }
        if (duration == 0) return false;
        var hit = frame.target();
        LivingEntity target = hit instanceof EntityHitResult selected && selected.getEntity() instanceof LivingEntity living
                && living.isAlive() && living != caster && !MobUtil.areAllies(caster, living)
                && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(living) ? living : null;
        var center = hit instanceof EntityHitResult selected ? selected.getEntity().position() : hit.getLocation();
        boolean added = false;
        for (int i = 0; i < amount; ++i) {
            var position = BlockPos.containing(center).offset(-2 + caster.getRandom().nextInt(5), 1,
                    -2 + caster.getRandom().nextInt(5));
            var skull = new HauntedSkull(ModEntityType.HAUNTED_SKULL.get(), world);
            skull.setTrueOwner(owner);
            skull.moveTo(position, 0, 0);
            NativeSummonHooks.finalizeSpawn(world, skull, position);
            skull.setBoundOrigin(position);
            skull.setLimitedLife(1200 * duration);
            spell.buffSummon(caster, skull, potency);
            skull.setExplosionPower((float)(skull.getExplosionPower() + radius));
            if (burning > 0) skull.setBurning(burning);
            if (target != null) skull.setTarget(target);
            else spell.setTarget(caster, skull);
            skull.setUpgraded(spell.NecroPower(caster));
            if (world.addFreshEntity(skull)) {
                added = true;
                spell.summonAdvancement(caster, skull);
            }
        }
        if (added) spell.playSound(world, caster, SoundEvents.EVOKER_CAST_SPELL);
        return added;
    }
}
