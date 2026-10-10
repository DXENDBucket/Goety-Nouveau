# Building

## Layout

```text
MinecraftDev/
  Ars-Conflux/              # compatible Core checkout, currently 0.1.3
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

Prepare an Ars Conflux 0.1.3 source checkout beside this project, then run:

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

Use a dedicated Ars integration instance. Its local path can be recorded in the ignored `test-environment.local.json`; personal instance paths are not part of the public source. The foundation build is not automatically installed into a game instance. Compilation alone does not verify mod loading, rendering or playable spell behavior.

## Version control

The initial version is `0.1.0-dev.1`. Dependencies, build scripts and shared code are committed; downloaded JARs, upstream audit checkouts and runtime directories are ignored. No remote publishing is part of the initial preparation.
