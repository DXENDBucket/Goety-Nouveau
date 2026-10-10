package dev.goetynouveau.casting;

/** Only replacement entities opt in; native Goety projectiles retain all original behavior. */
public interface ManagedBolt extends dev.arsconflux.api.projectile.CarrierProxy {
    boolean goetyNouveau$managed();
    void goetyNouveau$managed(boolean value);
    boolean goetyNouveau$arsDriven();
    void goetyNouveau$arsDriven(boolean value);
    float goetyNouveau$speedMultiplier();
    void goetyNouveau$speedMultiplier(float value);
    float goetyNouveau$nativeSpeed();
    void goetyNouveau$nativeSpeed(float value);
    dev.arsconflux.api.execution.ExecutionSession<?> goetyNouveau$session();
    void goetyNouveau$session(dev.arsconflux.api.execution.ExecutionSession<?> session);
    void goetyNouveau$trajectory(com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell carrier);
    boolean goetyNouveau$impacting();
    void goetyNouveau$impacting(boolean value);
    default boolean goetyNouveau$handlingHit() { return false; }
    default void goetyNouveau$handlingHit(boolean value) {}
}
