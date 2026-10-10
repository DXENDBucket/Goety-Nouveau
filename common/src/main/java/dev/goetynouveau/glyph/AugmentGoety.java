package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.AbstractAugment;
import com.hollingsworth.arsnouveau.api.spell.SpellTier;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.goetynouveau.api.EffectFocus;
import dev.goetynouveau.platform.GoetyConfig;
import java.util.List;

/** Dedicated native parameters. Each payload explicitly opts in; these do not change Ars range. */
public final class AugmentGoety extends AbstractAugment {
    public enum Kind { EXTENSION, LIFE_ABSORB, SOUL_EATER }
    public static final AugmentGoety EXTENSION = new AugmentGoety(Kind.EXTENSION, "extension", "Extension");
    public static final AugmentGoety LIFE_ABSORB = new AugmentGoety(Kind.LIFE_ABSORB, "life_absorb", "Life Absorb");
    public static final AugmentGoety SOUL_EATER = new AugmentGoety(Kind.SOUL_EATER, "soul_eater", "Soul Eater");
    public static final List<AugmentGoety> ALL = List.of(EXTENSION, LIFE_ABSORB, SOUL_EATER);
    private final Kind kind;
    private AugmentGoety(Kind kind, String id, String name) {
        super(CarrierRegistry.id("goety_nouveau:glyph_" + id), name);
        this.kind = kind;
    }
    public Kind kind() { return kind; }
    public boolean supports(EffectFocus focus) { return focus == EffectFocus.FANGS; }
    public int limit() { return kind == Kind.LIFE_ABSORB ? 1 : kind == Kind.SOUL_EATER ? 10 : Integer.MAX_VALUE; }
    public int additionalCost() {
        return switch (kind) {
            case EXTENSION -> GoetyConfig.EXTENSION_COST.get();
            case LIFE_ABSORB -> GoetyConfig.LIFE_ABSORB_COST.get();
            case SOUL_EATER -> GoetyConfig.SOUL_EATER_COST.get();
        };
    }
    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
}
