package dev.goetynouveau.casting;

/** Only replacement entities opt in; native Goety projectiles retain all original behavior. */
public interface ManagedBolt {
    boolean goetyNouveau$managed();
    void goetyNouveau$managed(boolean value);
    boolean goetyNouveau$impacting();
    void goetyNouveau$impacting(boolean value);
}
