# Killing, Bolting and Illusion audit

Added in `0.1.0-dev.26`, against pinned Goety 3.2.0 / 2.5.58.4. Three native focuses become effects after an Ars cast method. The catalogue has 80 glyphs: 18 projectile replacements, 58 effects and four native parameter augments, covering 76 native focuses. Core, survival acquisition and existing sorting are unchanged.

## Killing

An actual living Ars-selected enemy is required; self, allies, creative/spectator recipients and block hits fail without feedback or payment. The adapter bypasses the native caster-view condition and Forge's mutable lockedTarget field: an already-issued Ars carrier can hit independently of the caster's current view, and shared spell instances must not retain another cast's target.

Target health determines damage. The configured native feedback fraction determines caster damage. If native calculated feedback would kill a mortal caster, both amounts become caster health minus one, preserving the original low-health restriction. Native death-curse damage and damage events remain authoritative; this is not a direct health assignment or guaranteed execution. Creative retains the native feedback-immunity bypass and free resources.

Resources are checked before either health cost. If caster damage is rejected in survival, target damage does not run and no charge is committed. If actual feedback succeeds but another mod cancels target damage, the activation still pays: health has already changed. Successful target damage retains eight gathering trails, native wind particles, thunderbolt packet and both sounds. Packets are limited to nearby observers in the same dimension.

Feedback is borne by the actual caster. Target damage uses the explicit Conflux damage owner; selecting an entity does not make it the caster or payer. Potency, duration and AOE augments are not offered because the native focus does not consume those parameters. Native range enchantment selects a target in the original spell; Ars supplies targeting here, so it is not exposed as a second reach increase.

The original held-cast useSpell can draw particles around a view-selected enemy before release. With an Ars projectile the actual target may not exist until impact, so this batch preserves the impact particles rather than inventing a different preselected target during shared chanting.

## Bolting

The actual caster moves toward the selected entity/location. Self hits use the caster's facing; a coincident point falls back to facing. The selected enemy is not moved. Native base speeds (ordinary 2.5 / Storm staff 3.5) and velocity enchantment contribution remain, scaled by Ars Accelerate/Decelerate using the existing integration speed convention. Amplify/Dampen modifies native dash potency.

`GoetyDashTask` extends the public native BoltingDashTask. Start, termination, collision/death checks, spin texture and Forge's native spin flag remain inherited. The attack tick is adapted from the original MIT task to preserve explicit damage ownership independently of the moving caster. It retains native nearby enemy filtering, shock damage, invulnerability/fall handling, electrical particles, Spasms chance, weather and staff chaining. The original counter behavior is retained rather than introducing a second duration timer. A successful dash pays once; its already-paid native attack ticks do not charge again.

## Illusion

Four native Doppelgangers appear around the Ars selection; Nameless staff creates eight. They remain the caster's doubles, with native owner UUID/client ID, equipment copy, bow attacks, target diversion, hit dismissal and expiry. Selecting an enemy additionally aims the doubles at it. Ordinary doubles have the native 1200-tick life. Nameless crown/cape plus staff select the native short-lived undead clones, staff weapons, target-centered four-block ring and NecroBolt/hasShot behavior. Illusion robe retains upgraded doubles, caster invisibility and blindness on the actual selected enemy.

Recasts dismiss owned doubles older than ten ticks. Fresh doubles retain the original grace period. Book-only sneak casts dismiss eligible old doubles without spawning another batch; empty dismiss does not charge. A replacement batch must successfully add at least one clone before old doubles are removed. Native particles, colored Lich modes, shockwaves and sounds are retained. No summon quota, SummonDown, upkeep or generic teleport recall is invented for this ordinary Spell.

The native SSetPlayerOwnerPacket assigns the receiving local player as owner. It is therefore sent only to the clone's actual player owner, through the existing StartTracking hook after spawn/metadata packets. Other observers receive the native synchronized owner UUID/client ID; the packet is never broadcast to them. Native Doppelganger renderers continue to resolve skin/cape information from that UUID. The marker scopes this extra packet to addon-created clones and persists with them.

## Verification boundaries

Focused `goety_nouveau_personal` cases exercise actual native damage/feedback events, low-health limits, immune feedback/target handling, payment exhaustion and creative behavior; actual native clone recasts, synchronized metadata, equipment branches, kill/expiry and NecroBolt firing; and real server ticks through the native TimedEvents queue for dash attacks and cleanup. An existing EarthFist tracking case covers the shared observer hook. All five cases passed on both NeoForge 1.21.1 (with Iron/NEG coexistence) and Forge 1.20.1 (standalone). Logs: `build/personal-spells-verified.log` and `forge/build/personal-spells-verified.log`. No historical catalogue rerun or client launch was performed.

Killing needs a genuinely vulnerable player fixture: native fake players permanently reject damage, actual ServerPlayer spawn protection must elapse, and native self-attributed death curse requires PVP in the test server. The creative billing check uses a separate damage owner so native kill soul rewards are not mistaken for spell payments. These fixture corrections do not change production damage, immunity, PVP or reward behavior.

Actual skin/cape rendering, multiplayer packet ordering on a running client, dash animation and particle appearance remain manual checks. Metadata replicas in GameTest are not claimed as client rendering tests. All source is shared between loaders except the native dependency's inherited behavior; no Iron dependency or provider-specific Core logic is added.
