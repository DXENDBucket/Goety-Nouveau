# Goety & Nouveau

An independent Goety integration for Ars Nouveau, built on Ars Conflux.

**Status: playable development build (`0.1.0-dev.10`). Five projectile replacements, fifteen target/location effects and four native parameter augments are available; this is not yet a complete Goety spell catalogue.**

The project provides builds for Minecraft **1.21.1 NeoForge** and **1.20.1 Forge**. Both use the same version file and shared integration source. Iron & Nouveau is not a dependency. Optional Ars projectile integrations belong to Ars Conflux, so each spell addon does not need to implement them again.

Goety & Nouveau and Iron & Nouveau can be installed together with one matching Ars Conflux build. A focused NeoForge server test verifies both providers cast their own native projectiles through the same Ars form, without claiming each other's payloads.

## Try Soul Bolt

Find **Soul Bolt** in the Goety & Nouveau creative tab and learn it by using the glyph item. In an Ars spellbook, compose **Projectile → Soul Bolt**. Soul Bolt is a projectile replacement augment; effects placed after it resolve on impact. For example, **Projectile → Soul Bolt → Ignite** uses the native Goety bolt, then applies Ars Ignite.

Amplify adds one native potency point; Dampen removes one, with a minimum of zero. Ordinary projectiles retain Goety's native launch offset, gradual acceleration, yaw/pitch rotation, collision loop and velocity enchantments. Ars selects the aim and scatter, and its acceleration statistic scales native travel. Special carriers such as Orbit keep their Ars trajectory. The native Goety entity supplies its renderer, animation, trail, impact particles, damage and staff-dependent Soul/Poison/Wither/Necro variants. Projectiles retain a maximum lifetime of ten seconds.

The glyphs are available in the creative tab; survival acquisition and ritual recipes remain undecided. Goety casting animations and cooldown enforcement are not added yet.

## Target and location effects

These fifteen glyphs are **effects**, placed after an Ars cast method. They use the actual Ars-selected entity or hit location rather than tracing the caster's view again.

| Glyph | Target and behavior | Ars augments |
| --- | --- | --- |
| Ice Chunk | A native Goety chunk appears four blocks above the entity/location, then gathers frost, tracks and falls using native collision and damage. Frost staffs retain their shorter hover time. | Amplify / Dampen |
| Frost Nova | A freezing shockwave centered on the selected entity/location, with native Goety explosion, ally filtering, damage, freezing and particle types. Frost staffs retain their radius/freezing bonus. | Amplify / Dampen, AOE, Extend Time / Duration Down |
| Entangling | Native vines bind the selected enemy or form a trap at the selected location. AOE selects nearby enemies; Wild staffs retain their larger group selection and target limit, and Wild robes retain vine damage. | AOE, Extend Time / Duration Down |
| Haunted Skull | Native skulls summon near the selected entity/location and keep their owner, AI, explosions and limited lifetime. Necromancy staffs retain batch bonuses; sneak-casting recalls existing skulls. One successful batch pays one focus charge. | Amplify / Dampen, AOE, Extend Time / Duration Down, Burning |
| Soul Heal | Heals yourself or the selected native ally/owned creature. AOE also heals nearby native allies, using the original caster's alliance rules. Full-health groups and invalid targets are uncharged. | Amplify / Dampen, AOE |
| Iron Hide | Protects yourself or the selected ally using native Iron Hide, including its hidden effect particles. One duration unit is one minute. | Amplify / Dampen, Extend Time / Duration Down |
| Chill Hide | Native Chill Hide protects yourself or the selected ally; a Frost staff also protects nearby allies. One duration unit is 45 seconds. | Amplify / Dampen, Extend Time / Duration Down |
| Weakening | A native weakening wave at the selected entity/location, with both Weakness and Sapped, native particles/sounds and original-caster ally filtering. | Amplify / Dampen, AOE, Extend Time / Duration Down |
| Lightning | Native lightning at the selected entity/block, including native damage, visuals and interactions. Nearby rods redirect it unless using the native Storm staff. | Amplify / Dampen |
| Updraft | Native delayed damage and upward launch at the selected entity or above the selected block. Wind staff retains its area bonus. | Amplify / Dampen, AOE |
| Shockwave | Native area damage, ring/sphere particles and sound centered on the selected entity/location. | Amplify / Dampen, AOE |
| Cushion | Creates a native cushion at the selected entity or above the selected block, retaining native interactions and expiry. | AOE, Extend Time / Duration Down |
| Fangs | Native delayed fangs start at the selected point, along the incoming projectile direction or caster facing. Sneaking switches to rings; Ominous staff adds side rows/outer rings. Actual native damage, animation, lifetime, lifesteal and soul gains remain native. | Amplify / Dampen, Burning, Extension, Life Absorb, Soul Eater |
| Feast | Native fangs attack non-allied creatures in the selected area, with one charge for the whole batch. Ominous staff adds five surrounding fangs per enemy. | Amplify / Dampen, AOE, Burning, Life Absorb |
| Ignite | Uses Goety's entity ignition and original fire-charge block interactions at the Ars hit. Lights supported campfires/candles and places fire without another view ray trace. | Burning |

