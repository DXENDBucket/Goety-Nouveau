package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import dev.arsconflux.api.projectile.CarrierRegistry;

/** One native burning unit. The payload owns its meaning and trigger-time resource cost. */
public final class AugmentBurning extends AbstractAugment {
    public static final AugmentBurning INSTANCE = new AugmentBurning();
    private AugmentBurning() { super(CarrierRegistry.id("goety_nouveau:glyph_burning"), "Burning"); }
    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
}
