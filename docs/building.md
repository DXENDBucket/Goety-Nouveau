# Building

## Layout

```text
MinecraftDev/
  Ars-Conflux/              # compatible Core checkout, at least 0.1.10
  Goety-Nouveau/
    VERSION                 # single version for both targets
    common/src/main/        # shared integration source and resources
    src/main/               # NeoForge entry point and configuration
    forge/src/main/         # Forge entry point and configuration
    dependencies.lock.json
    tools/
```

Use JDK 21 for the NeoForge Gradle wrapper. The Forge wrapper supports JDK 17 or 21 and compiles with a Java 17 toolchain; a Java 17 toolchain must be available. Python 3.11+ is required by the dependency downloader.

The root wrapper uses Gradle 9.2.1; the Forge wrapper uses Gradle 8.8. Do not replace one with the other.

## Prepare dependencies

Prepare an Ars Conflux 0.1.10 source checkout beside this project, then run:

```powershell
python tools/fetch_dependencies.py
```

The downloader verifies SHA-512 hashes and also supplies Ars Conflux with its direct build dependencies. Files with different hashes are preserved unless `--replace` is explicitly supplied. To fetch only one target, use `--platform neoforge` or `--platform forge`.

For a different Core location:

```powershell
python tools/fetch_dependencies.py --conflux C:/dev/Ars-Conflux
```

## Build both targets

```powershell
./tools/build_all.ps1 -NeoJavaHome 'C:/Program Files/Java/jdk-21' -ForgeJavaHome 'C:/Program Files/Java/jdk-21'
```

With a custom Core checkout, also pass `-ConfluxCheckout C:/dev/Ars-Conflux`. This builds addon and Core artifacts. It does not launch Minecraft or run a broad runtime test suite.

Individual addon builds:

```powershell
./gradlew.bat :jar :sourcesJar --console=plain
cd forge
./gradlew.bat :jar :sourcesJar --console=plain
```

Custom Gradle paths differ between targets: the root expects `-PconfluxCheckout=C:/dev/Ars-Conflux`; Forge expects `-PconfluxCheckout=C:/dev/Ars-Conflux/forge`.

Outputs:

```text
build/libs/goety-nouveau-<VERSION>-mc1.21.1-neoforge.jar
forge/build/libs/goety-nouveau-<VERSION>-mc1.20.1-forge.jar
```

Forge's `:jar` also runs `reobfJar`. Neither addon artifact embeds Goety, Ars Nouveau, Iron & Nouveau or Ars Conflux. Install a matching Core JAR separately.

## Development launches

Both targets expose `:runClient` and `:runServer`; server launches use `--nogui`. These runs include the local addon and Core source sets. Goety's own content remains present, so use disposable test worlds.

Forge development runs explicitly register `ars_conflux.mixins.json`: an included Core source directory has no release-JAR manifest to register it automatically. Production JARs keep their own independent mixin declarations; the addon does not bundle Core.

Use a dedicated Ars integration instance. Its local path can be recorded in the ignored `test-environment.local.json`; personal instance paths are not part of the public source. Build tasks do not automatically install into a game instance. Compilation alone does not verify mod loading, rendering or playable spell behavior.

## Focused runtime validation

Each platform exposes `:runGameTestServer`, containing shared Soul Bolt and target/location-effect scenarios with platform-specific test discovery and fake-player setup. These run with Ars, Goety and Core, without Iron:

```powershell
./gradlew.bat :runGameTestServer --no-daemon --max-workers=2
cd forge
./gradlew.bat :runGameTestServer --no-daemon --max-workers=2
```

Coverage: actual native hit damage and Ars continuation; no duplicated carriers; real player/totem storage, reduced scatter, creative bypass; Ars mana alternative and failed-action billing; repeated carrier payments and cleanup; native soul-loss event pricing; tick-by-tick launch, acceleration, displacement and gradual rotation against a bolt spawned by Goety's actual `SpellResult`, including accelerated travel. Effect cases cover selected-target ice falling damage, native vine restraint/lifetime, block-hit placement, freezing area allies, selected-ally healing and per-focus fees. An account regression exercises native bolt → Goety effect → subsequent provider, preserving both an explicit source account and an absent source account. Headless tests do not inspect client rendering or animations.

On 2026-10-11, the six Forge scenarios passed with Core 0.1.7 after correcting source-directory mixin registration. The launch log confirms Core's configuration is active; production addon code and its version remain unchanged by this development-launch fix.

