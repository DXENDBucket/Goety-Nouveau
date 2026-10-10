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
    public static boolean supportsBurning(EffectFocus focus) {
        return focus == EffectFocus.HAUNTED_SKULL || focus == EffectFocus.FANGS || focus == EffectFocus.IGNITE;
    }
    /** Also enforced at server execution for generated/raw recipes that skip the editor. */
    public static boolean validEffect(EffectFocus focus, List<AbstractAugment> augments) {
        if (augments.contains(AugmentBurning.INSTANCE) && !supportsBurning(focus)) return false;
        for (var augment : AugmentGoety.ALL) {
            long count = augments.stream().filter(a -> a == augment).count();
            if (count > 0 && (!augment.supports(focus) || count > augment.limit())) return false;
        }
        return !augments.contains(AugmentGoety.LIFE_ABSORB)
                || !augments.contains(com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify.INSTANCE);
    }
    public static List<SpellValidationError> validate(List<AbstractSpellPart> recipe) {
        var errors = new ArrayList<SpellValidationError>();
        for (var phrase : SpellPhraseValidator.splitSpellIntoPhrases(recipe)) {
            var augments = phrase.getAugments();
            var focus = phrase.getAction() instanceof GoetyEffectGlyph effect ? effect.focus() : null;
            if (augments.contains(AugmentBurning.INSTANCE)) {
                boolean supported = focus != null && supportsBurning(focus);
                supported |= augments.stream().anyMatch(augment -> augment instanceof GoetyBoltGlyph bolt
                        && bolt.focus() == BoltFocus.FIREBALL);
                if (!supported) errors.add(new AugmentError(phrase.getFirstPosition(), phrase.getAction(), "burning"));
            }
            for (var augment : AugmentGoety.ALL) {
                long count = augments.stream().filter(a -> a == augment).count();
                if (count == 0) continue;
                String key = focus == null || !augment.supports(focus) ? "native_parameter"
                        : count > augment.limit() ? "augment_limit"
                        : augment == AugmentGoety.LIFE_ABSORB
                            && augments.contains(com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify.INSTANCE)
                                ? "absorb_potency" : null;
                if (key != null) errors.add(new AugmentError(phrase.getFirstPosition(), phrase.getAction(), key));
            }
        }
        return errors;
    }
    private record AugmentError(int position, AbstractSpellPart spellPart, String key) implements SpellValidationError {
        public int getPosition() { return position; }
        public AbstractSpellPart getSpellPart() { return spellPart; }
        public MutableComponent makeTextComponentExisting() { return Component.translatable("goety_nouveau.validation." + key); }
        public MutableComponent makeTextComponentAdding() { return makeTextComponentExisting(); }
    }
}
