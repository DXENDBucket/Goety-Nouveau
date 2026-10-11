# Servants and barricade audit

Added in `0.1.0-dev.24`, against the pinned Goety 3.2.0 / 2.5.58.4 JARs. Six effects add native `barricade_focus`, `hunting_focus`, `mauling_focus`, `reaping_focus`, `spooky_focus` and `phantasm_focus`. The catalogue now has 70 glyphs: 18 replacements, 48 effects and four parameter augments, covering 66 native focuses. Neither Core nor survival acquisition changes in this batch.

## Shared summon execution

`GoetyServantCasting` handles owner quota (including ordinary Goety summons), funded activation, selected target, native lifetime, spawn hooks, summon decay and recall. `GoetyServantProfiles` handles species selection/count and aquatic/grounded rules. Native entities retain their AI, upkeep, particles, spawn equipment, alliances and expiry; there is no second servant timer or command system. A successful batch pays once; exhausted quota, insufficient resources, impossible space and empty/ineligible recall do not pay. Native SummonDown runs once after a successful batch.

Ars supplies the selected entity/location rather than a second caster-view trace. Native bounded ground/flying searches and variant selection use that location. Water candidate walks retain native offsets/attempts, with loading/height guards. Phantom placement stops beneath solid ceilings and checks the final scaled body, rather than embedding a phantom in a ceiling. No chunk is deliberately loaded by the adapter.

| Focus | Native rules retained |
| --- | --- |
| Hunting | Wild staff 3, special staff 2, otherwise 1. Variant precedence: Necromancy skeleton wolf; storm/Storm staff; mountain/Wind staff; water/Abyss staff; snow/Frost staff; Nether/Nether staff; ordinary black wolf. Snapper skips grounding and recalls only while caster is underwater. Neo calls the public native ring-granted armor hook; Forge has no such hook. |
| Mauling | Wild staff 2, otherwise 1. Water/Abyss gives Gnasher, Frost/cold gives polar bear, low underground positions give cave bear before checking Nether/Hoglin. Native underwater ground/recall rules retained. |
| Reaper | Matching staff 2–5, otherwise 1. Forge Nameless staff 4–7; Neo has no equivalent branch. |
| Wraith | Matching staff 2–5, otherwise 1. Wild staff gives Muck Wraith; others use native environmental variant selection. Forge additionally has Nameless 4–7 and Frost 2. |
| Phantom | Matching staff 1–4, otherwise 1. Forge Nameless 4–7. Shared native spawn-group data, airborne placement, potency-controlled size and limited lifetime. Neo calls buffSummon; Forge deliberately retains its absence. Neither version adds persistence/uponSummon hooks absent from the native spell. |

Existing zombie, skeleton, Vex and Leapleaf also use the shared profiles; their native variants, special counts, equipment and distinct recall behavior remain.

## Barricade

Actual native WandUtil trap helpers create walls/bombs at the Ars selection. Entity targets retain the native three random formations and facing-dependent square opening; blocks use the wall formation. Frost staff uses glacial walls. Sneaking chooses one bomb or a quad (5% base, 25% Geomancy staff). Wall duration and bomb potency retain original interpretation; duration does not extend a bomb fuse. Native entities perform emergence/retraction, explosions, sounds, particles, griefing and removal.

Result cooldown is `(base + 40/60 bomb ticks) × native cooldown discount`, rounded only after multiplication. Normal walls use base cooldown. This preserves fractional discounts using the normal native calculation, rather than copying Barricade's early integer-cast bug. Native focus records and packets are reused; the base active-release callback only extends remaining cooldown and cannot shorten the result cooldown. With delayed/repeated Ars triggers a successful result can refresh the focus cooldown; this does not block an already-issued carrier's triggers. Existing independent config/creative bypass applies.

The native SpellResult is intentionally not called: it would retrace the caster view and write cooldown to the held focus rather than this glyph's focus.

## Validation and remaining manual checks

Both loaders passed five focused `goety_nouveau_summon_wall` cases. Neo includes Iron/NEG coexistence; Forge is standalone. Actual callbacks cover six hunting staff variants, bear/polar/Gnasher selection, aquatic recall exclusion, native quota shared with ordinary summons, selected enemy/owner/life, book-only recall, Phantom airborne size and real native expiry, walls' real retraction, Frost walls, real bomb fuse/removal, result cooldown surviving shared active release, exhausted/creative payment and existing zombie/skeleton/Vex regression.

Initial tests overestimated default potency and wall duration. Expectations were corrected to the actual native defaults: amplified Phantom size 1 and extended wall lifetime 7 seconds. Production base values were not raised to satisfy tests.

No client was launched or broad historical suite rerun. Client renderer/animation/particles, water-environment generation, natural Wraith variants, Neo ring-granted armor, player equipment/upkeep permutations and all random wall/quad branches still benefit from in-game observation. Native implementations are used for these behaviors; not every branch is claimed to have been exercised by GameTest.

The previous six utility glyphs' Chinese placeholder text is also repaired.
