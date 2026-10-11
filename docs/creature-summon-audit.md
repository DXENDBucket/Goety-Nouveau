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
