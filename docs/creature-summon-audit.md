# Creature summon audit

Added in `0.1.0-dev.25`, against Goety 3.2.0 (NeoForge) and 2.5.58.4 (Forge). Seven native summon focuses become target/location effects: Slimy, Carrion, Whispering, Frostborn, Sprite, Vanguard and Blackguard. The catalogue has 77 glyphs: 18 projectile replacements, 55 effects and four parameter augments, covering 73 native focuses. Core and survival acquisition are unchanged.

## Shared execution and native differences

Ars selects the center and optional enemy. The shared servant adapter retains native owner identity, native quota predicates, spell buffs, lifespan, summon decay, equipment, advancement and book-only sneak recall. Counts are clipped to the remaining native owner quota; one successful batch pays once. No space, no resources, full quota or empty recall costs nothing. Native entities retain their own AI and lifecycle; the integration adds no separate command or expiry system.

| Focus | Native rules |
| --- | --- |
| Slimy | Wild staff summons 2–3, special staffs 2, otherwise 1. Abyss / Necromancy / Nether staff select Tropical / Crypt / Magma variants. Without a special staff, water, Nether dimension and a native Crypt structure select the corresponding variants. All begin at size 2 after native finalization. Tropical slimes skip grounding. |
| Carrion | Wild staff summons 3–6 flies, otherwise 3. Native quota and recall include both flies and maggots. Flies spawn one block above the selected center with random horizontal offsets. Persistence and summon hooks remain enabled. Native kill callbacks create maggots and transfer owner and remaining limited life. |
| Whispering | Wild staff summons 2, otherwise 1. Water or Abyss staff selects Wavewhisperer, which skips grounding. Native recall allows Wavewhisperers on land; the Hunting/Mauling underwater recall exclusion is not applied here. |
| Frostborn | Frost staff may summon 2, otherwise 1. Native Ice Golem grounding, buffs, persistence, lifespan and recall are retained. |
| Sprite | Storm staff summons 2–5, otherwise 1. Native airborne offsets and caster rotation are retained. The adapter does not force persistence or uponSummon: the original spell does not call them. Native Owned finalization still makes player-owned sprites persistent. |
| Vanguard | Necromancy staff summons 2–7, Nameless staff 7, otherwise 1. Nameless summons form the native seven-column row, centered on the Ars-selected position and oriented by the caster. Blocked positions fall back to the native bounded search. Buffs precede finalization as in the native spell. |
| Blackguard | Vanguard's count/formation rules. Forge additionally retains Ominous staff's two royal variants; the pinned Neo version lacks that branch and summons one ordinary Blackguard. Native model weapons and guard-specific sounds/particles are retained. Vanguard/Blackguard do not populate inventory weapon items. |

Vanguard inherits the native skeleton servant class, but its spell does not set skeleton ArrowPower; the adapter now restricts that call to the actual Skeleton focus. Both guards retain their own summon sound. Forge's public native Nameless summon helper supplies formation particles; Neo uses the original MIT particle/sound sequence because that helper does not exist there.

## Children and scope

Slime death splitting and Carrion kill-produced maggots run through their actual native entity callbacks. Children count toward the same native quota predicate and remain eligible for recall. Slime tests cover splitting while the parent still has positive remaining limited life. The native slime implementation copies limited life only when it is positive; splitting after expiry can therefore differ, and this batch does not claim to repair that upstream edge case.

Swarm is a native breathing spell, despite its name. It is deferred rather than represented as a one-shot servant summon. No servant command focus is introduced here.

## Validation

The focused `goety_nouveau_creatures` namespace covers staff variants, size and real slime splitting, flies/maggots sharing quota, native kill-produced maggot ownership/life, Sprite airborne placement and native expiry, Whisperer/Wavewhisperer recall, Ice Golem quota/resource rejection/creative bypass, guard row geometry and equipment, loader-specific Blackguard variants, and an existing beast-summon regression. All five cases passed on each loader; Neo includes Iron/NEG coexistence and Forge uses standalone dependencies.

Actual client rendering, particle appearance, animations and multiplayer interpolation remain manual checks. Environmental water/Nether/Crypt variants, all randomized counts, equipment combinations and summon upkeep are source-audited rather than exhaustively tested. No client launch or broad historical catalogue rerun is needed for this batch.


## dev.27: Nether and Abyss summons

Three more native focuses join the shared servant adapter: Blazing, Ghastly and Guardian. Total catalogue: 83 glyphs (18 projectile replacements, 61 effects, four parameter augments), covering 79 native focuses. Pinned upstream spell classes were inspected separately on both loaders.