For 0.1.0-dev.4, nine NeoForge independent scenarios passed before the account regression was added. The final Forge build passed all ten independent scenarios, including that regression; the final NeoForge coexistence run also exercises the shared account regression together with real Iron integration. Both targets compile and produce release/source JARs. Focus-model parents exist in both pinned Goety JARs. Client appearance still requires an in-game check.

For the separate NeoForge coexistence test, place matching Iron & Nouveau, Iron's Spells 'n Spellbooks, irons_lib, Player Animator and Caelus JARs in the ignored `libs/coexistence/` directory. Run from the project root:

```powershell
./gradlew.bat :runGameTestServer -PwithIron=true --no-daemon --max-workers=2
```

This selects the `goety_nouveau_coexistence` namespace and checks real Goety and Iron glyph registration, learning, native projectile dispatch and rejection of two replacement glyphs on a single projectile. It also drives a real Iron Firebolt into Goety's native vines and checks that native Goety payments do not become resource overrides for following providers. The original three scenarios passed on 2026-10-11 with Goety & Nouveau 0.1.0-dev.4, Iron & Nouveau 0.17.6, Core 0.1.7, Iron's Spells 'n Spellbooks 3.16.3, irons_lib 2.2.0, Player Animator 2.0.4 and Caelus 7.0.1. These are optional development runtime dependencies, never bundled or required by this addon.

For 0.1.0-dev.5, the focused NeoForge coexistence run passed all five scenarios with Iron & Nouveau 0.17.7 and Core 0.1.8. The two additional checks cover contiguous provider groups without changing glyph categories, and native skull batches, Ars-selected targets, owner identity, focus payment, summon limits and actual expiry removal. Core's same-rank effect/subform unit regressions also passed. The Forge build passed all twelve independent scenarios, including the same two new sorting/summoning cases. No client rendering check was performed.

## Version control

The initial version is `0.1.0-dev.1`. Dependencies, build scripts and shared code are committed; downloaded JARs, upstream audit checkouts and runtime directories are ignored. No remote publishing is part of the initial preparation.


For 0.1.0-dev.10, the final NeoForge coexistence run passed all 19 required tests; the final Forge independent run passed all 26. New shared cases cover native Wither Skull flight, full hits/explosions, repeated activation and Feast alliance/delayed-damage/billing behavior. See `docs/simple-spell-audit.md` for the fixture corrections and visual-check limits. Both targets produced release/source JARs with current license notices.

2026-10-11 dev.11: one focused GameTest passed on each platform (`build/burning-eligibility.log` and `forge/build/burning-eligibility.log`). It exercises the real Ars editor validator and appended-slot filter for all 20 current payload/effect glyphs, phrase isolation, both sides of native absorption/potency exclusion, and raw unsupported projectile recipes with no spawn/payment. Both targets built release/source JARs. No client was launched.


2026-10-11 dev.12: the two focused shared combat-effect GameTests passed on both NeoForge 1.21.1 and Forge 1.20.1. Run `:runGameTestServer -PgameTestNamespaces=goety_nouveau_combat --max-workers=2`; logs are `build/combat-effects.log` and `forge/build/combat-effects.log`. Checks exercise real native shield capabilities/damage cancellation, native electrical area damage and chain lightning, source ownership, alliance filtering, trigger payment/failure and creative bypass. Both targets build release/source JARs. Native parent models and both languages were checked in the artifacts; client rendering and natural shield expiry remain manual checks. This focused run does not repeat the full catalogue or Iron coexistence suite.


2026-10-11 dev.14: both focused `goety_nouveau_variants` GameTests passed on each loader (`build/variant-report-tests.log` and `forge/build/variant-report-tests.log`). They use a real main-hand Ars book, verify book-only sneaking for Wither Skull/Lavaball/Fireball, and verify an offhand Goety Frost staff creates the native spear with a 50% speed bonus and actual two-target piercing. No broad suite or client launch was performed. These server checks do not establish the user's exact offhand item or verify client rendering.


2026-10-11 dev.15: two focused shared `goety_nouveau_locations` cases passed on each platform. NeoForge used `-PwithIron=true` (Iron and NEG coexistence); Forge used the standalone dependency set. Final logs: `build/location-effects-tests-final.log` and `forge/build/location-effects-tests-final.log`. Checks cover native entity warmup/damage/lift/expiry/staff clusters, native leech healing events and selected-recipient launch movement with native-version speed comparison, failed triggers/resource exhaustion and creative exemption. Both release/source builds succeeded; models/languages/licenses, Java 21/17 bytecode and Forge refmap were checked. First placement run needed a fixture correction to register its simulated caster in the native world UUID index. No broad catalogue rerun or client launch.


