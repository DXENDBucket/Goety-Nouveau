# Changelog

## 0.1.0-dev.5

- Group Goety glyphs after ordinary Ars/addon glyphs within their original categories, using the new shared Conflux presentation registry. Preserve separate Iron groups and native Iron school ordering.
- Add Haunted Skull as an effect glyph: summon near the selected Ars entity/location, retain native ownership, pursuit, explosion and expiry, and support potency/duration/radius augments.
- Retain native necromancy-staff batch bonuses, enchantments, cape upgrades and shift recall. Charge one focus cost per successful batch; refuse empty recalls, exhausted summon limits, zero lifetime and failed spawns.
- Keep Forge/NeoForge native differences in owner resolution, nameless-staff count and duration enchantments; require Core 0.1.8.
- Add focused category/group and native summon billing/lifetime tests.

## 0.1.0-dev.4

- Add Ice Chunk, Frost Nova, Entangling and Soul Heal as Ars effect glyphs, with Chinese/English descriptions and native focus-model icons.
- Respect the actual Ars target/location, retain native ice/vine entity behavior, Goety explosion/alliance rules, healing/freezing visuals and applicable staff/robe/enchantment bonuses.
- Quote each focus independently and commit soul-energy or Ars-mana payment only when its trigger succeeds; invalid healing targets, fully healed groups and failed spawns are uncharged.
- Keep native Goety payment local to its execution so later foreign effects do not inherit a soul-energy account; verify mixed Iron/Goety continuations through Core.
- Share catalogue, parameter/target adaptation and trigger billing between both loaders. Require Core 0.1.7 and retain Iron-independent operation.
- Add shared native-world tests for selected-target ice impacts, vine control, block placement, ally filtering, healing and effect billing; retain original Soul Bolt regression coverage.
- Retain Polarice3's MIT notice for adapted original spell implementations and clarify the upstream port's separate license terms for additional code.

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
