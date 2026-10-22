# Native stats and billing — dev.57

This change restores existing Goety equipment growth and billing; it does not introduce an Ars spell-damage bridge or replace Goety potency with a universal damage multiplier.

## Parameter boundary

Read `WandUtil.getStats(originalCaster, nativeSpell)` once for the baseline. It contains the native defaults plus the caster's potency, duration, range, radius, burning and velocity attributes. It does not contain focus enchantments. The Goety helper decides school selection; the addon does not sum every school's potency.

Manual adapters receive this baseline and retain their per-spell focus-enchantment/Ars-augment rules. Native `SpellResult` paths already obtain their own baseline and retain their native enchantment handling. A baseline prepared by the effect dispatcher is not additionally merged into these native-callback paths. Similarly, the additional-projectile factory computes its own baseline; it does not add the enclosing adapter's values a second time.

Exceptions remain native: Spike's enchanted potency/burning can replace a stat instead of adding; Frost Nova and other spells can use fractional enchantment bonuses. Ignite's small `defaultStats()` object only carries an already prepared burning value into its native touch callback; it is not a second omitted attribute baseline.

Flying changed between supported Goety releases. The adapter precomputes original-caster equipment because the actual executor may be an ally; its scoped native mixin suppresses a second enchantment pass:

| Loader | Original rule | Current rule |
|---|---|---|
| NeoForge | 3.2.0: enchanted focus replaces potency | 3.2.1+: adds focus potency to baseline |
| Forge | 2.5.58.4: enchanted focus replaces potency | 2.5.59.0+: adds focus potency to baseline |

The four local release JARs 3.2.0 / 3.2.1 / 2.5.58.4 / 2.5.59.2 have the same `WandUtil.getStats(LivingEntity, ISpell)` API. No new minimum dependency is required. The intermediate Forge change is supported by upstream [FlyingSpell at 4c521bb](https://github.com/Polarice3/Goety-2/blob/4c521bb74cb055b1331eca005a7928c6f8f47330/src/main/java/com/Polarice3/Goety/common/magic/spells/wind/FlyingSpell.java#L69), whose [version is 2.5.59.0](https://github.com/Polarice3/Goety-2/blob/4c521bb74cb055b1331eca005a7928c6f8f47330/gradle.properties#L19).

## Billing order

1. Obtain the selected glyph's `spell.soulCost(originalCaster, actualStaff)`. This already includes native robe, environment, summon-decay and spell-attribute rules. Never reuse the staff's cached cost: it may hold another focus.
2. If Goety `enchantMultiCost` is enabled and `WandUtil.enchantedFocus(originalCaster)` is true, multiply that native focus cost by two. A merely enchanted book/staff, or Ars Amplify with no enchanted focus, does not qualify.
3. Add configured addon-only augment surcharges at their existing flat amounts. These new augments are not native focus enchantments, so their documented fixed charges are not also doubled. Accumulate in `long`, saturating the eventual quote at `Integer.MAX_VALUE`.
4. `SOUL_ENERGY`: apply the existing armor `SEHelper.soulDiscount`, truncate once, then run the native Soul loss event once at actual activation. Raw storage spending avoids applying the discount/event twice.
5. `ARS_MANA`: use the result of step 3 in the Ars account, retaining the existing absence of Soul-only armor discount and Soul loss event. Native `soulCost` reductions in step 1 still apply. This mode is an account choice, not a claim that one Soul equals one Mana economically.
6. The existing Conflux transaction reserves the price before activation. Failure/exception releases the reservation without spending; success spends once. Creative is free. Repeated triggers retain the same transactional path.

The multiplier follows the actual equipped focus that supplies native enchantments, even when its type differs from the glyph: this matches the existing native `WandUtil` equipment lookup, rather than requiring a new matching-focus inventory rule. Abilities using the shared Goety resource quote use the same equipment-cost policy.

## Verification scope

Focused integration cases exercise three real projectile branches, a real Void Rift effect, the native Fire Breath callback, an actual offhand enchanted focus, native armor discounts, both accounts, failed actions and creative bypass. See the [dev.57 handoff](handoff-dev57.md) for the runs actually completed. Compilation or API inspection of a dependency version is not a gameplay test of that version. Client rendering was not changed and is not asserted by these server checks.