2026-10-11 dev.16: all three focused shared `goety_nouveau_hazards` cases passed on each loader. NeoForge used `-PwithIron=true`; Forge used the standalone set. Logs: `build/hazard-effects-tests.log` and `forge/build/hazard-effects-tests.log`. Actual native world ticks verify volcano emergence/owned burning Pyroclasts/descent, mine arming/explosion/extended Acid Pool expiry, tooth ceiling placement/hover/fall/damage/four owned pikes/expiry, native staff batches and charge boundaries. No historical catalogue rerun or client launch. Final artifact builds include the activation sound and loaded-chunk/build-height guards completed during review.


2026-10-11 dev.17: both focused shared `goety_nouveau_weather` cases passed on each loader. NeoForge used `-PwithIron=true` (Iron/NEG coexistence); Forge used the standalone set. Final logs: `build/weather-effects-tests-final.log` and `forge/build/weather-effects-tests-final.log`. Actual native-world checks cover Thunderbolt damage/owner/rod/chaining/rejected damage and cloud warmup/frost/ally filtering/tracking/stationary lightning/expiry/low-ceiling placement/color/billing. First run corrected a fixture assumption: native clouds discard on the tick after active lifetime reaches zero, not that last active tick. No production lifetime change, full catalogue rerun or client launch. Both release/source JARs and 48 glyph models, languages, native model parents, licenses, Java 21/17 bytecode and Forge refmap verified.


2026-10-11 dev.18: focused active-casting checks passed on NeoForge (5 cases with Iron 0.17.11 / NEG) and Forge (4 cases without Iron). Namespaces: `goety_nouveau_casting,goety_nouveau_native_casting`. Checks cover one shared preparation, nested active hooks, cancellation, denied releases, pure Ars passthrough, native Goety focus cooldowns, native charging preparation, and actual Ars caster-tool dispatch. The NeoForge mixed recipe applies both native effects and commits both native cooldowns. Logs: `build/active-casting-release-final.log`, `forge/build/active-casting-tests-final.log`. The shared mock-dispatch fixture uses a player wrapper on both loaders; Ars 4 rejected the former non-player wrapper, correctly producing no release callback. Native Iron Hide is used for the actual preparation test because native Soul Heal is instantaneous. No client was launched; native HUD appearance remains a manual check.


2026-10-11 dev.19: the focused active-casting namespaces passed on both loaders (NeoForge 5 cases with Iron/NEG, Forge 4 standalone cases). Updated checks cover creative default bypass, no new cooldown on release, preservation of an existing native cooldown, survival restoration, explicit creative cooldown opt-in and the mixed recipe with both opt-ins. Logs: `build/preparation-movement-creative.log` and `forge/build/preparation-movement-creative.log`. Client movement hooks target the native Goety input event on both pinned versions; dedicated-server loading passed. Native movement feel, fullStopCast and hidden-HUD behavior remain manual client checks; no client was launched or broad catalogue repeated.


2026-10-11 dev.20: the three focused shared `goety_nouveau_areas` cases passed on both loaders. NeoForge used Iron/NEG coexistence; Forge used standalone dependencies. Final logs: `build/area-effects-tests-final.log` and `forge/build/area-effects-tests-final.log`. Coverage includes native Fire Blast damage/knockback/ignition, Burning, explicit damage ownership and caster alliance filtering with a separate damage owner; Sonic Boom armor/toughness bypass, knockback, entity/self/location targeting and unsuccessful damage; FirePillar native target/sneak/staff formations, actual damage/burning, scaled tick duration and expiry; VoidRift activation, pulling/central damage, allies, complete closing stage/final explosion/removal and staff branch. Both zero-duration and exhausted/creative trigger billing are covered. Unmarked native rifts still cancel under the original wand-state rule.

The rift runtime check exposed native CastSpellTrap cancellation before the first activation tick. A marker-scoped adapter now allows only already-prepared Ars rifts through that guard, retaining the normal native activation event instead of faking native player wand use. The closing-burst fixture was corrected to place its victim inside the native burst radius and outside the central damage zone. No broad spell-catalogue rerun or client launch. Native client particle/model/sound appearance and multiplayer interpolation remain manual checks.