- Blazing creates one native BlazeServant, or a native 2?5 batch with the Nether staff, clipped to remaining native quota. The full Unholy set creates Infernos before the spawn search so collision checks use their actual body. Ground/water search, lifetime, persistence, native fireball potency buff, SummonSap, SummonDown, fiery sound and uponSummon particles remain native.
- Ghastly creates three airborne MiniGhasts, or 3?5 with the Nether staff. Native NetherPower (Nether/Unholy robe) reduces the batch by two and selects GhastServants; the full Unholy set selects Malghasts. Large bodies use the native flying radius 12 around the Ars point plus two vertical blocks, without dragging them down to the ground. Native fireball potency clamp, caster Weakness adjustment and extra explosion power are retained. Neo's native additional buffSummon is applied; Forge's native spell omits it. No explicit persistence call is invented (native owned finalization may supply persistence).
- Guardian creates one GuardianServant or a pair with an Abyss staff. Underwater casting uses the guarded shared native-style water candidate walk centered on the Ars selection. No extra moveDownToGround is added: the original guardian spell does not call it. Native lifetime, persistence, buffs, particles and drowned-necromancer summon sound remain active.

All variants share their native focus's owner quota and book-only sneak recall. Owner and payer remain the caster, independently of the selected target or explicit damage source. Native entities retain attacks, movement, drops, skin/rendering and expiry; the addon does not introduce replacement AI or a parallel lifetime timer. Failed spawns and exhausted accounts do not pay. Native lifecycle/config changes apply equally to ordinary Goety summons.

The focused `goety_nouveau_elemental` namespace checks these three focuses plus one existing water/variant summon regression. Client particle appearance, rendering, sounds and full servant combat AI remain manual checks.

All four focused dev.27 cases passed on both loaders. Neo uses Iron/NEG coexistence; Forge uses standalone dependencies. Logs: `build/elemental-summon-tests-final.log` and `forge/build/elemental-summon-tests-final.log`. Checks exercise native Curios detection, Inferno/three ghast branches, live owner quota/recall, actual native fireball/explosion parameter state, water placement, resource exhaustion, creative bypass, SummonDown and native expiry callbacks. The first water fixture used a fake player whose tick does not update immersion; it was corrected to a real ServerPlayer with actual doTick updates. No production immersion rule was changed. Both release/source artifacts passed model/language/parent/license/metadata/bytecode/refmap checks; no client or historical catalogue suite was launched.


## dev.28: Ender servants

Watching, Snaring and Blasting add three native summon focuses as Ars effects. Total: 86 glyphs (18 projectile replacements, 64 effects, four parameter augments), covering 82 native focuses. The fixed Neo and Forge JARs both use one servant, or a random 2?5 with the matching Void staff. Native owner selection differs by loader and remains behind the existing platform hook.

All three reuse the existing grounded native summon path at the Ars-selected location: original body, native limited life/persistence/finalization, potency buff, SummonSap, selected enemy (or native target fallback), uponSummon particles, advancement, SummonDown and summon sound. Each native focus predicate keeps its own quota; another owner's servants do not consume it. Book-only sneak casts recall eligible native servants without creating a new batch, including at quota. Failure and empty recall do not commit resources; creative keeps free activation.

Their inherited AbstractEnderling combat, hiding and teleport behavior remains native. Snareling's actual ranged callback creates SnarelingShot. Blastling's performRangedAttack is empty upstream: its BlastlingRangedGoal owns windup, alternating-arm EnderGoo shots and post-attack state. The adapter does not replace that with a synthetic projectile or treat the empty callback as missing functionality. Native hide/expiry and projectile effects remain responsible for visuals and lifecycle.

The focused `goety_nouveau_ender` namespace covers independent quota/owner predicates, native batch clipping, recall, actual native ranged goal/shot callbacks and hide/expiry, resource/creative and SummonDown, plus an existing blaze summon regression. Client rendering and full combat navigation remain manual checks.

2026-10-11 dev.28: all three focused shared `goety_nouveau_ender` cases passed on both loaders. NeoForge used Iron/NEG coexistence; Forge used standalone dependencies. Logs: `build/ender-summon-tests.log` and `forge/build/ender-summon-tests.log`. Checks cover native servant types, separate owner/focus quotas, Void-staff batch clipping, recall, source ownership, limited life, native Snareling shot and Blastling attack goal, hiding, expiry, resource exhaustion, creative bypass and SummonDown, plus the existing blaze equipment/quota/expiry regression. Both release/source JARs passed model/language/native-parent/license/metadata/bytecode/refmap checks. No client launch or broad historical catalogue rerun; actual appearance and full combat navigation remain manual checks.
