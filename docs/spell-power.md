# Goety & Nouveau — percentage power

Core 0.1.42 owns the shared percentage formula and raw-native damage scope. Goety retains native potency, equipment, healing, timings and soul payment. Shared sessions and payloads retain schools; the separate Bolting task restores its captured profile. Killing feedback explicitly bypasses the damage bonus as a cost. Both loaders build from shared code. Attributes default to zero; schools are not preset.

General attribute: `ars_conflux:confluence_spell_power`; `0.2` is +20%, `-0.2` is -20%. General power also applies without school declarations. General and weighted school percentages add before the final nonnegative clamp. Optional school attributes are declared with `schools.power_attributes` in Core startup config, which requires matching client/server lists and a restart.

Verification and distribution hashes are recorded in Core’s `docs/spell-power-handoff-0.1.42.md` and the local release manifest. No new permanent progression or equipment is granted.
