package dev.goetynouveau.casting;

/** Durable spawn/tracking state for instant Ars rifts; zero leaves native rifts untouched. */
public interface RiftVisualState {
    int NATIVE = 0, ACTIVE = 1, CLOSING = 2;
    int goetyNouveau$visualPhase();
    void goetyNouveau$visualPhase(int phase);
}
