# Building

## Layout

```text
MinecraftDev/
  Ars-Conflux/              # compatible Core checkout, at least 0.1.6
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

Prepare an Ars Conflux 0.1.6 source checkout beside this project, then run:

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

Use a dedicated Ars integration instance. Its local path can be recorded in the ignored `test-environment.local.json`; personal instance paths are not part of the public source. Build tasks do not automatically install into a game instance. Compilation alone does not verify mod loading, rendering or playable spell behavior.

## Focused runtime validation

Each platform exposes `:runGameTestServer`, containing the same shared Soul Bolt scenarios with platform-specific test discovery and fake-player setup. These run with Ars, Goety and Core, without Iron:

```powershell
./gradlew.bat :runGameTestServer --no-daemon --max-workers=2
cd forge
./gradlew.bat :runGameTestServer --no-daemon --max-workers=2
```

Coverage: actual native hit damage and Ars continuation; no duplicated carriers; real player/totem storage, reduced scatter, creative bypass; Ars mana alternative and failed-action billing; repeated carrier payments and cleanup; native soul-loss event pricing; tick-by-tick launch, acceleration, displacement and gradual rotation against a bolt spawned by Goety's actual `SpellResult`, including accelerated travel. Headless tests check entity rotation, but do not inspect client rendering or animations.

For the separate NeoForge coexistence test, place matching Iron & Nouveau, Iron's Spells 'n Spellbooks, irons_lib, Player Animator and Caelus JARs in the ignored `libs/coexistence/` directory. Run from the project root:

```powershell
./gradlew.bat :runGameTestServer -PwithIron=true --no-daemon --max-workers=2
```

This selects the `goety_nouveau_coexistence` namespace and checks real Goety and Iron glyph registration, learning, native projectile dispatch and rejection of two replacement glyphs on a single projectile. The verified versions are Goety & Nouveau 0.1.0-dev.3, Iron & Nouveau 0.17.5, Core 0.1.6, Iron's Spells 'n Spellbooks 3.16.3, irons_lib 2.2.0, Player Animator 2.0.4 and Caelus 7.0.1. These are optional development runtime dependencies, never bundled or required by this addon.

## Version control

The initial version is `0.1.0-dev.1`. Dependencies, build scripts and shared code are committed; downloaded JARs, upstream audit checkouts and runtime directories are ignored. No remote publishing is part of the initial preparation.
