package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.AbstractSpellCloud;
import com.Polarice3.Goety.common.entities.projectiles.HailCloud;
import com.Polarice3.Goety.common.entities.projectiles.MonsoonCloud;
import com.Polarice3.Goety.common.items.ModItems;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.common.network.ModNetwork;
import com.Polarice3.Goety.common.network.server.SThunderBoltPacket;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.BlockFinder;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.MobUtil;
import com.Polarice3.Goety.utils.ModDamageSource;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.platform.NativeEffectHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/** MIT Thunderbolt/Hail/Monsoon activation at Ars-selected hits; see third-party notices.
 * Native cloud entities retain warmup, particles, attack cadence, allies, tracking and expiry. */
public final class GoetyWeatherEffects {
    private GoetyWeatherEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit = frame.target();
        LivingEntity selected = hit instanceof EntityHitResult entity ? MobUtil.getLivingTarget(entity.getEntity()) : null;
        if (hit instanceof EntityHitResult entity && (entity.getEntity().isRemoved()
                || !entity.getEntity().isAlive() || entity.getEntity().level() != world)) return false;
        var point = selected != null ? selected.position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !loaded(world,point)) return false;
        return focus == EffectFocus.THUNDERBOLT ? thunderbolt(world,frame,spell,defaults,ars,selected,point)
                : cloud(focus,world,frame,spell,defaults,ars,selected,point);
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean loaded(ServerLevel world, Vec3 point) {
        var pos = BlockPos.containing(point);
        return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos);
    }
    private static boolean thunderbolt(ServerLevel world, CastContext frame, Spell spell, NativeSpellStats defaults,
                                       SpellStats ars, LivingEntity target, Vec3 point) {
        var caster = frame.caster();
        float damage = SpellConfig.ThunderboltDamage.get().floatValue()*WandUtil.damageMultiply()
                + defaults.potency() + (float)ars.getAmpMultiplier();
        int range = defaults.range();
        if (WandUtil.enchantedFocus(caster)) {
            damage += WandUtil.getPotencyLevel(caster); range += WandUtil.getRangeLevel(caster);
        }
        if (!Float.isFinite(damage) || damage <= 0) return false;
        var staff = GoetyResources.staff(caster);
        boolean matching = spell.rightStaff(staff);
        var color = new ColorUtil(staff.is(ModItems.NAMELESS_STAFF.get()) ? 0xa7fc3e : 0xb1abf1);
        var rod = matching ? java.util.Optional.<BlockPos>empty()
                : BlockFinder.findLightningRod(world,BlockPos.containing(point),range);
        if (rod.isPresent()) {
            var pos = rod.get(); point = new Vec3(pos.getX(),pos.getY(),pos.getZ());
            if (!loaded(world,point)) return false;
        } else if (target != null) {
            if (target == caster || dev.arsconflux.api.targeting.Allies.areAllies(caster,target)
                    || !EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) return false;
            if (!target.hurt(ModDamageSource.directShock(owner(frame)),damage)) return false;
            point = target.position().add(0,target.getBbHeight()/2.0,0);
            float chance = matching ? .25F : .05F, chainDamage = damage/2;
            if (world.isThundering() && world.isRainingAt(target.blockPosition())) {
                chance += .25F; chainDamage = damage;
            }
            if (world.random.nextFloat() <= chance) target.addEffect(NativeEffectHooks.spasms(MathHelper.secondsToTicks(5)));
            if (matching) WandUtil.chainLightning(target,owner(frame),range/4.0,chainDamage);
        }
        // The original dedicated packet includes the thick beam geometry and its lifetime.
        var packet = new SThunderBoltPacket(caster.getEyePosition(),point,color,10);
        for (var observer : world.players()) if (observer.distanceToSqr(caster) <= 128*128
                || observer.distanceToSqr(point) <= 128*128) ModNetwork.sendTo(observer,packet);
        spell.playSound(world,caster,ModSounds.THUNDERBOLT.get());
        return true;
    }
    private static boolean cloud(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                  NativeSpellStats defaults, SpellStats ars, LivingEntity target, Vec3 point) {
        var caster = frame.caster();
        float potency = defaults.potency()+(float)ars.getAmpMultiplier();
        double radius = defaults.radius()+ars.getAoeMultiplier();
        // Cloud duration is native ticks. Each Ars time modifier adds/removes one base lifetime.
        double duration = defaults.duration()*Math.max(0,1+ars.getDurationMultiplier());
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            radius += NativeEffectHooks.radius(caster);
            duration *= NativeEffectHooks.duration(caster)+1.0;
        }
        boolean matching = spell.rightStaff(GoetyResources.staff(caster));
        if (focus == EffectFocus.MONSOON && matching) radius += 1;
        if (!Double.isFinite(duration) || duration < 1 || !Double.isFinite(radius) || radius <= 0
                || !Float.isFinite(potency)) return false;
        AbstractSpellCloud cloud;
        if (focus == EffectFocus.HAIL) {
            var hail = new HailCloud(ModEntityType.HAIL_CLOUD.get(),world);
            // Only Hail's Frost-staff branch follows a target; Monsoon stays stationary.
            if (target != null) hail.setTarget(target);
            cloud = hail;
        } else {
            var monsoon = new MonsoonCloud(ModEntityType.MONSOON_CLOUD.get(),world);
            if (GoetyResources.staff(caster).is(ModItems.NAMELESS_STAFF.get())) monsoon.setLightningColor(0xa7fc3e);
            cloud = monsoon;
        }
        cloud.setOwner(owner(frame)); cloud.setExtraDamage(Math.max(0,potency));
        cloud.setRadius((float)radius); cloud.setLifeSpan((int)Math.min(Integer.MAX_VALUE,duration));
        cloud.setStaff(matching);
        if (!placeCloud(world,cloud,point)) return false;
        if (!world.addFreshEntity(cloud)) return false;
        if (focus == EffectFocus.HAIL) spell.playSound(world,caster,SoundEvents.PLAYER_HURT_FREEZE);
        else {
            // Native positional constructor plays at birth; defer until valid placement.
            cloud.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER,.5F,1.25F);
            spell.playSound(world,caster,SoundEvents.LIGHTNING_BOLT_THUNDER,.5F,1.25F);
        }
        return true;
    }
    private static boolean placeCloud(ServerLevel world, AbstractSpellCloud cloud, Vec3 point) {
        var base = BlockPos.containing(point);
        double y = point.y+4;
        for (int offset = 0; offset <= 4; offset++) {
            var pos = base.above(offset);
            if (world.isOutsideBuildHeight(pos) || !world.hasChunkAt(pos)) return false;
            if (world.getBlockState(pos).blocksMotion()) {
                y = pos.getY()-cloud.getBbHeight()-.01; break;
            }
        }
        if (y <= point.y) return false;
        cloud.setPos(point.x,y,point.z);
        var box = cloud.getBoundingBox();
        if (!loaded(world,new Vec3(box.minX,box.minY,box.minZ))
                || !loaded(world,new Vec3(box.maxX,box.maxY,box.maxZ))
                || !world.getWorldBorder().isWithinBounds(box)) return false;
        for (int x = ((int)Math.floor(box.minX)) >> 4; x <= ((int)Math.floor(box.maxX)) >> 4; x++)
            for (int z = ((int)Math.floor(box.minZ)) >> 4; z <= ((int)Math.floor(box.maxZ)) >> 4; z++)
                if (!world.hasChunk(x,z)) return false;
        return !world.getBlockCollisions(cloud,box).iterator().hasNext();
    }
}
