# Changelog

## 0.1.0-dev.44

- Add End Walk on both loaders: 118 shared glyphs, 119 on NeoForge. Execute native EndWalkSpell to apply native Shadow Walk, initialize the real recipient’s native return position/dimension, and send native particles/sound packets. Native potion and interaction handlers own expiry, removal, concealment, attack/projectile interruption and return.
- Restrict the effect to yourself or an allied player, with source equipment/resources separate from the recipient. Fold native Potency and Duration enchantment rules once, then apply Ars potency and twenty-tick duration units. Reject invalid/nonpositive/overflow duration.
- Preserve existing return anchors/effects rather than replacing them on repeated casts. Successful application pays once; creative is free. If the actual effect is rejected, remove the native callback’s otherwise orphaned anchor and leave payment uncommitted.
- Four focused runtime cases passed per loader, including native effect/target concealment, actual return callbacks/removal/expiry/projectile interruption, source enchantments/allied anchors, resource/recipient/creative/denied-effect gates and unscoped native behavior. Fake-player teleport probes verify callback destinations; real client movement/visuals remain manual checks. Five registered NeoForge focus keys remain without glyph entry points.

## 0.1.0-dev.43

- Add Grapple on both loaders: 117 shared glyphs, 118 on NeoForge. Execute the actual native GrappleSpell/VineHook and player-state pulling, preserving hook collision, attachment, rope renderer, sounds and original-caster staff/Velocity behavior.
- Restrict the actual hooked player to self or an ally. Source resources and native equipment remain separate from the player being pulled; quote the target player’s hook state rather than an unrelated source hook. Launch pays once, recall is free, and creative is free; no continuous resource drain is invented.
- Use a finite non-exclusive Core execution lease. Expiry, alliance loss or replacement ends the managed hook. Do not take over an ordinary wand hook or another caster’s hook, and do not clear a newer hook when the old one closes. Apply the native focus cooldown on managed completion rather than the launch callback’s zero cooldown.
- Synchronize the Ars casting hand for the native rope renderer without requiring a Goety wand to stay held. Four focused runtime cases passed per loader; actual client rope appearance and cooldown HUD remain manual checks. Six registered NeoForge focus keys remain without glyph entry points.

## 0.1.0-dev.42

- Add Tunnel on both loaders: 116 shared glyphs, 117 on NeoForge. Execute the complete native block callback at the actual Ars block/face, preserving native depth propagation, Void-staff width, block-entity/blacklist/fluid/unbreakable exclusions, particles and original block-state restoration.
- Pay only after successful native initial-hole creation. Rejected entity targets, invalid lifetime and insufficient resources produce no holes; creative is free. Ars time modifies the native hole lifetime additively by twenty ticks per unit.
- Mark addon-created native holes and propagate that marker through native asynchronous expansion. Reject unloaded/out-of-build-boundary creation and reads at the addon boundary without changing ordinary native holes.
- Preserve int depth within addon casting rather than the upstream callback's accidental byte narrowing. Preserve DOWN-facing propagation after save/load. For addon holes only, repair NeoForge's registry-aware save/update-tag path so native life/count/face data is actually persisted and synchronized.
- Four focused runtime cases and one large-range/native-isolation edge case passed per loader. Native client visuals remain manual checks. Seven registered NeoForge focus keys remain without glyph entry points; menu, two-interaction and teleport behavior require separate designs.

## 0.1.0-dev.41

- Add Pulverize and Scatter on both loaders: 115 shared glyphs, 116 on NeoForge. Pulverize uses actual native recipes/block processing/drop counts; native staff/AOE geometry uses the Ars-selected face rather than another caster ray trace.
- Ordinary Scatter deploys three real native mines around the Ars point and incoming horizontal heading. Geomancy staff runs native timed deployment around a live self/allied executor, preserving focus stats, staff behavior, mine damage/detection/growth/events and both native expiry branches.
- Retain original-caster equipment, soul account and mine base-damage rules independently of explicit damage attribution. Persistent roles supplement native ally filtering; unmarked native mines retain their original behavior.
- Staff execution uses the original five-tick cadence and 21-emission window without duplicating that window as preparation. Bill the first successful native result and each subsequent group of twenty emissions; rejected recipients/resources produce no mines and creative is free.
- Four focused runtime cases passed per loader, including native recipes/item counts, hit-face geometry, real mine damage/expiry/save-load/native isolation, staff cadence/payment/expiry, and boundary/resource/alliance gates. Client animation/audio remain manual checks. Eight registered NeoForge focus keys remain without entry points.