**Magic Bolt** is the second projectile replacement: **Projectile → Magic Bolt → Ignite**. It shares only the native `SpellHurtingProjectile` flight adapter with Soul Bolt. Its own native curse and lingering `MagicGround` run on impact before the remaining Ars effects, including block impacts. Amplify/Dampen modify potency; Extend Time/Duration Down modify its extra curse/ground duration. Native flight, initial model turn, trails and impact entities are preserved. Magic Bolt keeps its native five-second expiry; it does not inherit Soul Bolt's staff variants or velocity enchantment.

**Fireball** and **Poison Dart** are also projectile replacements. Ordinary Fireball uses native SmallFireball flight, ignition and staff salvos; Unholy equipment selects a native Hell Bolt with lingering Hellfire. The Forge build retains its native Malefic Helm launch branch. Poison Dart retains native arrow gravity, rotation, underwater behavior, Wild staff piercing and robe-dependent Acid Venom. Amplify/Dampen modify their potency; Poison Dart also supports duration modifiers. Both support Ars speed modifiers. Poison Dart keeps its native five-second lifetime, while Fireball keeps ten seconds.

**Wither Skull** replaces the projectile with Goety's native exploding skull. It retains native flight, Wither II, kill healing, native explosion/griefing/loot rules, the sneaking blue variant and Nether-staff three-shot salvo. Amplify/Dampen modify potency, AOE adds explosion radius, and Burning enables the native fiery explosion. Ordinary salvos pay once at launch; special carriers use the same shared-trajectory batch rule described below. The native entity remains responsible for expiry and renderer state. Ars duration modifiers do not change its Wither debuff.

**Burning** increases a supported spell's native burning intensity in the same phrase; its concrete effect depends on the spell. Example: **Projectile → Fireball → Burning → Amplify**. It changes the native payload rather than appending Ars Ignite. Each unit adds a configurable trigger cost (`resources.burningCostPerUnit`, default `5`, before soul discounts); creative casts are free. It has zero upfront Ars glyph cost. Unsupported payload combinations are rejected through Core's shared validator.


**Extension** and **Soul Eater** are dedicated Fangs augments; **Life Absorb** works with Fangs and Feast. Extension adds one native range unit: one additional fang and 1.25 blocks per straight row; it does not extend Ars targeting or change the sneaking rings. Life Absorb enables the native player-owner lifesteal handler, with at most one per phrase and no Amplify/potency bonus. Soul Eater adds a native level up to ten, returning native soul energy on eligible successful fang hits; it never regenerates Ars mana. Both versions retain their original soul-gain eligibility checks and event hooks.

Each new augment defaults to 5 extra selected-resource units before soul discounts. Configure `resources.extensionCostPerUnit`, `resources.lifeAbsorbCost` and `resources.soulEaterCostPerUnit`; all are paid only at the actual trigger. Invalid combinations are rejected by Core's validator and at server execution. Example: **Touch → Fangs → Life Absorb → Soul Eater**, or **Projectile → Fangs → Extension → Burning**.

Feast uses the Ars-selected center with the native search volume and ally rules. Its native radius enchantment multiplies the search radius; Ominous staff adds native warmup rings at each enemy. All generated fangs keep their actual owner, animation, damage and absorption. No enemies or no suitable ground means no paid spawn. Ground-dust rings and poof particles execute at activation; Goety's charging prelude has not been added.

Fangs' geometry is adapted explicitly: its first central fang occupies the Ars-selected point; subsequent fangs follow the incoming direction, with grounded surface placement near each point. Sneaking rings are centered on that point. This does not ray-trace from the caster a second time. One successful row/ring batch pays once; no suitable ground or insufficient resources produces no paid spawn. Ignite only charges if ignition changes or a native block interaction succeeds.

A Nether staff's three-fireball or three-skull salvo is one native batch and pays one focus charge. Ordinary salvos retain separate native flight and hits. For special Ars carriers, the salvo follows one shared Ars trajectory; its native payloads resolve at that trajectory's hit, each continuing the remaining Ars effects. Repeated carriers pay once per triggered batch, including Burning, and stop on insufficient resources. This is an explicit trajectory adaptation rather than three independent native orbit paths.

Examples: **Projectile → Ice Chunk**, **Touch → Entangling → Extend Time**, **Self → Frost Nova**, **Self → Soul Heal**. These are not projectile replacements: the Ars carrier reaches its hit first, then the effect runs. Native damage ownership stays with the original caster, independently of the selected target.

Amplify/Dampen add/subtract Goety potency; Extend Time/Duration Down add/subtract its duration parameter; AOE adds to its radius. Supported native focus potency, duration and radius enchantments and staff/robe bonuses are preserved. Each effect quotes its own focus cost, charges only on a successful trigger, and can trigger again until the selected resource runs out. Their glyph cost is zero; the rest of the Ars spell follows Core's configured billing behavior.

Within each original Ars category, ordinary Ars/addon glyphs come first, followed by contiguous Goety and Iron provider groups. Within the Goety group, its ordinary augments precede its projectile replacements, before the next provider group. This uses Conflux presentation registration; it does not reclassify effects or change glyph tiers.

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

See [building instructions](docs/building.md), the [integration plan and API audit](docs/integration-plan.md), the [per-spell behavior audit](docs/simple-spell-audit.md), and [third-party notices](THIRD_PARTY_NOTICES.md). The shared [adapter checklist](https://github.com/DXENDBucket/Ars-Conflux/blob/main/docs/spell-adapter-checklist.md) covers full native behavior, logical sides and both loaders.

## License

MIT © 2026 EndXiom. See [LICENSE](LICENSE).
