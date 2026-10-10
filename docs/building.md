# Building

## Layout

```text
MinecraftDev/
  Ars-Conflux/              # compatible Core checkout, at least 0.1.8
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

Prepare an Ars Conflux 0.1.8 source checkout beside this project, then run:

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
