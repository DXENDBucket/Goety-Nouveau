# NPC summon planning and Inferno launch alignment (0.1.9)

- Adds quiet Conflux usefulness predicates for Soul Heal, Chill Hide, Iron Hide and Bulwark. They inspect the actual recipient's health, native effects and native shield count. Blazing/Ghastly predicates query the real native summon quota. These are AI hints, not new player casting restrictions.
- NPC self-summons can inherit their caster's valid living enemy; ordinary player selection and native summon ownership, count, lifespan, Summon Down and payment remain unchanged.
- Chill Hide now validates the Ars-selected recipient in the adapter, like Iron Hide, rather than rejecting an allied cast because the original caster already has the native effect.

## Inferno

Inspected native Goety 3.2.2: `Inferno$InfernoAttackGoal.tick()` constructs a HellBolt with a vector to the target center, but only queues a slow 10-degree LookControl turn afterward. The model reads head yaw/pitch, so a native shot can visibly leave in a direction different from the face.

Infernos created by this addon's servant adapter receive the persistent `GoetyNouveauInfernoAim` marker. At the native shooter HellBolt constructor, only a marked Inferno aligns yaw, head yaw, body yaw and pitch to that shot and updates its LookControl. No projectile direction, acceleration, damage, spread, cooldown, renderer or native attack schedule is replaced. Existing unmarked summons and ordinary native Infernos remain unchanged; resummon for this adjustment. The constructor descriptor is shared between loaders and does not depend on an inherited Minecraft method-name mapping.

## Validation boundary

User requested builds only: no GameTest, server/client launch or shared pack replacement. NeoForge and Forge compilation/packaging and native constructor signatures are checked; actual client visuals and combat remain unverified. See the build handoff for paths/hashes.
