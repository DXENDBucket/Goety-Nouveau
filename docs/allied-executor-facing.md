# Allied directional focus execution (0.1.9)

## Cause

The user clarified that the affected casts are Goety focuses executed by allied mobs through Ars, especially breaths. The previously investigated Inferno HellBolt is a different path; its speculative orientation synchronization was removed and stays removed.

The adapter substituted the executor but inherited inconsistent native direction reads:

- `IBreathingSpell.breathAttack` / `dragonBreathAttack` emit moving particles from `getLookAngle()`.
- Minecraft's `Entity.getLookAngle()` uses `getXRot()` / `getYRot()`, i.e. body yaw.
- Goety `MobUtil.getTargets` and `ISpell.entityResult` use `getViewVector(1)`. LivingEntity supplies head yaw through `getViewYRot(1)`.
- `ISpell.blockResult` instead reads body yaw, so block and entity ray tests can diverge too.
- Native `AbstractBeam.updatePositionAndRotation` copies owner body yaw, although its muzzle offset uses the owner's head view.

A mob can have body yaw 0 and head yaw 90: the former points along +Z, the latter along -X. Players usually have matching values, so player-only checks hide the discrepancy. The affected particle velocity really points away from the mob's head; it is not merely a model-rendering problem. Breath damage targeting may already point along the head, producing a visual/hit mismatch.

## Change

`GoetyExecutorFacing` resolves the real executor only inside the existing native/breath callback scope, then reads its live `getViewVector(1)` / `getViewYRot(1)`. Unscoped calls keep the original native accessors. No entity rotation is changed, no new network synchronization is added, and caster/owner/equipment/resource roles are retained.

- Scoped breath particles and targeting use that same direction each pulse. Native particle types, mouth offset, random spread, count, speed and emission packets are preserved. The two small native particle geometries are adapted in `GoetyBreathVisuals` with the Goety MIT notice; ordinary native focus use delegates unchanged.
- Scoped native block traces use executor eyes and view, retaining native OUTLINE/Fluid.NONE clipping. Native entity traces already use the head view.
- Scoped horizontal native offsets use executor head yaw while retaining their original left/right/forward calculations.
- Only Ars-triggered native corruption beams copy their owner's head view into the existing beam rotation, on both sides; the owner is not turned and original native beams are unchanged.

The rendering code and native focus effect/damage lifecycle remain native. No Goety Interface injection is introduced: Forge's Mixin 0.8.5 does not support injector methods in interface mixins. Existing class adapters provide the default-method override/delegation instead.

## Validation boundary

This batch is build-only at the user's request. Compile/package both NeoForge and Forge; verify the Forge refmap and registered class Mixin, absence of the removed Inferno helper and temporary interface injectors, native signatures and third-party notices. Do not run GameTest, launch a client/server or replace the shared pack.

Runtime/client confirmation remains pending. A later focused check should use a mob whose body and head differ by roughly 90 degrees, with the original player facing elsewhere; compare fire/frost/swarm particles and hits, rotate the head during a cast, and check block-vs-entity rays and a corruption beam. Also compare ordinary native wand casting outside the adapter scope.
