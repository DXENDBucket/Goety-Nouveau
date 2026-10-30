# Non-player soul accounts (0.1.8)

`dev.goetynouveau.api.SoulEnergyAccounts.register(id, priority, lookup)` registers an optional lookup from a living non-player caster to Conflux `ResourceAccount`. A lookup returns null for unsupported casters. Higher priorities run first; equal priorities sort by ID. Duplicate IDs are rejected.

The content integration owns storage, persistence, capacity and any regeneration. It must return equal reservation keys for wrappers of the same physical pool, report truthful affordability, and spend only after a successful transaction. Quotes and lookups must be read-only. The recommended resource key is `ResourcePoolKey.of("goety:soul_energy", actor)`.

In SOUL_ENERGY mode, Goety glyph quotes use the explicitly registered account for a supported NPC. Missing registrations retain the previous unavailable account, never free casting. ARS_MANA mode still selects Ars mana before consulting these providers.

Players always retain native Goety active-soul/totem storage, discounts and the actual activation's native loss event. The API cannot replace player accounts. No synthetic player, generic NPC soul capability or automatic regeneration is installed.

This makes resources usable, not every native focus valid for mobs. Native target, player-only, staff, summon and lifecycle restrictions still apply independently. The API is shared by NeoForge 1.21.1 and Forge 1.20.1.

The first content consumer is Abyss Trials' explicit Weimeng Huanxing entity. Its tests exercise read-only native quotes, successful and failed activation, empty pools, equal reservation keys, nested reservations, and independence from Ars mana. No predefined combat program is added by this resource API.
