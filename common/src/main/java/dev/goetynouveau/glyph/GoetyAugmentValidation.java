package dev.goetynouveau.glyph;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.validation.SpellPhraseValidator;
import dev.goetynouveau.api.BoltFocus;
import dev.goetynouveau.api.EffectFocus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.ArrayList;
import java.util.List;

/** Carrier compatibility alone cannot tell which provider payload an augment modifies. */
public final class GoetyAugmentValidation {
    private GoetyAugmentValidation() {}
    public static List<SpellValidationError> validate(List<AbstractSpellPart> recipe) {
        var errors = new ArrayList<SpellValidationError>();
        for (var phrase : SpellPhraseValidator.splitSpellIntoPhrases(recipe)) {
            if (!phrase.getAugments().contains(AugmentBurning.INSTANCE)) continue;
            boolean supported = phrase.getAction() instanceof GoetyEffectGlyph effect
                    && effect.focus() == EffectFocus.HAUNTED_SKULL;
            supported |= phrase.getAugments().stream().anyMatch(augment -> augment instanceof GoetyBoltGlyph bolt
                    && bolt.focus() == BoltFocus.FIREBALL);
            if (!supported) errors.add(new BurningError(phrase.getFirstPosition(), phrase.getAction()));
        }
        return errors;
    }
    private record BurningError(int position, AbstractSpellPart spellPart) implements SpellValidationError {
        public int getPosition() { return position; }
        public AbstractSpellPart getSpellPart() { return spellPart; }
        public MutableComponent makeTextComponentExisting() { return Component.translatable("goety_nouveau.validation.burning"); }
        public MutableComponent makeTextComponentAdding() { return makeTextComponentExisting(); }
    }
}