## 0.1.0-dev.40

- Add Illuminate on both loaders: 113 shared glyphs, 114 on NeoForge. Run the native periodic dark-space scan/visibility/support/placement/Soul Light conversion with a live allied executor and original-caster focus statistics.
- Pay one native focus charge only for each real successful light placement/conversion. Empty searches remain active and unpaid, live movement updates the search, and lack of resources, alliance loss or bounded expiry stops it. Use existing Core sessions/resource transactions and native particles/sounds.
- Use native IlluminateChargeUp for preparation instead of the 72000-tick held-item duration. The existing finite channel fallback and Ars time augments determine execution length; native scan interval remains native. Reject zero scan period before its modulo callback can throw.
- Scoped native placement avoids unloaded/build-boundary reads. Apply the same eventual native support tests before expensive visibility rays; native wand calls outside the addon scope remain unchanged.
- Four focused server cases and one zero-period case passed per loader. Native client appearance/audio remain manual checks. Ten registered NeoForge focus keys remain without glyph entry points.

## 0.1.0-dev.39

- Add Water Whip on both loaders: 112 shared glyphs, 113 on NeoForge. Use actual native GulfTentacle entities rather than a visual-only whip or synthetic channel.
- Only yourself or allied living executors receive the attached strike. Native synchronized ownership follows the actual executor for live attachment/client facing; original caster provides staff/focus potency, range and payment; explicit damage ownership remains separate.
- Retain original spawn/range/reposition packets and animation, ticks 8–11 attack trace, drench damage, hit sounds and twenty-tick expiry. Native Abyss-staff success emits native extra tentacles with original buff, range, arm offset and non-recursive staff flag; source/damage roles are copied and persisted.
- Original source allies and explicit damage owner allies are protected. Lost alliance or removed/dead executors stop the strike; ordinary unmarked Goety tentacles retain native behavior. Failed resources/invalid recipients are uncharged; creative bypasses payment.
- Four focused server cases passed per loader, covering actual native damage/following/expiry, staff offspring and NBT, ally filters/interruption, native isolation, rejection/creative and native animation event. Client appearance/audio remain manual checks. Eleven registered NeoForge focus keys remain without glyph entry points.

## 0.1.0-dev.38

- Add Cyclone and Tidal on both loaders: 111 shared glyphs, 112 on NeoForge. Both are one-charge native moving area entities, rather than artificial held channels.
- Cyclone retains native motion/pull/damage/expiry, Wind staff/focus stats and Unholy fire variant. The Ars-selected target stays independent of explicit damage-owner AI; native retarget replacements retain original-caster provenance and velocity. Original gear determines hellfire even with a different damage owner.
- Tidal starts at the Ars-selected point along the incoming direction, retaining original wave motion, drench damage, slam/shrink lifecycle and native Abyss-staff triple spread. Its impact-origin lifetime retains ten native aftermath ticks plus native duration bonuses, scaled by Ars time.
- Supplement native damage-owner ally checks with original-caster ally checks. Native wand entities are unchanged. Zero duration, invalid targets and failed resource charges create no areas; creative bypasses payment.
- Four focused server cases passed on each loader, including real native replacement/save-load, explicit ownership, original fire gear, wave damage/movement/expiry, staff spread and resource gates. Actual client appearance/audio remain manual checks. The native focus audit now has twelve pending registered keys.

## 0.1.0-dev.37

