# Native Goety equipment checks for NPCs — 0.1.10

Goety's `CuriosFinder.findCurio` overloads accept `LivingEntity` but return empty for every non-player. Curios itself supports entity slots and non-player inventories on both supported loaders.

`NativeMobCuriosMixin` supplements both the item and predicate overloads for non-player wearers, using the real Curios inventory and `findFirstCurio`. Player calls continue through Goety unchanged. No virtual equipment, species list, duplicate inventory, manual item ticking or summon-specific bypass is introduced.

NPC content must assign slots through Curios entity data and equip real stacks. Curios handles storage, reloads, ticking, modifiers, synchronization and drops. NPCs without equipped matching items gain nothing. This also allows native summon upgrade decisions, batch counts and spawn geometry to use the same equipment result.

In native Goety, an unholy robe qualifies as a nether robe. Together with an unholy hat it satisfies the unholy-set check: Blazing can create Infernos and Ghastly can create Malghasts. A second robe is unnecessary. Other equipment effects follow their own native checks; this change does not make player-only mechanics universally support NPCs.

Verified against local released Goety 3.2.2 / Curios 9.5.1 (NeoForge) and the local Forge dependencies during compilation. Mixin targets and archive metadata are checked statically. No client, server or GameTest is launched for this batch; rendering and in-game inventory lifecycle remain unverified.
