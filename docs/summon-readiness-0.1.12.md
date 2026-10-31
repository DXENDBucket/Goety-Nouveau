# Summon readiness hints — 0.1.12

Blackguard and Leapleaf now declare the same quiet native-cap usefulness check as Blazing and Ghastly. This lets a caller skip a full summon group before choosing a cast. It grants no resource account, target permission or extra summon capacity.

The read-only check uses the existing owner UUID, alive/owned-entity requirements and native summon predicate. Ordinary summoning stops counting at the native cap; recall stops at the first matching servant. It no longer allocates a complete servant list for a Boolean answer. Actual summoning and recall still use the existing native lifecycle and authoritative cap checks.

Illusion declares summoning/self/position hints. For NPC self casts with no selected enemy receiver, clones use the caster's live hostile target, matching ordinary servant targeting. Native clone equipment variants, lifespan, dismissal, resources and cooldowns remain unchanged.

No new Charge policy, labels or behavior are included. Native Charge stacking and staff chaining remain unchanged.

Both loader artifacts are compiled and packaged. Archive metadata, Forge refmaps and the absence of canceled Charge code are checked statically. No client, server or GameTest is launched for this batch.