- Add Telekinesis and Burrowing on both loaders: 109 shared glyphs, 110 on NeoForge.
- Each Telekinesis channel owns a fresh native spell instance. Preserve native grabbing/force, immunity and health checks, hurt/range cancellation, collision/choke damage and portal particles; original caster supplies gear/payment and allied executor supplies geometry. Explicit damage ownership and configurable release cooldown remain intact.
- Burrowing accepts allied players and executes complete native mining, including permissions/events, cracks, drops/XP, native enchantments and staff/Burning branches. Exclusive ownership protects native mining state; cleanup distinguishes switching to another wand spell from native Burrowing taking over.
- Both use the existing bounded duration for native unlimited held casts and grouped upkeep/creative bypass. No target, rejected executors, permissions or resource failure do not activate paid mining/grabs.
- Six focused runtime cases plus one native-wand handoff case passed per loader. Add an actual ModItems binding audit: fourteen registered NeoForge focus keys remain without glyph entry points; this is not a full-catalogue completion claim.

## 0.1.0-dev.36

- Add Flying, Whirlwind and Shocking on both loaders: 107 shared glyphs, 108 on NeoForge.
- Use original wind movement/cloud particles and wind rings/push/extinguishing/arrow reflection. Actual allied executor provides live facing and position; original caster supplies gear, potency and resources. Flying retains each loader's native base power; falling Whirlwind retains its native lift override.
- Shocking keeps native lightning packets, actual damage, rod diversion, status/Burning and staff/weather branches. Spatial targeting/sounds follow the allied executor; explicit damage ownership remains separate from original-caster equipment/alliance.
- Sustained callbacks pay first on activation and then per twenty results. Creative is free; empty accounts, lost alliance, removal and finite expiry stop new results. Native unlimited flight uses the existing configurable bounded fallback. Ordinary Goety wand casting remains unchanged.
- Five focused server cases passed on each loader, covering real movement/damage, live facing, original staff variants, upkeep, interruption and native rod diversion. Client visuals/audio and native wand-only arm poses remain manual checks/boundaries.

## 0.1.0-dev.35

- Add Void Shock, Quaking and Warding on both loaders: 104 shared glyphs, 105 on NeoForge.
- Void Shock executes the native Stellar Focus useSpell barrage, preserving hover, homing/trails, actual explosions, explicit owner and two/five-tick staff cadence. Yourself or an ally supplies the executor; original caster supplies gear/payment. Upkeep uses the shared per-20-tick sustained mapping.
- Quaking executes original forward lanes, sneaking rings and Geomancy-staff arcs, damage/shield breaking, debris and sounds. Original-caster equipment and sneaking remain separate from the allied executor. Bound the native downward ring search at build/unloaded boundaries without creating fake bedrock or forcing unloaded chunks.
- Warding exposes the native unregistered utility spell using this addon glyph as its casting key and the existing charm asset as its icon. Only allied players receive native ward storage (cap 20, five-recipient-tick cadence); cost remains zero. Native mob ward storage is not invented.
- Separate preparation from actual attack phases: ten ticks for the void barrage and native warmup for Quaking. Warding uses the configurable finite duration fallback. Ordinary Goety item casting stays unchanged.
- Six focused server cases passed on each loader, covering damage/ownership, native cadence and variants, interruption/creative/resource gates, void-boundary termination and actual player ward data. Actual client appearance/audio remain manual checks.

## 0.1.0-dev.34

- Add Water Jet and Prisma Beam on both loaders: 101 shared glyphs, 102 on NeoForge.
- Water Jet retains its native trace, damage, extinguishing, Burning branch, Abyss-staff Sapped, loop sound and paid upkeep. Yourself or a native ally supplies the live spatial executor; original caster supplies equipment and payment, with explicit damage ownership retained.
- Prisma Beam keeps native target-dependent charging, bubble particles and final magic/attack callbacks. Charge setup is free; the successful final callback pays once. No duplicate pre-hit preparation; Ars duration scales the charge without pushing native particle spacing beyond its safe range.
- Add a transient tracked visual entity for both beams, reusing native water draw routines/textures and prism vertices/charge animation. Real player/mob executors render from their eyes without faking held-wand player capabilities. Friendly targets, lost alliance, removal, empty accounts and expiry clean up owned state. Ordinary Goety wand behavior remains unchanged.
- Five focused server cases passed on each loader, including a corrupted-beam regression; actual client appearance, audio and multiplayer interpolation remain manual checks.

## 0.1.0-dev.33

