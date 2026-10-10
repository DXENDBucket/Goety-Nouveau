package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.ModEntityType;
import com.Polarice3.Goety.common.entities.projectiles.BioMine;
import com.Polarice3.Goety.common.entities.projectiles.ViciousTooth;
import com.Polarice3.Goety.common.magic.Spell;
import com.Polarice3.Goety.config.SpellConfig;
import com.Polarice3.Goety.init.ModSounds;
import com.Polarice3.Goety.utils.ColorUtil;
import com.Polarice3.Goety.utils.MathHelper;
import com.Polarice3.Goety.utils.ServerParticleUtil;
import com.Polarice3.Goety.utils.WandUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import dev.arsconflux.api.context.CastContext;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.api.NativeSpellStats;
import dev.goetynouveau.glyph.AugmentBurning;
import dev.goetynouveau.platform.NativeEffectHooks;
import dev.goetynouveau.platform.NativeLocationHooks;
import dev.goetynouveau.platform.NativeSummonHooks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** MIT native Eruption/BioMine/Teeth activation adapted to Ars points, not a second view ray.
 * Native entities own their animation, aftermath, collisions and lifetime. See notices. */
public final class GoetyHazardEffects {
    private GoetyHazardEffects() {}
    public static boolean apply(EffectFocus focus, ServerLevel world, CastContext frame, Spell spell,
                                NativeSpellStats defaults, SpellStats ars) {
        var hit = frame.target();
        if (hit instanceof EntityHitResult entity && (entity.getEntity().isRemoved()
                || entity.getEntity().level() != world || !entity.getEntity().isAlive())) return false;
        var point = hit instanceof EntityHitResult entity ? entity.getEntity().position() : hit.getLocation();
        if (!Double.isFinite(point.lengthSqr()) || !loaded(world, point)) return false;
        var caster = frame.caster();
        int potency = defaults.potency() + (int)ars.getAmpMultiplier();
        int duration = defaults.duration() + (int)ars.getDurationMultiplier();
        double radius = defaults.radius() + ars.getAoeMultiplier();
        int burning = defaults.burning() + ars.getBuffCount(AugmentBurning.INSTANCE);
        if (WandUtil.enchantedFocus(caster)) {
            potency += WandUtil.getPotencyLevel(caster);
            duration += NativeEffectHooks.duration(caster);
            radius += NativeEffectHooks.radius(caster);
            burning += NativeSummonHooks.burning(caster);
        }
        boolean staff = spell.rightStaff(GoetyResources.staff(caster));
        return switch (focus) {
            case ERUPTION -> eruption(world, frame, point, staff, Math.max(0,potency),
                    Math.max(0,duration), Math.max(0,(int)radius), Math.max(0,burning));
            case BIO_MINE -> mines(world, frame, point, staff, Math.max(0,potency),
                    Math.max(0,duration), Math.max(0,radius));
            case TEETH -> teeth(world, frame, spell, point, staff, Math.max(0,potency));
            default -> false;
        };
    }
    private static LivingEntity owner(CastContext frame) {
        return frame.damageOwner() != null ? frame.damageOwner() : frame.caster();
    }
    private static boolean loaded(ServerLevel world, Vec3 point) {
        var pos = BlockPos.containing(point);
        return !world.isOutsideBuildHeight(pos) && world.hasChunkAt(pos);
    }
    private static boolean loadedBetween(ServerLevel world, Vec3 first, Vec3 last) {
        int minX = ((int)Math.floor(Math.min(first.x,last.x))) >> 4;
        int maxX = ((int)Math.floor(Math.max(first.x,last.x))) >> 4;
        int minZ = ((int)Math.floor(Math.min(first.z,last.z))) >> 4;
        int maxZ = ((int)Math.floor(Math.max(first.z,last.z))) >> 4;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++)
            if (!world.hasChunk(x,z)) return false;
        return true;
    }
    /** Block-only collision preserves native overlapping staff formations. Never load chunks. */
    private static boolean free(ServerLevel world, Entity entity) {
        var box = entity.getBoundingBox();
        return loaded(world, new Vec3(box.minX,box.minY,box.minZ))
                && loaded(world, new Vec3(box.maxX,box.maxY,box.maxZ))
                && loadedBetween(world,new Vec3(box.minX,box.minY,box.minZ),new Vec3(box.maxX,box.maxY,box.maxZ))
                && world.getWorldBorder().isWithinBounds(box)
                && !world.getBlockCollisions(entity,box).iterator().hasNext();
    }
    private static boolean eruption(ServerLevel world, CastContext frame, Vec3 point, boolean staff,
                                     int potency, int duration, int radius, int burning) {
        if (duration == 0) return false;
        boolean added = false;
        var pos = BlockPos.containing(point);
        for (int i = 0; i < (staff ? 3 : 1); i++) {
            if (i > 0) pos = pos.offset(-4+world.random.nextInt(8),0,-4+world.random.nextInt(8));
            if (!loaded(world,Vec3.atBottomCenterOf(pos))) continue;
            var volcano = ModEntityType.VOLCANO.get().create(world);
            if (volcano == null) continue;
            volcano.setPos(Vec3.atBottomCenterOf(pos));
            if (!free(world,volcano)) continue;
            volcano.setTrueOwner(NativeSummonHooks.owner(owner(frame)));
            NativeLocationHooks.finalizeVolcano(world,volcano,pos);
            volcano.setExplosionPower(volcano.getExplosionPower()+radius/2F);
            volcano.setLifeSpan(6*duration); // Native monolith lifetime is seconds, not ticks.
            volcano.setPotency(potency); volcano.setFlaming(burning);
            volcano.setUpgraded(SpellConfig.PyroclastStun.get());
            if (!volcano.isRemoved()) added |= world.addFreshEntity(volcano);
        }
        return added;
    }
    private static boolean mines(ServerLevel world, CastContext frame, Vec3 point, boolean staff,
                                  int potency, int duration, double radius) {
        boolean added = false;
        for (int i = 0; i < (staff ? 8 : 4); i++) {
            var mine = new BioMine(ModEntityType.BIOMINE.get(),world);
            boolean placed = false;
            // Native helper depends on an entity's current position/bounding box. Sample
            // around the Ars point instead, retaining 16 attempts and eight-block spread.
            for (int attempt = 0; attempt < 16; attempt++) {
                int x = world.random.nextInt(17)-8, z = world.random.nextInt(17)-8;
                if (x*x+z*z < 16) continue;
                var candidate = point.add(x,world.random.nextInt(9)-4,z);
                if (!loaded(world,candidate) || !loadedBetween(world,point,candidate)) continue;
                mine.setPos(candidate);
                if (!free(world,mine)) continue;
                var ray = world.clip(new ClipContext(point.add(0,.5,0),candidate.add(0,.5,0),
                        ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,frame.caster()));
                if (ray.getType() != HitResult.Type.MISS) continue;
                placed = true; break;
            }
            if (!placed) { mine.setPos(point); if (!free(world,mine)) continue; }
            mine.setOwner(owner(frame)); mine.setExtraRadius((float)radius); mine.setExtraDamage(potency);
            mine.setLifeTicks(mine.getLifeTicks()+MathHelper.secondsToTicks(duration));
            mine.setExtraDuration(MathHelper.secondsToTicks(duration));
            if (!world.addFreshEntity(mine)) continue;
            added = true;
            ServerParticleUtil.summonedParticles(world,mine,new ColorUtil(0xffffff),0xffffff,0xffffff);
            world.playSound(null,mine.getX(),mine.getY(),mine.getZ(),ModSounds.BIOMINE_SPAWN.get(),
                    SoundSource.PLAYERS,1,1);
        }
        return added;
    }
    private static boolean teeth(ServerLevel world, CastContext frame, Spell spell, Vec3 point, boolean staff, int potency) {
        boolean added = false;
        if (staff && GoetyResources.sneaking(frame.caster())) {
            // Preserve native inner ring's cosine/cosine geometry and unaugmented damage.
            for (int i = 0; i < 5; i++) {
                double a = i*Math.PI*.4;
                added |= tooth(world,frame,point.add(Math.cos(a)*1.5,0,Math.cos(a)*1.5),0);
            }
            for (int i = 0; i < 8; i++) {
                double a = i*Math.PI*2/8+1.2566371;
                added |= tooth(world,frame,point.add(Math.cos(a)*2.5,0,Math.sin(a)*2.5),potency);
            }
        } else {
            for (int i = 0; i < (staff ? 16 : 1); i++) {
                if (staff) point = point.add(world.random.nextInt(9)-4,0,world.random.nextInt(9)-4);
                added |= tooth(world,frame,point,potency);
            }
        }
        if (added) spell.playSound(world,frame.caster(),SoundEvents.EVOKER_CAST_SPELL);
        return added;
    }
    private static boolean tooth(ServerLevel world, CastContext frame, Vec3 point, int potency) {
        var tooth = new ViciousTooth(ModEntityType.VICIOUS_TOOTH.get(),world);
        var base = BlockPos.containing(point); int y = base.getY();
        // Place below a ceiling, not inside the solid block where the native scan stops.
        for (; y < base.getY()+8; y++) {
            var pos = new BlockPos(base.getX(),y,base.getZ());
            if (!world.hasChunkAt(pos) || world.isOutsideBuildHeight(pos)) return false;
            if (world.getBlockState(pos).blocksMotion()) break;
        }
        var top = new BlockPos(base.getX(),y,base.getZ());
        if (!world.hasChunkAt(top) || world.isOutsideBuildHeight(top)) return false;
        boolean ceiling = world.getBlockState(top).blocksMotion();
        tooth.setPos(point.x,ceiling ? y-tooth.getBbHeight()-.01 : y+.5,point.z);
        if (tooth.getY() < point.y || !free(world,tooth)) return false;
        tooth.setOwner(owner(frame)); tooth.setExtraDamage(potency);
        if (!world.addFreshEntity(tooth)) return false;
        tooth.playSound(ModSounds.TOOTH_SPAWN.get());
        return true;
    }
}
