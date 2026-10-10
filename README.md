# Goety & Nouveau

An independent Goety integration for Ars Nouveau, built on Ars Conflux.

**Status: playable development build (`0.1.0-dev.5`). Soul Bolt and five target/location effects are available; this is not yet a complete Goety spell catalogue.**

The project provides builds for Minecraft **1.21.1 NeoForge** and **1.20.1 Forge**. Both use the same version file and shared integration source. Iron & Nouveau is not a dependency. Optional Ars projectile integrations belong to Ars Conflux, so each spell addon does not need to implement them again.

Goety & Nouveau and Iron & Nouveau can be installed together with one matching Ars Conflux build. A focused NeoForge server test verifies both providers cast their own native projectiles through the same Ars form, without claiming each other's payloads.

## Try Soul Bolt

Find **Soul Bolt** in the Goety & Nouveau creative tab and learn it by using the glyph item. In an Ars spellbook, compose **Projectile → Soul Bolt**. Soul Bolt is a projectile replacement augment; effects placed after it resolve on impact. For example, **Projectile → Soul Bolt → Ignite** uses the native Goety bolt, then applies Ars Ignite.

Amplify adds one native potency point; Dampen removes one, with a minimum of zero. Ordinary projectiles retain Goety's native launch offset, gradual acceleration, yaw/pitch rotation, collision loop and velocity enchantments. Ars selects the aim and scatter, and its acceleration statistic scales native travel. Special carriers such as Orbit keep their Ars trajectory. The native Goety entity supplies its renderer, animation, trail, impact particles, damage and staff-dependent Soul/Poison/Wither/Necro variants. Projectiles retain a maximum lifetime of ten seconds.

The glyphs are available in the creative tab; survival acquisition and ritual recipes remain undecided. Goety casting animations and cooldown enforcement are not added yet.

## Target and location effects

These five glyphs are **effects**, placed after an Ars cast method. They use the actual Ars-selected entity or hit location rather than tracing the caster's view again.

| Glyph | Target and behavior | Ars augments |
| --- | --- | --- |
| Ice Chunk | A native Goety chunk appears four blocks above the entity/location, then gathers frost, tracks and falls using native collision and damage. Frost staffs retain their shorter hover time. | Amplify / Dampen |
| Frost Nova | A freezing shockwave centered on the selected entity/location, with native Goety explosion, ally filtering, damage, freezing and particle types. Frost staffs retain their radius/freezing bonus. | Amplify / Dampen, AOE, Extend Time / Duration Down |
| Entangling | Native vines bind the selected enemy or form a trap at the selected location. AOE selects nearby enemies; Wild staffs retain their larger group selection and target limit, and Wild robes retain vine damage. | AOE, Extend Time / Duration Down |
| Haunted Skull | Native skulls summon near the selected entity/location and keep their owner, AI, explosions and limited lifetime. Necromancy staffs retain batch bonuses; sneak-casting recalls existing skulls. One successful batch pays one focus charge. | Amplify / Dampen, AOE, Extend Time / Duration Down |
| Soul Heal | Heals yourself or the selected native ally/owned creature. AOE also heals nearby native allies, using the original caster's alliance rules. Full-health groups and invalid targets are uncharged. | Amplify / Dampen, AOE |

Examples: **Projectile → Ice Chunk**, **Touch → Entangling → Extend Time**, **Self → Frost Nova**, **Self → Soul Heal**. These are not projectile replacements: the Ars carrier reaches its hit first, then the effect runs. Native damage ownership stays with the original caster, independently of the selected target.

Amplify/Dampen add/subtract Goety potency; Extend Time/Duration Down add/subtract its duration parameter; AOE adds to its radius. Supported native focus potency, duration and radius enchantments and staff/robe bonuses are preserved. Each effect quotes its own focus cost, charges only on a successful trigger, and can trigger again until the selected resource runs out. Their glyph cost is zero; the rest of the Ars spell follows Core's configured billing behavior.

Within each original Ars category, ordinary Ars/addon glyphs come first, followed by contiguous Goety and Iron provider groups. This uses Conflux presentation registration; it does not reclassify effects or change glyph tiers.

## Integration

- Independent mod entry points and metadata for both loaders.
- Shared access to existing Goety focus spells through `IFocus`, without relying on version-specific spell registries.
- Independent projectile executions use Core contexts, lifecycle cleanup and remaining-Ars continuations.
- Server configuration with `resources.source = "SOUL_ENERGY"` by default; `ARS_MANA` is the alternative. This governs Goety glyph triggers only.
- Real Goety active soul storage or the selected carried totem, including native equipment discounts and the native soul-loss event. Creative casting is free. Non-player soul-storage adapters are not implemented; unsupported casters cannot cast for free.
- One payment per ordinary launch; repeated carriers pay per impact and stop when resources run out. A failed spawn is uncharged. The original Ars upfront cost is retained according to Core configuration; the Goety augment itself costs zero Ars mana.
- Shared Conflux carrier dispatch and scatter budgeting. Optional glyph-addon compatibility is owned by Core, without an Iron dependency or pairwise integration code.
- Pinned dependency downloads, source builds, MIT licensing and third-party notices.

Quotes are side-effect-free and include native discounts. Soul-loss events run at actual activation, before the final price is reserved. If another mod changes the event price, its final amount can differ from the quote used for AI/scatter planning; every activation still checks that final amount before spawning or applying its impact.

Each Goety trigger uses its own native payment account without replacing the source account inherited by later effects. An Iron effect after a Goety bolt/effect therefore retains its configured or explicitly supplied resource account.

## Dependencies and builds

| Target | Ars Nouveau | Goety | Ars Conflux |
| --- | --- | --- | --- |
| 1.21.1 NeoForge | 5.13.1 | 3.2.0 | 0.1.8 |
| 1.20.1 Forge | 4.12.7 | 2.5.58.4 | 0.1.8 |

These are the pinned build dependencies. Transitive mod dependencies are included in the download manifest, but upstream mod JARs are not committed or bundled in this addon.

See [building instructions](docs/building.md), the [integration plan and API audit](docs/integration-plan.md), and [third-party notices](THIRD_PARTY_NOTICES.md).

## License

MIT © 2026 EndXiom. See [LICENSE](LICENSE).
