package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.projectile.CarrierProfile;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.arsconflux.api.projectile.ProjectileForm;
import dev.arsconflux.api.resource.ResourceCharge;
import dev.goetynouveau.casting.GoetyResources;
import dev.goetynouveau.casting.SoulBoltCasting;
import java.util.List;

/** An augment selects the native projectile; it does not resolve as a second damage effect. */
public final class SoulBoltGlyph extends AbstractAugment implements ProjectileForm {
    public SoulBoltGlyph() {
        super(CarrierRegistry.id("goety_nouveau:glyph_soul_bolt"), "Soul Bolt");
        spellSchools.add(SpellSchools.CONJURATION);
        SpellSchools.CONJURATION.addSpellPart(this);
    }
    @Override public boolean replace(EntityProjectileSpell carrier, CarrierProfile profile) {
        return SoulBoltCasting.replace(carrier, profile);
    }
    @Override public ResourceCharge initialCharge(SpellContext context, List<AbstractAugment> augments) {
        return GoetyResources.quote(context);
    }
    @Override public int getDefaultManaCost() { return 0; }
    @Override public int getCastingCost() { return 0; }
    @Override public SpellTier defaultTier() { return SpellTier.ONE; }
}