2026-10-11 dev.21: all five focused shared `goety_nouveau_areas` tests passed on both loaders. NeoForge used Iron/NEG coexistence; Forge used standalone dependencies. Logs: `build/rift-visual-tests.log` and `forge/build/rift-visual-tests.log`. The two new cases exercise an unaugmented rift, independent rendering bounds, actual tracking-metadata assignment without activation/closing events, save/load phase restoration and unchanged unmarked native behavior; EarthFist checks native emergence/retreat event replays for a new observer and original-cast exclusion. The first fixture incorrectly assumed the native zero bonus immediately creates zero physical bounds; it now explicitly performs the native dimension refresh, which is the relevant boundary. No production size/damage change was made to satisfy that assertion.

Both patched Minecraft versions send spawn/tracking data before the StartTracking event, allowing observer-only native animation replays. Release/source JARs, license notices, Java 21/17 bytecode, mixin lists and Forge remapped rendering overrides/refmap were checked. Tests exercise native metadata and animation callbacks in server-world replicas, not an actual client's rendering pipeline. No full catalogue rerun or client launch; actual visuals remain a manual check. See `docs/native-visual-audit.md`.


2026-10-11 dev.22: all four focused shared `goety_nouveau_nature` cases passed on both loaders. NeoForge includes Iron/NEG; Forge uses standalone dependencies. Final logs: `build/nature-servants-tests-final.log` and `forge/build/nature-servants-tests-final.log`. Checks cover selected gust origin/direction, native staff/alliance effects, native vines/turret expiry, shared owner quotas, servant variants/targets/equipment/SummonDown, book-only undead/skull recall and Vex dismissal, ordinary servant starvation, and full native light hits with repeated resource exhaustion/continuations. First runs corrected test assumptions about turret emergence, ordinary servant expiry and randomized lifespans; a Forge-only native duration API difference was moved behind the existing platform hook. Servant placement rejects block collisions while retaining native entity overlap behavior. No full historical catalogue rerun or client launch. Actual particle/render appearance and native command interactions remain manual checks. See `docs/nature-summon-audit.md`.


2026-10-11 dev.23: all three focused shared `goety_nouveau_utility` cases passed on both loaders. NeoForge uses Iron/NEG coexistence; Forge is standalone. Logs: `build/utility-tests-release.log`, `forge/build/utility-tests-release.log`. Real native callbacks cover selected Tremor Sense, successful/excluded/unfunded rotation, native radial push/debuff/allies, charge stacking/overcharge and Storm-staff chaining, royal spike ownership/damage/burning/expiry/two- and three-ring geometry, and shared native Leapleaf quota/lifetime/target/book-only recall/exhausted and creative payment. The initial fixture named a non-existent staff field, then used Geomancy where the native spike spell actually requires ILL/Ominous; both fixture assumptions were corrected. No native matching rule was changed. Both release/source JARs build with current MIT notices; all 64 models/languages/native parents, version metadata and Java 21/17 bytecode checked. No client launch or broad historical suite rerun. See `docs/utility-spell-audit.md` for manual rendering/permission boundaries.

2026-10-11 dev.24: five focused shared `goety_nouveau_summon_wall` cases passed on both loaders (Neo with Iron/NEG, Forge standalone), including an existing servant regression. Logs: `build/summon-wall-tests-final.log`, `forge/build/summon-wall-tests-final.log`. New servants and native barricade retain lifecycle, quota, variants and result cooldown; see `docs/summon-wall-audit.md` for checked and manual boundaries. No client launch or broad historical rerun.

Both dev.24 release/source JARs were verified: 70 models and bilingual entries, actual native parent models, version/Core dependency metadata, MIT and current third-party notices, Java 21/17 bytecode, Forge refmap, and no bundled dependencies or test classes.

2026-10-11 dev.25: all five focused shared `goety_nouveau_creatures` cases passed on both loaders. Neo includes Iron/NEG coexistence; Forge is standalone. Logs: `build/creature-summon-tests-final.log`, `forge/build/creature-summon-tests-final.log`. Native entity callbacks verify slime death splitting, shared flies/maggots quota and real kill-produced maggot ownership/life, Sprite expiry, Whisperer variants/recall, Ice Golem limits/resource rejection/creative bypass, guard row geometry/model weapons and loader-specific Blackguards; one existing beast case guards the shared spawn path. See `docs/creature-summon-audit.md`.

Initial tests incorrectly expected nonpersistent player-owned Sprites and inventory weapon items on guards. Native Owned finalization supplies persistence, and guard weapons belong to their model. Only test expectations changed. Both release/source JARs were verified: 77 models and bilingual entries, native parent models, version/Core dependency metadata, current MIT/third-party notices, Java 21/17 bytecode, Forge refmap, and no bundled dependencies/test classes. No client launch or historical catalogue rerun; actual client visuals remain manual checks.


