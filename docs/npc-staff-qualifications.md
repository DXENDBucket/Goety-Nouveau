# NPC staff qualifications — 0.1.11

`StaffQualifications` supplements Goety staff qualification checks for explicitly registered non-player casters. It is independent of actual Curios equipment: robes remain real items, while a creature can innately qualify for several staff types without equipping several staffs.

Register during common setup, using a stable identifier and a side-effect-free lookup:

```java
StaffQualifications.register(myId, actor -> isMyCaster(actor)
    ? StaffQualifications.Qualification.ALL
    : StaffQualifications.Qualification.NONE);
```

Selective grants are available through `Qualification.schools(SpellType.NETHER, SpellType.FROST)` or `Qualification.items(staffIdA, staffIdB)`. The public record constructor combines both. Item grants also supply the real staff's native schools. Multiple providers add their grants; duplicate provider IDs are rejected. Player calls always remain native.

## Covered native checks

- `Spell.rightStaff` and `Spell.typeStaff` read the extra school qualifications.
- Direct `staff.is(ModItems.NAMELESS_STAFF.get())` / ominous-staff and equivalent item checks read explicitly granted staff identities. `ALL` includes all registered `IWand` items, including addon staffs.
- `WandUtil.findWand` returns an ephemeral qualified copy for registered NPCs. A caster with no physical wand receives a transient native dark-wand casting argument. Its actual hand and inventory are unchanged.
- The qualification survives `ItemStack.copy`, including the existing native-focus casting scope. It is not a saved component or transferable inventory item; later calls resolve it from the caster again. Unrelated item stacks take only a null branch in the identity hook and retain vanilla behavior.

`ALL` means all qualifications, not repeated execution of every mutually exclusive branch. Native `if` / `else if` ordering remains authoritative; e.g. a summon with several eligible variants still chooses one. Native `instanceof` checks on a particular wand implementation, custom overrides that bypass Goety's base `Spell`, tag checks, and bespoke addon callbacks are not automatically rewritten. No generic claim of every possible addon staff behavior is made.

The API does not grant staff attribute modifiers, an enchanted focus, set bonuses, extra soul energy, unlimited summons, free casting or cooldown bypasses. Costs, timing and summon caps continue through their existing paths; natural effects of a native staff qualification are retained.

## Validation boundary

Both loader builds and packaged mixin/refmap checks are recorded in `npc-staff-qualifications-build.json`. Native staff predicate targets are checked against released dependency classes. This batch does not launch Minecraft, a dedicated server or GameTest; in-game behavior remains unverified.
