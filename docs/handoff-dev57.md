# dev.57 handoff — native Goety growth and billing

Date: 2026-10-23. Scope: restore native attributes in manual adapters and the native enchanted-focus fee; keep existing accounts and callback rules.

## Workspace and compatibility

- Worktree: `C:/Users/57991/Documents/MinecraftDev/parallel-workspaces/goety-balance-dev57-20261023/Goety-Nouveau`
- Branch: `fix/native-growth-and-billing`; base `0f85fcb6999d217030f95aa8e696ff692d934fb2`.
- Fixed adjacent Core: `ed44ebb86bd087f3546a1bc6a315d2692f70262c` (0.1.28). Its tracked-file status is clean. No Core change or dependency-range increase.
- Current runtime verification: MC 1.21.1 / NeoForge 21.1.250 / Goety 3.2.1 / Java 21; MC 1.20.1 / Forge 47.4.0 / Goety 2.5.59.2 / Java 17.
- Supported older releases Neo 3.2.0 and Forge 2.5.58.4 were inspected as real JARs for relevant signatures and behavior. They were **not runtime-tested in this task**.

## Final behavior

`NativeSpellStats.forCaster` and manual projectile adapters now use native `WandUtil.getStats`. Native attributes, focus enchantments and Ars augments are applied once at their respective boundaries. Existing native callback paths remain responsible for their own baseline/enchantments. Flying follows the loaded native version's additive or replacement rule; its movement still uses native integer division. Spike's replacement rule remains unchanged.

Billing: native focus cost ×2 when the actual equipped focus is enchanted and native `enchantMultiCost` is enabled; then addon flat augment charges; then the existing Soul discount/truncation and activation event. ARS_MANA retains its old absence of Soul armor discounts/events. Failed/exceptional activation spends nothing; creative remains free. Detailed decision and native source evidence: [native-stats-and-billing.md](native-stats-and-billing.md).

The old EndWalk fixture now quotes through the shared account path instead of assuming the pre-fix unmultiplied fee. Static search of remaining direct native-fee assertions found no other outdated hand-computed payment assertion; the full historical test suite was not rerun.

## Verification performed

All logs below are in the worktree's parent directory. No optional Iron/NEG/client coexistence matrix was started.

| Platform | Actual checks | Evidence |
|---|---|---|
| Neo / Goety 3.2.1 | Five growth/billing GameTests all passed; compile + binary/source JAR tasks passed | `dev57-neoforge-final.log` |
| Neo / Goety 3.2.1 | New version-sensitive Flying comparison alone passed (1/1); JAR tasks succeeded | `dev57-neoforge-flying-final.log` |
| Forge / Goety 2.5.59.2 | Five growth/billing cases all passed in the six-case run; that run failed only the then-incorrect Flying test expectation | `dev57-forge-tests-2.log` |
| Forge / Goety 2.5.59.2 | Corrected Flying comparison alone passed (1/1); `jar`, `reobfJar`, `sourcesJar` succeeded | `dev57-forge-flying-final.log` |

Thus all six distinct focused cases passed on both current native versions, across the recorded runs; this was not a single clean six-case final run on Forge. The scope covers actual Soul Bolt, Fireball, Ice Spike and Magic Bolt branches; Void Rift; native Fire Breath and Flying callbacks; native focus lookup/config multiplier; armor discount/rounding; failed/exceptional actions; insufficient resources; ARS_MANA; creative bypass. Two explicit helper assertions also retain the supported older Flying replacement rule, alongside release-JAR inspection.

Commands, run serially with the appropriate `JAVA_HOME`:

```powershell
# Neo, worktree root: first five tests, then only newly added Flying
./gradlew.bat :runGameTestServer :jar :sourcesJar -PgameTestNamespaces=goety_nouveau_native_growth --offline --console=plain
./gradlew.bat :runGameTestServer :jar :sourcesJar -PgameTestNamespaces=goety_nouveau_native_flying --offline --console=plain

# Forge, forge/: five cases passed in the recorded earlier run; final focused check + release packaging
./gradlew.bat :runGameTestServer :jar :reobfJar :sourcesJar -PgameTestNamespaces=goety_nouveau_native_flying --offline --console=plain
```

Earlier fixture issues are retained in the logs rather than hidden: initial test-source compilation errors; the Neo creative assertion reused an account quoted before switching game mode (fixed by obtaining a new quote, no production creative change); the first Forge native config-disable assertion failed but did not recur on two subsequent executions, with no production change (root cause unconfirmed, not attributed conclusively to a watcher); Flying's first expectation missed native `potency / 2` integer division; its separate-namespace template initially used an invalid Forge cross-namespace name. The current fixture uses a local empty template and two Amplifies for an exact native +1 velocity comparison.

Binary/source ZIP checks: license and notices present; no native Goety classes/assets/data, dependency JARs or GameTest classes/templates bundled. `git diff --check` passed. This task made no client-rendering changes; client visuals, full historical suite and old-native-version runtime behavior remain untested. No shared-instance deployment was performed. No task GameTest/client process remains. The Forge Gradle daemon started by this task (PID 25080, first build at 03:23 in this worktree) was stopped after packaging; the pre-existing Neo Gradle daemon was left for the coordinator.

## Artifacts

Version: `0.1.0-dev.57` on both loaders. Paths relative to the worktree above:

| Artifact | SHA-256 |
|---|---|
| `build/libs/goety-nouveau-0.1.0-dev.57-mc1.21.1-neoforge.jar` | `8e0605c41de714f0ed8f94d33697eb08bba8451bb149e60c7de115709b71b37c` |
| `build/libs/goety-nouveau-0.1.0-dev.57-sources.jar` | `3fe53f05160ab61a570777560efab56ddb1b22c3cc8c55d1e7cbc8866eb491e3` |
| `forge/build/libs/goety-nouveau-0.1.0-dev.57-mc1.20.1-forge.jar` | `8a7641cc99dbbee0e27bbd79e879fc5ec69ae04c3cc0c8adfcb55c89e4eecae6` |
| `forge/build/libs/goety-nouveau-0.1.0-dev.57-sources.jar` | `6cd9f114e5515bb651b98723aff59d9e32024133d3ddffd602f443bbd94cd71d` |

Native inputs inspected:

| Version | SHA-256 |
|---|---|
| Neo 3.2.1 | `71026c15919792ac9e7a89cedbbfc408386e62f138857c914818ba338c5cc153` |
| Neo 3.2.0 | `edeeb623f626bc85bd26df6458dbb440fd4b044f34f3de78ee715cbc01e692d8` |
| Forge 2.5.59.2 | `d8136cfb7ef70736c7125f9837b1d5938225bd231513a2332f92655216e40e89` |
| Forge 2.5.58.4 | `b97f9a5928b5b3665cc7de88d5a1b52a4b07b6ed260f64ef9584b0c4603bee52` |

Independent static review evidence is in `%TEMP%/goety-stats-review-20261023/review.md`; additional four-JAR API/Flying evidence is in `%TEMP%/goety-api-check-20261023/`. These local audit materials are not bundled.

Coordinator may merge the task commit and deploy the verified binary. Do not repeat passed checks unless integration adds new changes or a concrete new concern. No push or CurseForge publication was performed or authorized by this subtask.
