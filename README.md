# Goety & Nouveau

An independent Goety integration for Ars Nouveau, built on Ars Conflux.

**Status: development foundation (`0.1.0-dev.1`). No playable Goety glyphs are registered yet.**

The project provides builds for Minecraft **1.21.1 NeoForge** and **1.20.1 Forge**. Both use the same version file and shared integration source. Iron & Nouveau is not a dependency. Optional Ars projectile integrations belong to Ars Conflux, so each spell addon does not need to implement them again.

## Prepared

- Independent mod entry points and metadata for both loaders.
- Shared access to existing Goety focus spells through `IFocus`, without relying on version-specific spell registries.
- Immutable native spell parameter snapshots; each future execution gets a separate mutable `SpellStat`.
- Server configuration with `resources.source = "SOUL_ENERGY"` by default; `ARS_MANA` is the alternative. This setting will govern Goety glyph triggers when those triggers are implemented.
- Pinned dependency downloads, source builds, MIT licensing and third-party notices.

The next implementation slice is a Soul Bolt glyph: Ars carrier → Goety projectile → complete native impact → remaining Ars effects. Resource charging must be implemented alongside it, including Goety discounts and soul-loss events.

## Dependencies and builds

| Target | Ars Nouveau | Goety | Ars Conflux |
| --- | --- | --- | --- |
| 1.21.1 NeoForge | 5.13.1 | 3.2.0 | 0.1.3 |
| 1.20.1 Forge | 4.12.7 | 2.5.58.4 | 0.1.3 |

These are the pinned build dependencies. Transitive mod dependencies are included in the download manifest, but upstream mod JARs are not committed or bundled in this addon.

See [building instructions](docs/building.md), the [integration plan and API audit](docs/integration-plan.md), and [third-party notices](THIRD_PARTY_NOTICES.md).

## License

MIT © 2026 EndXiom. See [LICENSE](LICENSE).
