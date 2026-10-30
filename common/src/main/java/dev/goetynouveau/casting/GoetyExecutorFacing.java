package dev.goetynouveau.casting;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Read the real executor's live view, without changing any entity's rotation or ownership. */
public final class GoetyExecutorFacing {
    private static LivingEntity executor(Entity source) {
        var breath=GoetyBreathGeometry.current();
        if(breath!=null&&(source==breath.context().caster()||source==breath.host()))return breath.host();
        var nativeCast=GoetyNativeGeometry.current();
        if(nativeCast!=null&&(source==nativeCast.context().caster()||source==nativeCast.host()))return nativeCast.host();
        return null;
    }
    public static Vec3 look(LivingEntity source) {
        var host=executor(source);
        return host==null?source.getLookAngle():dev.arsconflux.api.execution.ExecutionAnchor.viewDirection(host);
    }
    public static float yaw(Entity source) {
        var host=executor(source);
        return host==null?source.getYRot():host.getViewYRot(1.0F);
    }
    private GoetyExecutorFacing() {}
}