2026-10-11 dev.26: all five focused shared `goety_nouveau_personal` cases passed on both loaders (Neo with Iron/NEG, Forge standalone). Logs: `build/personal-spells-verified.log`, `forge/build/personal-spells-verified.log`. Actual native Killing damage/feedback and resource boundaries, Bolting timed attacks/cleanup, Illusion equipment/metadata/recasts/expiry and an existing EarthFist tracking regression are covered. The vulnerable player fixture respects native spawn protection and self-damage/PVP rules; its creative check separates native kill rewards from payment. No production immunity bypass was introduced. See `docs/personal-spell-audit.md`. Both release/source JARs were verified: 80 glyph models and bilingual entries, native parents, metadata, MIT/notices, Java 21/17 bytecode, Forge refmap and no bundled dependencies/test classes. No client launch or broad historical suite rerun.


2026-10-11 dev.27: four focused shared `goety_nouveau_elemental` tests passed on both loaders (Neo with Iron/NEG, Forge standalone). Logs: `build/elemental-summon-tests-final.log`, `forge/build/elemental-summon-tests-final.log`. Blazing/Ghastly/Guardian equipment branches, native potency/explosion parameters, quota/recall/expiry, water placement, resources/creative and SummonDown are covered, with one existing water-variant summon regression. The water fixture needs actual ServerPlayer doTick; fake-player tick does not refresh immersion. All 83 glyph models and bilingual entries, native parents, release/source notices, Java 21/17 bytecode, metadata and Forge refmap verified. No broad catalogue rerun or client launch. See `docs/creature-summon-audit.md`.

2026-10-11 dev.28: all three focused shared `goety_nouveau_ender` cases passed on both loaders. NeoForge used Iron/NEG coexistence; Forge used standalone dependencies. Logs: `build/ender-summon-tests.log` and `forge/build/ender-summon-tests.log`. Checks cover native servant types, separate owner/focus quotas, Void-staff batch clipping, recall, source ownership, limited life, native Snareling shot and Blastling attack goal, hiding, expiry, resource exhaustion, creative bypass and SummonDown, plus the existing blaze equipment/quota/expiry regression. Both release/source JARs passed model/language/native-parent/license/metadata/bytecode/refmap checks. No client launch or broad historical catalogue rerun; actual appearance and full combat navigation remain manual checks.


2026-10-11 dev.29: both focused `goety_nouveau_breathing` server cases passed on each loader. Checks cover native Fire/Frost/Swarm damage, allied geometry and damage-source identity, ordinary Goety targeting outside the scope, denied enemy executors, per-second payment, changing allegiance, finite duration, creative casts and entity removal. NeoForge includes Iron/NEG; Forge uses standalone dependencies. Final logs: `build/breath-tests-final.log`, `forge/build/breath-tests-final.log`. Fixtures isolate batches and clear their disposable arena because reused test worlds retain earlier entities. Expiry now waits for the actual native duration instead of editing the watched spell config: the old temporary one-tick value could be persisted by Forge's config watcher. No production targeting or duration rule was changed to satisfy these fixtures.

Release/source verification checks all 89 glyph models and bilingual entries, upstream model parents, current MIT notices, Java 21/17 bytecode, Core/version metadata, Forge refmap and absence of dependency/test classes. No historical catalogue rerun or client launch. Actual particle appearance, sounds and input slowdown remain manual checks; see `docs/active-casting.md` for sustained behavior and remaining channel scope.


2026-10-11 dev.30: all four focused `goety_nouveau_remaining_summons` cases passed on NeoForge (current Iron 0.17.12/NEG coexistence); all three passed on Forge. The additional NeoForge case covers the native Breeze focus absent from Forge. Final logs: `build/remaining-summons-tests.log`, `forge/build/remaining-summons-tests.log`. Cases check stone/redstone equipment and staff variants, actual shared quota clipping, recall, buffs/lifetime, short-lived hoglin flank/windup/radius parameters, real contact and expiry explosions, resources, creative and native summon debuff, plus the previous native-breath damage/ownership/alliance regression.

The initial equipment fixture confused Spell.GeoPower with SummonSpell.GeoPower, then changed equipment within one tick after Curios 9 had cached a negative search. It now equips the actual body-slot robe and lets one game tick pass before the native query. Production continues to call the unchanged native summon predicate; no integration gear cache or alternate robe predicate was added. Ordinary Goety entity/gear behavior was not patched to satisfy the fixture.

