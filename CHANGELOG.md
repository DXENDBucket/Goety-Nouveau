# Changelog

## 0.1.0-dev.3

- Restore native Goety launch offset, acceleration, gradual model rotation and collision behavior for ordinary Soul Bolt projectiles; special Ars carriers retain their shared trajectory.
- Preserve native velocity enchantments, synchronize motion modifiers and retain exact default speed independently of Ars shot inaccuracy.
- Restore Wither Bolt's native launch sounds and expose the off-world carrier through Core's carrier proxy contract.
- Keep native impacts, Ars continuation, soul billing and repeated-carrier cleanup; compare real Goety and integration motion tick by tick on both loaders.
- Require Core 0.1.6, whose projectile dispatcher ignores client spawn packets before reading a server-only caster context.

## 0.1.0-dev.2

- Add the Soul Bolt projectile replacement glyph, with native Goety projectile rendering and complete impacts followed by remaining Ars effects.
- Retain native staff-dependent Soul, Poison, Wither and Necro variants and potency bonuses; map Ars Amplify/Dampen to potency.
- Implement Goety soul storage/totem billing, native discounts and final soul-loss event pricing. Add functional Ars mana alternative and creative bypass.
- Use Conflux contexts, carrier dispatch, lifecycle cleanup, scatter budgets and repeated impact payments. Require Core 0.1.5; Iron is not required.
- Add Chinese/English descriptions, a creative glyph tab and a focus-model icon reference.
- Add shared behavioral GameTests for both loaders. Survival recipes, native chanting/cooldowns and further spells remain future work.
- Verify NeoForge coexistence with Iron & Nouveau through one shared Core; add an optional development-only runtime test.

## 0.1.0-dev.1

- Establish independent NeoForge 1.21.1 and Forge 1.20.1 projects, native focus/stats access, configuration, pinned dependencies and MIT licensing.
