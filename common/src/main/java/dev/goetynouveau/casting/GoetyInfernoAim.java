package dev.goetynouveau.casting;

import com.Polarice3.Goety.common.entities.hostile.servants.Inferno;
import net.minecraft.world.entity.LivingEntity;

/** Align an integration-summoned Inferno's visible mouth with its native, target-aimed shot. */
public final class GoetyInfernoAim {
    private static final String KEY="GoetyNouveauInfernoAim";
    public static void mark(LivingEntity servant) {
        if(servant instanceof Inferno)servant.getPersistentData().putBoolean(KEY,true);
    }
    public static void align(LivingEntity shooter,double x,double y,double z) {
        if(!(shooter instanceof Inferno inferno)||shooter.level().isClientSide
                ||!shooter.getPersistentData().getBoolean(KEY))return;
        double length=x*x+y*y+z*z;
        if(!Double.isFinite(length)||length<1.0E-10)return;
        float yaw=(float)Math.toDegrees(Math.atan2(z,x))-90;
        float pitch=(float)-Math.toDegrees(Math.atan2(y,Math.sqrt(x*x+z*z)));
        inferno.setYRot(yaw);inferno.setYHeadRot(yaw);inferno.yBodyRot=yaw;inferno.setXRot(pitch);
        inferno.getLookControl().setLookAt(inferno.getX()+x,inferno.getEyeY()+y,inferno.getZ()+z,180,180);
    }
    private GoetyInfernoAim() {}
}