- Add Corrupted Beam on both loaders: 99 shared glyphs, 100 on NeoForge.
- Use the actual native beam entity, trace, blocking, damage and renderer. An allied executor supplies spatial ownership while the original caster supplies potency/payment and explicit damage ownership is retained. Both host and original-caster allies are excluded.
- Bound the native infinite held channel to a configurable 200-tick fallback; positive native duration remains native. Pay upkeep per 20 pulses and resolve the first paid damage immediately, including when the first fee empties the account.
- Remove the beam on expiry, resource failure, lost alliance or either role's removal. Sync Ars provenance, retain native client host interpolation without held-wand aim packets, and prevent orphaned saved beams from surviving without a transient execution session.
- Extend the shared channel driver with failure, liveness and close callbacks; ordinary rains and breaths retain their existing lifecycle. Native Goety beams keep their wand requirement.
- Four focused server cases passed on each loader, including the previous meteor regression. Release/source artifacts verified; client appearance and audio remain manual checks.


## 0.1.0-dev.32

- Add Bombardment, Thunderstorm and Trident Storm effects on both loaders: 98 shared glyphs, 99 on NeoForge.
- Preserve native fireball/Unholy/staff salvos, hover motion, lightning trap variants and particles, and trident formations, delayed real strikes and expiry. Sustained executors must be yourself or an ally; original caster supplies equipment and payment, with explicit damage ownership retained.
- Bombardment keeps native emission cadence and pays per 20 salvos. Thunderstorm uses native per-tick callbacks with ongoing payment per 20 pulses; Ars preparation replaces its initial 20-tick warmup. Trident Storm is a single paid area effect.
- Sync and persist Ars provenance for native trident entities so they survive preactivation without a held Goety wand. Ordinary native wand traps retain their original cancellation rule.
- Four focused server cases passed on each loader, including the previous meteor cadence/resource regression. Actual client appearance and audio remain manual checks.


## 0.1.0-dev.31

- Add Arrow Rain, Blizzard and Meteor Shower effects, plus the Ice Storm projectile replacement on both loaders: 95 shared glyphs, 96 on NeoForge.
- Use native rain callbacks and entities, cloud particles, impact behavior, staff/enchantment variants and explicit damage ownership. Rains run from yourself or an allied executor; caster gear and source account stay on the original caster.
- Share the bounded channel driver with breaths. Preserve native rain cadence and emission limits; ever-charge prepayment repeats per 20 native emissions, not per 20 elapsed ticks. Empty accounts, changed alliances, removal and natural expiry stop the channel. First rain batch triggers immediately after Ars preparation.
- Preserve the native moving ice cloud's growth, area freezing, snow trail, staff size and lifetime, with Conflux carrier compatibility.
- Five focused server cases passed on each loader, including a breath regression; client appearance and audio remain manual checks.


## 0.1.0-dev.30

- Add Ministrous and Hogging summon effects on both loaders, and native Breeze on NeoForge: 91 shared glyphs, 92 on NeoForge.
- Retain actual GeoPower equipment variants, matching-staff batches, native owner quotas, recall and servant life. Breeze uses the existing native entity API and is not registered on Forge.
- Preserve Burning Hoglin flank placement, short life, staff windup, radius scaling, actual contact/expiry explosions and native summon debuff. Sneaking retains its original summon behavior rather than introducing recall.


- Four focused server cases passed on NeoForge, three on Forge, including a native-breath regression. Release/source artifacts and platform-specific resources verified; client visuals remain manual checks.

## 0.1.0-dev.29

- Add Fire Breath, Frost Breath and Swarm as sustained effect glyphs: 89 glyphs covering 85 native focuses.
- Retain native per-tick results, breath particle helpers, equipment variants, damage types, freezing, fire and kill-produced swarms; bill the native focus once per second while active.
- Select only the caster or a native Goety ally as executor. Track that entity's eyes/facing, preserve original stats/payer and explicit damage ownership, and terminate on loss of alliance, resources or finite duration.
- Reuse Conflux execution sessions and Goety preparation movement configuration for channeling. Native Goety casts outside the scoped adapter remain unchanged.
- Two focused shared server cases passed on each loader. Client appearance, audio and input slowdown remain manual checks.

## 0.1.0-dev.28