Both release/source JARs build and pass artifact checks: 92 NeoForge / 91 Forge models and bilingual entries, actual upstream parent resources, current licenses/notices, Java 21/17 bytecode, metadata/Core dependency, Forge refmap, no dependency/test classes, and no NeoForge Breeze model/class reference in Forge. An unavailable Breeze glyph is not constructed on Forge, so it also creates no phantom spell-school entry. Native summon subclass coverage was inspected in both pinned JARs; existing summon command/combat/visual paths were not all rerun. No client launch. Actual rendering, sound, navigation and Breeze wind-charge animation remain manual checks; see `docs/creature-summon-audit.md`.


## Native rains and moving cloud (dev.31)

Run the focused shared cases with `-PgameTestNamespaces=goety_nouveau_rain_storm`; add `-PwithIron=true` on NeoForge for Iron/NEG coexistence. Cases cover native projectile families, remote ally aim and upper cloud placement, explicit ownership, frost-staff rain spears, book-only sneak meteors, burning arrows, staggered meteor cadence, empty resource termination, upkeep and changed allegiance, native moving cloud growth/damage/expiry, and the previous breath upkeep/expiry regression.

The native callbacks stay in Goety; only scoped geometry and explicit projectile ownership are adapted. The geometric scope is bound to its spell instance, restores on exit and retains ordinary virtual ray-trace helper overrides. The original player is never moved and no fake entity replaces it.

Arrow Rain / Blizzard emit every tick. Meteor Shower uses its native Cooldown as emission interval and shotsNumber as finite batch count. Extend Time scales the count, not the native interval. The first batch emits immediately after Ars preparation; ever-charge prepayment covers groups of 20 emissions. This matches breaths' per-second upkeep at one emission per tick, but deliberately avoids charging a slow rain once every second regardless of its native salvos. Configuring a native zero/infinite shot count currently rejects that channel; the bridge remains bounded.

Ice Storm is a native moving freezing cloud, not a rain that fires separate ice spikes. Its server-side area damage, freezing, size growth and native expiry are unchanged. Its native duration parameter changes freezing duration, while range affects cloud lifetime; Ars Extend Time maps to freezing duration as in Goety. Native geometry is the default for ordinary Projectile; optional carriers retain Conflux motion semantics. Client appearance and audio remain manual checks.

Remaining planned area/channel work includes Bombardment, Thunderstorm, Trident Storm, Water Jet, Prisma Beam and Corrupted Beam. They require native use/start/stop callbacks and, for beams, renderer/capability lifetime audit; calling only SpellResult would miss behavior or visuals. This release does not claim these are implemented.

2026-10-11: final dev.31 focused server runs passed all five cases on both loaders (`build/rain-storm-final.log`, `forge/build/rain-storm-final.log`). NeoForge includes current Iron 0.17.12 and NEG; Forge uses the pinned standalone dependencies. The original Forge fixture needed its reused high-altitude ceiling cleared and more than the first vanilla acceleration tick before asserting displacement; the final cases preserve these native differences. Both targets build successfully.

Release/source artifacts verified: 96 NeoForge / 95 Forge models with both language entries, actual Goety parent resources, current MIT and third-party notices, version/Core metadata, Java 21/17 bytecode, Forge refmap, no dependency/test classes and no NeoForge-only Breeze resource/class reference in Forge. No client launch or broad historical projectile suite rerun. Ice Storm is excluded from the older direct-impact-damage loop because its damage is an autonomous area tick, covered by the new moving-cloud case.


## Native bombardment, lightning and trident channels (dev.32)

Run `-PgameTestNamespaces=goety_nouveau_storm_channels` on both loaders; add `-PwithIron=true` on NeoForge for current Iron/NEG coexistence. Four focused cases pass on each loader (`build/storm-channel-tests.log`, `forge/build/storm-channel-tests.log`). Checks cover actual allied hovering/fireball origin, staff salvos, burning and sneak, native cadence/payment/empty-account stopping, Storm/Void trap variants and explicit ownership, alliance stopping, selected trident placement and Abyss layouts, real warmup/strike/expiry, synchronized and saved provenance, unchanged native wand cancellation, creative bypass, scope cleanup and the previous meteor regression.

