package dev.goetynouveau.platform;

import com.Polarice3.Goety.client.particles.ShockwaveParticleOption;
import com.Polarice3.Goety.utils.ColorUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Preserve the pinned native version's fire particles and mapped ignition API. */
public final class NativeAreaHooks {
    private NativeAreaHooks() {}
    public static boolean temblorSupported(){return true;}
    public static boolean temblor(ServerLevel world,dev.arsconflux.api.context.CastContext frame,com.hollingsworth.arsnouveau.api.spell.SpellStats ars){
        var hit=frame.target();
        if(hit instanceof net.minecraft.world.phys.EntityHitResult e && (!e.getEntity().isAlive() || e.getEntity().isRemoved() || e.getEntity().level()!=world))return false;
        var point=hit instanceof net.minecraft.world.phys.EntityHitResult e?e.getEntity().position():hit.getLocation();
        var pos=net.minecraft.core.BlockPos.containing(point);
        if(!Double.isFinite(point.lengthSqr()) || !world.isInWorldBounds(pos) || !world.hasChunkAt(pos))return false;
        var source=frame.caster();var spell=new com.Polarice3.Goety.common.magic.spells.geomancy.TemblorClapSpell();
        var stats=com.Polarice3.Goety.utils.WandUtil.getStats(source,spell);
        double radius=stats.getRadius()+ars.getAoeMultiplier();
        double potency=stats.getPotency()+ars.getAmpMultiplier();
        if(!Double.isFinite(radius) || radius<=0 || !Double.isFinite(potency) || potency>Integer.MAX_VALUE)return false;
        stats.setRadius(radius);stats.setPotency((int)Math.max(0,potency));
        var view=new dev.goetynouveau.casting.GoetyNativeGeometry.View(frame,source,spell);
        return dev.goetynouveau.casting.GoetyNativeGeometry.scoped(view,()->{
            spell.SpellResult(world,source,dev.goetynouveau.casting.GoetyResources.staff(source).copy(),stats);return true;
        });
    }
    public static SoundEvent explosionSound() { return SoundEvents.GENERIC_EXPLODE; }
    public static void ignite(LivingEntity target, int seconds) { target.setSecondsOnFire(seconds); }
    public static void fireBlastParticles(ServerLevel world, Vec3 point, float radius, boolean staff, ColorUtil color) {
        world.sendParticles(new ShockwaveParticleOption(color.red(),color.green(),color.blue(),radius,1,true),
                point.x,point.y+.5,point.z,1,0,0,0,0);
        int amount = world.random.nextIntBetweenInclusive(4, staff ? 16 : 8);
        for (int i=0; i<amount; i++) {
            var pos = point.add(0,.5,0).offsetRandom(world.random, radius);
            world.sendParticles(com.Polarice3.Goety.client.particles.ModParticleTypes.BIG_FIRE.get(),
                    pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
    }
}