- Add Watching, Snaring and Blasting summon effect glyphs on both loaders: 86 glyphs covering 82 native focuses.
- Reuse native ender servants, Void-staff batches, per-focus owner quotas, recall, lifetime, hiding, teleport behavior, particles and sounds.
- Preserve Snareling's native shot and Blastling's dedicated attack goal, including windup, alternating arms and post-attack state; no replacement combat AI is introduced.
- Three focused shared server cases passed on each loader, including a blaze summon regression. Client appearance and full combat navigation remain manual checks.

## 0.1.0-dev.27

- Add Blazing, Ghastly and Guardian effect glyphs on both loaders: 83 glyphs covering 79 native focuses.
- Retain Inferno and all three ghast equipment branches, native batch sizes, water/flying placement, fireball/explosion potency, owner quota, recall, expiry, particles and sounds.
- Preserve the native loader difference in additional ghast buffs; resources still commit only on successful summon and creative casts remain free.
- Four focused shared server cases passed on each loader, including an existing water-variant regression. Client visuals remain manual checks.

## 0.1.0-dev.19

- Apply native-style preparation movement with an independent switch, including Goety fullStopCast and no duplicate held-item slowdown.
- Creative bypasses native focus cooldown checks and writes by default; add an explicit opt-in.
- Keep pure Ars and native Goety casting unchanged; reuse preparation packets for HUD-independent movement state.

## 0.1.0-dev.18

- Register native preparation and focus cooldowns with Conflux 0.1.10. Mixed recipes prepare and release once.
- Independent chanting/cooldown switches default on; native effective times, soul HUD appearance and focus cooldown storage are reused.
- Record charging, continuous-use, native custom cooldown and summoning boundaries.

## 0.1.0-dev.17

- Add Thunderbolt, Hail and Monsoon effect glyphs on both loaders (48 glyphs: 17 projectile replacements, 27 effects, 4 native parameter augments).
- Thunderbolt retains its dedicated native thick-beam packet, shock damage, lightning-rod redirection, Spasms, weather bonuses, Storm-staff chains and Nameless-staff color. Targeting uses the Ars hit; rejected damage is uncharged.
- Hail/Monsoon use full native cloud entities: warmup, rain/snow, attacks, ally and overhead-obstruction rules, lifetime, Frost-staff tracking and native Monsoon lightning remain intact.
- Cloud time augments scale the native active tick lifetime; gear duration keeps its native multiplicative rule. Low-ceiling placement uses actual cloud dimensions and refuses obstructed or unloaded space.
- Native cloud upkeep does not consume additional glyph fees after a successful paid spawn. Creative remains free; effects retain their Ars category and provider grouping.

## 0.1.0-dev.16

- Add Eruption, Bio Mine and Teeth effect glyphs on both loaders, using the actual native entities and their complete Pyroclast/Acid Pool/Vicious Pike aftermath.
- Retain native arming, hover/fall, emergence/descent, expiry, ownership, ally rules, gear parameters and staff batches. One successful batch pays once; failed placement is uncharged.
- Sample mine locations around the Ars hit rather than an unrelated caster bounding box; place Teeth below a solid ceiling rather than embedded in it. No second view ray trace is used.
- Eruption supports Burning; Teeth has no invented duration or radius parameter. Zero-duration volcanoes are uncharged.
- Catalogue: 17 projectile replacements, 24 target/location effects, 4 parameter augments (45 glyphs).

## 0.1.0-dev.15

- Add Earth Fist, Ghost Fire, Leeching and Launch as Ars target/location effect glyphs, with native focus-model icons, both languages and existing Goety provider grouping.
- Preserve native delayed entities, animations, damage, upward lift, ghost-fire clusters, ally checks, expiry, leech particles/native healing events and recipient-facing launch motion.
- Keep Forge-specific Nameless ghost-fire towers and each version's native Launch power/enchantment scaling; equipment remains associated with the actual caster.
- Charge once on successful activation; reject invalid targets, blocked leech damage and exhausted resources, with creative exemption. Ars owns targeting and repeated trigger scheduling; native chanting/cooldowns remain outside this batch.
- Add two focused shared native-world cases for placement/damage/expiry/staff branches and entity healing/movement/resource behavior.

