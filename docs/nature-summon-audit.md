# Nature, light and native servants

Audited against Goety 3.2.0 (NeoForge 1.21.1) and 2.5.58.4 (Forge 1.20.1). Original Polarice3 implementations are MIT; no upstream JARs, textures or models are bundled. Added in 0.1.0-dev.22.

## Target adapters

- Wind Blast: native gust particles/sound, force and knockback resistance, UNBLOWABLE exclusion, native alliances, elemental staff debuffs, wind-powered block activation and hostile cyclone removal. Ars supplies origin and incoming direction; Self uses caster eyes and native body search bounds. AOE modifies native range, not Ars targeting. No second caster view selection replaces the Ars hit.
- Overgrowth: actual native WandUtil vine structures, ownership, emergence/retraction, trap geometry and poison-quill turret lifecycle. Entity selection chooses one of the five native trap patterns; a location uses the native cube. Sneaking selects turrets and Wild staff doubles the entity-targeted turret batch. Turrets are centered around the Ars point rather than around the distant caster. Structures require loaded surrounding chunks and valid build height.
- Glow Light: actual GlowLight, an Arrow/LightProjectile family rather than SpellThrowableProjectile. Native launch adds look velocity to its nominal speed, so the adapted default is 2.5 blocks/tick. No gravity, both native particle layers, full virtual native hit callbacks, entity Glowing and block light placement remain native. Native lifetime is capped at 600 ticks. Ordinary flight remains native; special carriers retain Conflux motion and each funded hit continues the remaining Ars recipe once. Unmarked native projectiles are unaffected.

## Servant quotas and lifecycle

Zombie, Skeleton and Vex retain their original SummonSpell predicates and configured limits. Existing living loaded native servants of the effective owner occupy the same quota. The addon clips each batch to remaining capacity, avoiding the original pre-batch check allowing a large batch to overshoot. A successful batch invokes native buffSummon, SummonSap, SummonDown and summon advancements; Haunted Skull does not acquire these decay calls because its original spell does not call them.

Zombie/Skeleton default to one servant, matching Necro staff yields 2?5, Nameless yields seven, and supported elemental/Ominous staves yield two. Vex defaults to three, or 3?5 with its matching staff. These batches are clipped to the native limit. Native equipment, Skeleton arrow potency and Vex weapon sharpness are retained. Servants receive the selected living enemy when valid, otherwise native fallback targeting.

| Staff | Zombie | Skeleton |
| --- | --- | --- |
| Frost | Frozen Zombie | Stray |
| Storm | Frayed | Rattled |
| Wild | Jungle Zombie | Mossy Skeleton |
| Nether | Zombie Piglin | Wither Skeleton |
| Abyss | Drowned | Sunken Skeleton |
| Ominous | Zombie Vindicator | Skeleton Pillager |

Without a special staff, native getVariant selects variants at the actual summon location. Ars supplies the summon center; ground placement uses native SummonRadius and moveDownToGround, except aquatic variants. This does not reproduce the caster-centered native underwater search: target-position placement is the bridge's explicit adaptation.

Native base lifespan is random, 30?119 seconds, multiplied by duration. Native ordinary undead apply starvation damage after it runs out; they do not immediately disappear like Haunted Skull. Native persistence/save/owner checks remain in the original entity. No generic continuous soul upkeep is invented. Native summon restrictions differ by spell: temporary magma-cube aftermath is not automatically subject to these three servant quotas.

Book-only sneaking is adapted without pretending the player holds a Goety wand. Zombie/Skeleton/Skull recall uses native teleportServants (including ownership/grounding checks); Skeleton excludes Drowned Necromancer, while Zombie includes it. Vex kills owned Vex/Irk and preserves the Irk heal. The quota precheck cannot block this recall branch. Native command/servant-management systems remain unchanged.

NeoForge uses native getSummonOwner and native duration-enchantment levels. Forge retains direct caster ownership and its original extra +1 duration-enchantment convention. Client classes are not referenced from common server execution.

## Validation boundary

Four focused shared GameTests exercise actual Ars resolution/native callbacks: directional gust/staff/allies/resources, vine/trap/turret ownership and expiry, shared native servant quotas/variants/summon decay/book-only recall, and native light motion/hits/repeating billing. Both loaders execute the same cases; NeoForge also loads Iron and NEG. Native lifetime assertions use the random range, not a fresh random draw as an expected value. The older damaging-projectile loop excludes non-damaging Glow Light, which has its own focused case.

No client is launched. Native renderers and model parents are reused, but particle appearance, late observer interpolation, every environmental variant and every command interaction remain manual checks.
