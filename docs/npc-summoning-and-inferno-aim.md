# NPC summon planning and Inferno launch alignment (0.1.9)

- Adds quiet Conflux usefulness predicates for Soul Heal, Chill Hide, Iron Hide and Bulwark. They inspect the actual recipient's health, native effects and native shield count. Blazing/Ghastly predicates query the real native summon quota. These are AI hints, not new player casting restrictions.
- NPC self-summons can inherit their caster's valid living enemy; ordinary player selection and native summon ownership, count, lifespan, Summon Down and payment remain unchanged.
- Chill Hide now validates the Ars-selected recipient in the adapter, like Iron Hide, rather than rejecting an allied cast because the original caster already has the native effect.

## Inferno investigation / rejected workaround

The initially added mouth-alignment marker/helper/constructor Mixin has been removed at the user's request. Do not restore it as a substitute for locating the reported projectile problem.

Verified against the installed Goety 3.2.2 JAR (the relevant classes match the compile fixture):

- Native `Inferno$InfernoAttackGoal.tick()` constructs HellBolt from the target-center minus shooter-center vector. Head rotation is updated separately through LookControl. A temporary difference can exist; this alone does not prove the reported cause.
- Our servant adapter creates the native Inferno, owner, spawn setup and native goals. Its later HellBolts do not enter our replacement-session constructor path. Movement hooks require `ManagedBolt.goetyNouveau$managed() == true`, which defaults false for native shots.
- `WaterHurtingProjectile` stores private x/y/z acceleration components only in its directional constructors. Client packet reconstruction comes from vanilla AbstractHurtingProjectile and restores velocity but not those private components; its save/load methods omit them too. This is an independently identified native prediction/persistence gap, not yet established as the cause of the user's direction report.
- The native HellBolt renderer reads the projectile rotation; native travel rotates toward actual velocity. Initial shooter rotation and gradual projectile rotation are separate from the server's movement vector.

No replacement aiming, additional entity orientation sync or speculative motion patch is installed. A distinction between incorrect actual flight and incorrect visible orientation is still needed to narrow the symptom without launching a client.

## Validation boundary

User requested builds only: no GameTest, server/client launch or shared pack replacement. NeoForge and Forge compilation/packaging and native constructor signatures are checked; actual client visuals and combat remain unverified. See the build handoff for paths/hashes.