## 0.1.0-dev.14

- Allow book-only sneaking to select native blue Wither Skulls and non-griefing Lavaballs/Fireballs. Goety's native wand requirement no longer suppresses projectile alternate casts in Ars.
- Clarify that Ice Spike's spear variant requires Goety's Frost staff, avoiding confusion with Iron's ice staff.
- Add two focused shared checks on both loaders: actual Ars book alternate casts and main-hand book/offhand Goety Frost staff with native speed and two-target piercing.

## 0.1.0-dev.10

- Add Wither Skull projectile replacement with native flight, sneaking blue state, Nether-staff salvo, full Wither/kill-heal/explosion/loot behavior and Ars continuation. Native impacts remain complete on repeated Ars trajectories.
- Add Feast as an Ars-centered area effect, retaining original alliance checks, Ominous-staff fang rings, native warmup/damage/absorption/expiry and one successful batch charge.
- Extend Burning to Wither Skull and Feast, and Life Absorb to Feast; keep niche augment validation and trigger-only billing.
- Shorten the Burning description without a payload support list. Keep narrow-scope descriptions for dedicated augments.
- Add shared native-world checks for both loader builds, including real native explosions, delayed damage/healing, denied/creative casts and repeated-trigger resource exhaustion.

## 0.1.0-dev.9

- Sort projectile-replacement augments after the other augments within the Goety provider group; preserve categories and contiguous provider groups on both loaders.

- Add Fangs and Ignite effect glyphs with real Ars hit-point/direction adaptation, native entities and native block/entity ignition.
- Preserve grounded fang placement, warmup, player ownership, native ally filters, Ominous staff side rows/rings, animation and expiry. Native damage events handle lifesteal; native fang hits handle soul gains.
- Add Extension, Life Absorb and Soul Eater dedicated augments. Extend straight fang rows, enable native absorption or add native Soul Eater levels; validate per-payload support, binary absorption, ten-level soul eating and Absorb/Amplify exclusion in both the editor and server execution.
- Expand Burning to Fangs and Ignite; include all dedicated augment fees in trigger quotes and actual resource billing, with independent configuration keys and creative-mode exemption.
- Add native-world coverage for batch geometry, staff variants, delayed damage/healing/soul gains, invalid/no-ground/denied/creative triggers and fire-charge interactions on both loaders.

## 0.1.0-dev.8

- Keep native fireball ignition, smoke and water-projectile trails when Ars/Not Enough Glyphs owns the trajectory. Suppress only native movement and collision instead of cancelling the whole projectile tick.
- Apply the same fix to orbit, splash, trail and other special carriers on both loaders; ordinary native flight remains unchanged.

## 0.1.0-dev.7

- Add Fireball and Poison Dart projectile replacements, plus Shockwave and Cushion effects.
- Preserve native fireball flight, staff salvos, ignition, Unholy Hell Bolt/Hellfire, and the Forge-specific Malefic Helm geometry branch. One native salvo pays once; special Ars carriers move and trigger the native salvo as one batch.
- Preserve native quill gravity, rotation, water behavior, Wild staff piercing, robe-dependent Acid Venom, and one Ars continuation per distinct native hit. Repeated Ars trajectories retain control after block hits and pay at each successful activation.
- Add Burning as a compatible augment for Fireball and Haunted Skull. Each copy adds one native burning unit and a configurable trigger cost, default 5 selected-resource units before native soul discounts; creative casting remains free.
- Validate Burning against the actual payload through Core, retain Goety provider grouping, add Chinese/English descriptions and native model references, and synchronize new native projectile management flags and speed modifiers.
- Add native-world checks for default flight, full impacts, piercing, batch billing, denied/creative casts, native area entities, summon Burning and repeated-carrier resource exhaustion on both loaders.

## 0.1.0-dev.6

