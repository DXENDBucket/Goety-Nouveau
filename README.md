# Goety & Nouveau

An independent Goety integration for Ars Nouveau, built on Ars Conflux.

**Status: first playable development slice (`0.1.0-dev.3`). Soul Bolt is available; this is not yet a complete Goety spell catalogue.**

The project provides builds for Minecraft **1.21.1 NeoForge** and **1.20.1 Forge**. Both use the same version file and shared integration source. Iron & Nouveau is not a dependency. Optional Ars projectile integrations belong to Ars Conflux, so each spell addon does not need to implement them again.

Goety & Nouveau and Iron & Nouveau can be installed together with one matching Ars Conflux build. A focused NeoForge server test verifies both providers cast their own native projectiles through the same Ars form, without claiming each other's payloads.

## Try Soul Bolt

Find **Soul Bolt** in the Goety & Nouveau creative tab and learn it by using the glyph item. In an Ars spellbook, compose **Projectile → Soul Bolt**. Soul Bolt is a projectile replacement augment; effects placed after it resolve on impact. For example, **Projectile → Soul Bolt → Ignite** uses the native Goety bolt, then applies Ars Ignite.

Amplify adds one native potency point; Dampen removes one, with a minimum of zero. Ordinary projectiles retain Goety's native launch offset, gradual acceleration, yaw/pitch rotation, collision loop and velocity enchantments. Ars selects the aim and scatter, and its acceleration statistic scales native travel. Special carriers such as Orbit keep their Ars trajectory. The native Goety entity supplies its renderer, animation, trail, impact particles, damage and staff-dependent Soul/Poison/Wither/Necro variants. Projectiles retain a maximum lifetime of ten seconds.

The first slice supplies a creative glyph item; survival acquisition and ritual recipes remain undecided. It does not add Goety casting animations or cooldown enforcement yet.

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

## Dependencies and builds

| Target | Ars Nouveau | Goety | Ars Conflux |
| --- | --- | --- | --- |
| 1.21.1 NeoForge | 5.13.1 | 3.2.0 | 0.1.6 |
| 1.20.1 Forge | 4.12.7 | 2.5.58.4 | 0.1.6 |

These are the pinned build dependencies. Transitive mod dependencies are included in the download manifest, but upstream mod JARs are not committed or bundled in this addon.

See [building instructions](docs/building.md), the [integration plan and API audit](docs/integration-plan.md), and [third-party notices](THIRD_PARTY_NOTICES.md).

## License

MIT © 2026 EndXiom. See [LICENSE](LICENSE).
