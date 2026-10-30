package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.projectile.CarrierProfile;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.projectile.ProjectileForm;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.GoetyBoltCasting;
import dev.goetynouveau.api.BoltFocus;
import java.util.List;

/** An augment selects the native projectile; it does not resolve as a second damage effect. */
public final class GoetyBoltGlyph extends AbstractAugment implements ProjectileForm {
    private final BoltFocus focus;
    public GoetyBoltGlyph(BoltFocus focus) {
        super(CarrierRegistry.id("goety_nouveau:glyph_" + focus.glyph), focus.name);
        this.focus = focus;
                dev.arsconflux.api.glyph.GlyphAbilities.declare(getRegistryName(),java.util.Set.of(dev.arsconflux.api.glyph.AbilityTags.ATTACK,dev.arsconflux.api.glyph.AbilityTags.ENEMY));
        spellSchools.add(SpellSchools.CONJURATION);
        SpellSchools.CONJURATION.addSpellPart(this);
    }
    @Override public boolean replace(EntityProjectileSpell carrier, CarrierProfile profile) {
        return GoetyBoltCasting.replace(focus, carrier, profile);
    }
    @Override public ResourceCharge initialCharge(SpellContext context, List<AbstractAugment> augments) {
        return GoetyResources.quote(context, focus.spell(), augments);
    }
    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
    public BoltFocus focus() { return focus; }
}
