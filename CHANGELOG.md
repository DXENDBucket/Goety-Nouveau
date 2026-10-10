# Changelog

## 0.1.0-dev.9

- Sort projectile-replacement augments after the other augments within the Goety provider group; preserve categories and contiguous provider groups on both loaders.

- Add Fangs and Ignite effect glyphs with real Ars hit-point/direction adaptation, native entities and native block/entity ignition.
- Preserve grounded fang placement, warmup, player ownership, native ally filters, Ominous staff side rows/rings, animation and expiry. Native damage events handle lifesteal; native fang hits handle soul gains.
- Add Extension, Life Absorb and Soul Eater dedicated augments. Extend straight fang rows, enable native absorption or add native Soul Eater levels; validate per-payload support, binary absorption, ten-level soul eating and Absorb/Amplify exclusion in both the editor and server execution.
- Expand Burning to Fangs and Ignite; include all dedicated augment fees in trigger quotes and actual resource billing, with independent configuration keys and creative-mode exemption.
- Add native-world coverage for batch geometry, staff variants, delayed damage/healing/soul gains, invalid/no-ground/denied/creative triggers and fire-charge interactions on both loaders.

## 0.1.0-dev.8

- Keep native fireball ignition, smoke and water-projectile trails when Ars/Not Enough Glyphs owns the trajectory. Suppress only native movement and collision instead of cancelling the whole projectile tick.
- Apply the same fix to orbit, splash, trail and other special carriers on both loaders; ordinary native flight remains unchanged.

## 0.1.0-dev.7

- Add Fireball and Poison Dart projectile replacements, plus Shockwave and Cushion effects.
- Preserve native fireball flight, staff salvos, ignition, Unholy Hell Bolt/Hellfire, and the Forge-specific Malefic Helm geometry branch. One native salvo pays once; special Ars carriers move and trigger the native salvo as one batch.
- Preserve native quill gravity, rotation, water behavior, Wild staff piercing, robe-dependent Acid Venom, and one Ars continuation per distinct native hit. Repeated Ars trajectories retain control after block hits and pay at each successful activation.
- Add Burning as a compatible augment for Fireball and Haunted Skull. Each copy adds one native burning unit and a configurable trigger cost, default 5 selected-resource units before native soul discounts; creative casting remains free.
- Validate Burning against the actual payload through Core, retain Goety provider grouping, add Chinese/English descriptions and native model references, and synchronize new native projectile management flags and speed modifiers.
- Add native-world checks for default flight, full impacts, piercing, batch billing, denied/creative casts, native area entities, summon Burning and repeated-carrier resource exhaustion on both loaders.

## 0.1.0-dev.6

- Add native Magic Bolt as a projectile replacement, retaining launch/flight/turn, curse, lingering magic ground on entity/block impact and Ars continuation. Share the native bolt adapter without applying Soul Bolt staff variants or velocity enchantments to Magic Bolt.
- Add Iron Hide, Chill Hide, Weakening, Lightning and Updraft effect glyphs, with actual Ars targets, native effect/entity behavior, visuals, equipment bonuses and per-focus trigger billing.
- Preserve Frost staff group protection, both Weakening debuffs, lightning-rod attraction and Storm staff bypass, and Wind staff area bonuses. Keep native duration units explicit.
- Add glyph icons by referencing native focus models, Chinese/English descriptions, a per-spell behavior audit and a shared Conflux checklist covering client/server boundaries and both loaders.
- Add targeted native-world checks for full Magic Bolt flight/entity/block impacts, protection/ally filtering/billing, and actual lightning/updraft damage and lift.

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