The first implementation of the shared particle redirect addressed inherited `useSpell`, but the concrete native particle method is `Spell.useParticle`; startup failure exposed that target mismatch. The final four-test runs load both providers successfully. The actual trident case also exposed native preactivation cancellation for players not holding a Goety wand. A synchronized per-entity Ars provenance marker now selectively bypasses that condition, persists across save/load and leaves ordinary Goety traps unchanged. This is a production fix, not a test bypass.

No broad historical catalogue rerun or client launch. Server tests verify actual native callbacks and damage but do not prove client rendering/audio quality. See `docs/storm-channel-audit.md` for continuous billing and remaining renderer work.

Both dev.32 release/source artifacts were verified: 99 NeoForge / 98 Forge glyph models and bilingual entries, native model resources, version/Core metadata, current MIT and third-party notices, Java 21/17 bytecode, Forge refmap for new lifecycle/geometry hooks, and no bundled dependencies or test classes. NeoForge retains its existing native SpellTypeDamage API reference; Forge contains no Vivideru class references or Breeze resources.


## Native corrupted beam channel (dev.33)

Four focused shared `goety_nouveau_beams` cases passed on both loaders. NeoForge includes current Iron/NEG; Forge uses standalone dependencies. Final release/test logs are `build/beam-release-final.log` and `forge/build/beam-release-final.log`. They verify actual native damage, selected spatial owner metadata, explicit damage attribution, living-target aim changes and wall obstruction, ally rejection, native wand cancellation outside the adapter, upkeep, exactly one fee producing actual damage, empty/unfunded stopping, finite creative expiry, both caster/executor removal, orphan cleanup, and the previous meteor cadence/resource regression. No broad historical catalogue rerun or client launch.

The first run exposed the native default CorruptionDuration=0 (infinite held casting), which the bounded adapter had rejected. The final integration gives native infinite channels a configurable finite Ars lifetime, default 200 ticks, without changing ordinary Goety wands. Subsequent checks include damage before committing the first fee so an exact-cost cast is effective, and immediate role/lifetime validation before the native entity damage tick. No native damage, wall collision or rendering method was replaced just to satisfy a fixture.

Both release/source artifacts verified: 100 NeoForge / 99 Forge models and bilingual entries, native parent resources, version/Core metadata, current MIT/third-party notices, Java 21/17 bytecode, new beam hooks and Forge refmap, no bundled dependencies/test classes, and no Vivideru/Breeze references in Forge. Actual native beam render/audio and multiplayer interpolation remain manual checks; see `beam-channel-audit.md`.

## Water Jet and Prisma Beam (dev.34)

Both loaders add native water callbacks and native charged prism results. Real allied executors receive a transient tracked display; native player held-wand rendering/state is not impersonated. Water pays per 20 pulses; prism setup is free and its final callback pays once. Extend Time stretches prism charge with native particle phase clamped to 0..1 to avoid negative bubble spacing.

Focused `goety_nouveau_water_prism` server cases cover real damage/explicit owner, native water extinguishing/Burning/Sapped, visual metadata/live aim, upkeep and empty accounts, creative payment, charged prism timing/final payment, friendly-target and alliance interruption, plus prior corrupted-beam regression. Five cases passed on each loader. Client appearance/audio/interpolation remain manual checks.

Release/source checks: 102 NeoForge / 101 Forge glyph models and bilingual entries, native parent resources, version/Core metadata, MIT and third-party notices, Java 21/17 bytecode, channel display registration and client-only draw accessors, Forge refmap, no bundled dependencies/test classes, no Vivideru/Breeze in Forge. See `water-prism-audit.md` for boundaries and remaining work.

## Native ground/void/ward channels (dev.35)

Void Shock binds to `stellar_focus` (the item class name/asset name is not `void_shock_focus`). Warding has no registered native focus/charm item in the pinned JARs; its native utility callback is exposed through this addon glyph key and the existing charm asset. Quaking has its native forward/radial/staff callbacks, scoped source ownership and bounded downward ground search.

Six targeted `goety_nouveau_ground_channels` cases passed on both loaders: native real damage and owner, ordinary/void-staff emission cadence and hover, upkeep/creative/interruption, native Quaking forward/staff/ring debris, true build-bottom ground search, native player ward data and invalid/unsupported/unfunded recipients. No broad historical catalogue rerun. Actual client appearance/audio remain manual checks.

Release/source checks: 105 NeoForge / 104 Forge glyph models and bilingual entries, native model resources, MIT/notices, version/Core metadata, Java 21/17 bytecode, new mixins/Forge refmap, no bundled dependencies/tests, no Vivideru/Breeze in Forge. Remaining scope is tracked in `ground-channel-audit.md`.

