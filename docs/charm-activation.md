# Activate Charm — 0.1.13

Optional Goety Charms integration, requiring its 0.2.2 public API. Ars Conflux baseline: `3ed7cd3209b8a538f4cfd25d3c18f34b13076f78` (0.1.47). No Conflux implementation changes are required.

## Player behavior

Activate Charm is a tier-one effect. `Projectile → Activate Charm` summons at the selected impact location or converts the selected living entity. It always uses the original real player caster's inventory and native ownership, never the selected entity's inventory or an allied executor's items. Non-player/fake-player casters do not activate it.

It selects one sealed summoning or conversion charm in this order:

1. Offhand.
2. Hotbar slots 0–8, left to right.
3. Backpack slots 9–35, row by row from left to right and top to bottom.

The cursor stack, armor, containers and nested storage are not searched. Blank charms, unsealed samples, teleport charms and location charms are skipped. A selected charm with an invalid current source or unsuitable target fails without trying a different one. Each actual successful effect invocation consumes one in survival; scatter/repeated triggers can release further charms. Creative retains the charm, matching direct item use.

The glyph costs zero additional Mana/soul energy and has no native focus preparation/cooldown. Materials, research, environment and ritual fee were already paid during sealing. Other Ars recipe parts keep their normal costs. No augments modify the stored ritual. Native summon caps, Apostle warning, conversion rules and completion remain in Goety Charms; the integration does not copy ritual execution or mint inventory items.

A block hit uses the adjacent hit face as the summon origin, matching direct charm targeting. A living entity hit supplies its block position and the actual selected entity. No new caster sight trace replaces the Ars hit. A miss does nothing.

## Acquisition and optional loading

Native Ars Scribe recipe: Blank Charm ×1 + Source Gem ×1 → Activate Charm glyph ×1, 27 experience points. Shared recipe generation emits the appropriate loader mod-presence condition, so absent Goety Charms does not leave unknown-item recipes. The glyph and its school registration are only constructed after a loader presence check. Its icon references the existing summoning charm texture and uses Conflux framing; no texture is copied.

The dependency is optional on both loaders, ordered after Goety Charms when present and requiring 0.2.2+. Only the isolated compatibility glyph references `dev.goetycharms` classes; the main catalogue calls its bridge only in the installed branch. The charm provider JAR is compile-only and is never embedded.

## Development and validation

Put a NeoForge 0.2.2+ Goety Charms release in `libs/optional/`; for Forge the pinned compile API is the reobfuscated 0.2.2 JAR at `forge/libs/optional/goety-charms-0.2.2-forge1201.jar`. These are ignored local dependencies. `-PwithCharms=true` adds the matching JAR to development runtime; default runtime excludes it. The Forge dependency uses native ForgeGradle remapping.

Five focused cases are provided in `goety_nouveau_charms`: optional registration, inventory ordering/kind filtering, wrong-target retention without fallback, actual Ars-hit summoning/creative behavior/no second soul fee, and selected-entity conversion. The four item cases require Charms to be present. Their source is compiled only for this batch; no client/server/GameTest is launched. Actual gameplay, coexistence loading and client icon presentation remain unverified.
