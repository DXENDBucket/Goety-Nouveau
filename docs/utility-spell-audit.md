# Utility effects and royal spikes

Added in 0.1.0-dev.23. Native source audit uses pinned Goety 3.2.0 (NeoForge 1.21.1) and 2.5.58.4 (Forge 1.20.1), including actual ModItems registrations. All six additions are effects; projectile replacement ordering stays unchanged. No Core changes or new survival acquisition rules.

| Glyph | Native focus | Target adaptation |
| --- | --- | --- |
| Sensing | sensing_focus | Actual living recipient, restricted to caster/native ally. Original Tremor Sense instance, potency/duration, particles flag and debris sound. |
| Rotation | rotation_focus | Actual block and hit face, using native blockCanRotate and blockResult sound. |
| Trembling | trembling_focus | WindHornSpell radial push and both movement/mining debuffs around the selected point. |
| Charge | charge_focus | Direct native touchResult on the actual living recipient. |
| Spikes | shredding_focus | Actual native Spike entities, rooted at the selected point and oriented by incoming direction. |
| Summon Leapleaf | leaping_focus | Actual native servant using the existing summon adapter. |

## Important native branches

Sensing uses the original native effect/event consumers for detecting grounded entities; the addon does not implement a separate wall-detection renderer. Amplify/Dampen changes potency, time augments change duration in seconds. Invalid recipients and ineffective effect additions do not commit a trigger charge.

Rotation preserves native slab/axis/facing/rotation properties, survival checks and exclusions for double-height blocks, beds, native multipart blocks and chests. World interaction checks are retained at the addon entry. Native blockCanRotate mutates the world despite its predicate-like name: it runs inside the funded activation, never during quote/validation. A rejected/no-op rotation is uncharged. The selected face reaches native code unchanged. This is not a general bypass for land-protection mods; third-party permission behavior still requires their integration.

Trembling uses the original distance-based horizontal force, upward push, native alliances, radius-doubling matching staff, CircleExplode/Smash particles and wind-horn sound. The selected point replaces the caster-centered origin. Existing creative/spectator exclusions used by the bridge's other combat effects also apply. Each successful gust pays once; particles and native debuffs are not rebilled per tick.

Charge directly invokes the complete original ChargeSpell.touchResult, including native CHARGED stacking, overcharge shock damage, sound, native chain candidates/hop count and lightning packets. It does not substitute a generic addEffect. Matching Storm staff retains the native chain branch, which has distinct ownership eligibility and can affect nearby recipients instead of the primary one. Result snapshots include health, absorption and charge duration/amplifier across the chain's reachable region; ordinary unchained charge only observes the selected recipient. A true no-op does not commit the charge. No extra potency/duration/chain augment is invented. Charge is allowed on creative recipients as in its native touch logic.

Royal spikes use the native Spike, retaining emergence/sound events, continuous collision damage, alliance checks, burning, native soul gain, save data and 600 active-tick lifetime. Their normal line starts at the actual Ars point, subsequent spikes spaced 1.25 blocks along incoming horizontal direction. Sneaking uses the original two rings (5+8); matching Ominous staff adds the third ring (11). Normal staff casts add two side rows. The native spell is ILL, not GEOMANCY. No new ray trace overrides Ars targeting. Ground search is bounded to local valid build-height/loaded chunks, and failure to find a supported surface does not charge.

Spike gear behavior is unusual: native WandUtil replaces base potency/burning with the focus enchantment levels. The adapter preserves that base, then applies the requested Ars modifiers. Native soul-eater enchantment still reaches the entity, without broadening the dedicated Fangs-only Soul Eater augment. Burning is explicitly eligible for Spikes; editor and execution validation still reject unsupported spells.

Leapleaf uses native LeapleafLimit and predicate, counting existing loaded living servants of the same effective owner. Wild staff summons one or two, clipped to remaining capacity. Actual spawn/finalization, random lifetime, potency, target, SummonSap/SummonDown, uponSummon, advancement and native summon sound remain intact. Sneaking recalls via native teleportServants even with only an Ars book and a full quota. NeoForge effective owner and Forge duration-enchantment +1 differences remain behind the existing platform hooks. No new command AI is added.

## Verification scope

Three focused shared GameTests cover real Ars/native callbacks: selected sensing and actual block rotation including denied payment, native horn push/debuff/allies; charge stacking/overcharge and actual matching-staff chaining; native spike damage/expiry/ring geometry, and native-existing/shared Leapleaf quota, target, lifespan, recall and creative/exhausted payment. NeoForge runs with Iron/NEG; Forge is standalone. Initial fixtures used an incorrect Geomancy staff for the native ILL spike branch; corrected to the registered Ominous staff. No production staff matching was changed to satisfy that check.

No client rendering test or historical spell catalogue rerun. Native lightning packets, spike late-observer animation, wall-detection appearance, claims permissions and environmental summon placements remain manual checks.
