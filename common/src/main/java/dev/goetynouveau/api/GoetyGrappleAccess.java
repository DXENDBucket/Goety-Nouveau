package dev.goetynouveau.api;
/** Native synchronized hand selector: -1 ordinary Goety, 0 Ars main hand, 1 Ars off hand. */
public interface GoetyGrappleAccess {
    int goetyNouveau$hand();
    void goetyNouveau$hand(int hand);
    default boolean goetyNouveau$managed(){return goetyNouveau$hand()>=0;}
}