## Native wind/lightning channels (dev.36)

Flying, Whirlwind and Shocking execute actual native callbacks with an allied executor, original-caster equipment/resources and explicit damage ownership. Shocking sends native lightning packets instead of cloned effects. Five focused `goety_nouveau_wind_storm` server cases passed on each loader: actual motion/damage, real-time facing, original Wind staff power/reflection/extinguishing, falling lift, creative/upkeep/empty resources, lost alliance, enemy-executor rejection and native lightning-rod diversion. Client visuals/audio remain manual checks.

Use the fully qualified root task `:runGameTestServer` to avoid also launching the included Core project's unrelated test server with the addon namespace. Release/source checks target 108 NeoForge / 107 Forge glyph models and bilingual entries, native parents, metadata/licenses, Java 21/17 bytecode, scoped mixins/Forge refmap and no bundled dependencies/test classes. Remaining continuous work is tracked in `wind-storm-audit.md`.

## Native grab/mining channels (dev.37)

Telekinesis uses a separate native stateful instance per channel. Burrowing reuses each loader's complete native mining callback with exclusive ownership of its player mining fields and native-wand handoff. No duplicated mining system or synthetic player is introduced.

Six `goety_nouveau_utility_channels` cases passed per loader, followed by the separate `goety_nouveau_mining_handoff` case for the ownership cleanup refinement. This checks native grab concurrency, real collision/source, hurt/range/alliance rejection, real mining/drop/permission/staff/Burning, resources/creative and native mining state handoff. Use fully qualified `:runGameTestServer`; do not rerun the historical catalogue without a new concern.

Release/source checks cover 110 NeoForge / 109 Forge glyph models, bilingual entries/native parents, version/Core/licenses, Java 21/17 classes, scoped mixins/Forge refmap and no bundled dependencies/tests. Actual client appearance/audio remain manual checks. Registered focus coverage is tracked in native-focus-coverage.md.

## Native moving areas (dev.38)

Cyclone and Tidal use actual native mobile area entities at Ars impact points. Four focused `goety_nouveau_moving_areas` cases passed per loader: native retarget replacement and NBT provenance, explicit damage owner, source gear/fire variant, source ally protection, actual wave motion/damage/slam/expiry, Abyss staff spread and failed-charge/creative gates. No historical catalogue rerun. Native client visuals/audio remain manual checks.

Release/source checks cover 112 NeoForge / 111 Forge glyph models, bilingual entries/native parents, Core/version/licenses, Java 21/17 classes, scoped mixins/Forge refmap and no bundled dependencies/tests. See moving-area-audit.md for impact-origin timing and remaining water-whip work.

## Native attached water strike (dev.39)

Water Whip uses the actual native GulfTentacle, retaining native packets/animation, warmup trace, damage, expiry and staff-created extra tentacles. Actual synchronized owner is the allied executor; original source and explicit damage owner use separate persistent provenance.

Four focused `goety_nouveau_tentacles` cases passed per loader after replacing a protected fresh FakePlayer native-isolation damage probe with a real Cow. Real native damage, attachment, offspring/NBT, ally filtering/interruption, unmarked native behavior, resource/recipient rejection and creative/animation hooks are covered. No historical catalogue rerun; actual client visuals/audio remain manual checks.

Release/source checks cover 113 NeoForge / 112 Forge glyph models, bilingual text/native parents, Core/version/licenses, Java 21/17 bytecode, mixins/Forge refmap and no bundled dependencies/tests. See tentacle-audit.md for role boundaries and native timing.

## Native periodic illumination (dev.40)

Illuminate uses native scan/place/convert callbacks and original focus parameters, with live allied-executor geometry. A successful native block change pays through the existing Core resource transaction; empty searches remain unpaid and can follow movement. Actual charge-up replaces the held-item duration in preparation.

Four focused goety_nouveau_illuminate cases passed per loader after clearing the full source/host/support fixture boundary. A separate goety_nouveau_illuminate_edge case also passed on each loader for zero native scan period. Real native light blocks/conversion, source geometry, fee/cadence, empty searching, interrupted/expired execution, creative, invalid recipients and loaded/build bounds are covered. No historical catalogue rerun. Client appearance/audio remain manual checks.

Release/source checks cover 114 NeoForge / 113 Forge glyph models and bilingual/native resources, Java 21/17 classes, scoped mixins/Forge refmap, Core/version/license metadata and no bundled dependencies/tests. See illuminate-audit.md for timing, payment and native-scope boundaries.