- Add native Magic Bolt as a projectile replacement, retaining launch/flight/turn, curse, lingering magic ground on entity/block impact and Ars continuation. Share the native bolt adapter without applying Soul Bolt staff variants or velocity enchantments to Magic Bolt.
- Add Iron Hide, Chill Hide, Weakening, Lightning and Updraft effect glyphs, with actual Ars targets, native effect/entity behavior, visuals, equipment bonuses and per-focus trigger billing.
- Preserve Frost staff group protection, both Weakening debuffs, lightning-rod attraction and Storm staff bypass, and Wind staff area bonuses. Keep native duration units explicit.
- Add glyph icons by referencing native focus models, Chinese/English descriptions, a per-spell behavior audit and a shared Conflux checklist covering client/server boundaries and both loaders.
- Add targeted native-world checks for full Magic Bolt flight/entity/block impacts, protection/ally filtering/billing, and actual lightning/updraft damage and lift.

## 0.1.0-dev.5

- Group Goety glyphs after ordinary Ars/addon glyphs within their original categories, using the new shared Conflux presentation registry. Preserve separate Iron groups and native Iron school ordering.
- Add Haunted Skull as an effect glyph: summon near the selected Ars entity/location, retain native ownership, pursuit, explosion and expiry, and support potency/duration/radius augments.
- Retain native necromancy-staff batch bonuses, enchantments, cape upgrades and shift recall. Charge one focus cost per successful batch; refuse empty recalls, exhausted summon limits, zero lifetime and failed spawns.
- Keep Forge/NeoForge native differences in owner resolution, nameless-staff count and duration enchantments; require Core 0.1.8.
- Add focused category/group and native summon billing/lifetime tests.

## 0.1.0-dev.4

- Add Ice Chunk, Frost Nova, Entangling and Soul Heal as Ars effect glyphs, with Chinese/English descriptions and native focus-model icons.
- Respect the actual Ars target/location, retain native ice/vine entity behavior, Goety explosion/alliance rules, healing/freezing visuals and applicable staff/robe/enchantment bonuses.
- Quote each focus independently and commit soul-energy or Ars-mana payment only when its trigger succeeds; invalid healing targets, fully healed groups and failed spawns are uncharged.
- Keep native Goety payment local to its execution so later foreign effects do not inherit a soul-energy account; verify mixed Iron/Goety continuations through Core.
- Share catalogue, parameter/target adaptation and trigger billing between both loaders. Require Core 0.1.7 and retain Iron-independent operation.
- Add shared native-world tests for selected-target ice impacts, vine control, block placement, ally filtering, healing and effect billing; retain original Soul Bolt regression coverage.
- Retain Polarice3's MIT notice for adapted original spell implementations and clarify the upstream port's separate license terms for additional code.

## 0.1.0-dev.3

- Restore native Goety launch offset, acceleration, gradual model rotation and collision behavior for ordinary Soul Bolt projectiles; special Ars carriers retain their shared trajectory.
- Preserve native velocity enchantments, synchronize motion modifiers and retain exact default speed independently of Ars shot inaccuracy.
- Restore Wither Bolt's native launch sounds and expose the off-world carrier through Core's carrier proxy contract.
- Keep native impacts, Ars continuation, soul billing and repeated-carrier cleanup; compare real Goety and integration motion tick by tick on both loaders.
- Require Core 0.1.6, whose projectile dispatcher ignores client spawn packets before reading a server-only caster context.

## 0.1.0-dev.2

- Add the Soul Bolt projectile replacement glyph, with native Goety projectile rendering and complete impacts followed by remaining Ars effects.
- Retain native staff-dependent Soul, Poison, Wither and Necro variants and potency bonuses; map Ars Amplify/Dampen to potency.
- Implement Goety soul storage/totem billing, native discounts and final soul-loss event pricing. Add functional Ars mana alternative and creative bypass.
- Use Conflux contexts, carrier dispatch, lifecycle cleanup, scatter budgets and repeated impact payments. Require Core 0.1.5; Iron is not required.
- Add Chinese/English descriptions, a creative glyph tab and a focus-model icon reference.
- Add shared behavioral GameTests for both loaders. Survival recipes, native chanting/cooldowns and further spells remain future work.
- Verify NeoForge coexistence with Iron & Nouveau through one shared Core; add an optional development-only runtime test.

## 0.1.0-dev.1

- Establish independent NeoForge 1.21.1 and Forge 1.20.1 projects, native focus/stats access, configuration, pinned dependencies and MIT licensing.
