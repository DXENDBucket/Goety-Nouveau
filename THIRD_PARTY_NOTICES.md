# Third-party notices

Goety & Nouveau uses the public APIs of Ars Nouveau, Ars Conflux and Goety. Upstream mod JARs and assets are not redistributed inside this addon. The adapters in `GoetyEffectCasting`, `GoetySimpleEffects`, `GoetySummonCasting`, `GoetyBoltCasting`, `GoetyNativeProjectileCasting`, `GoetyProjectileFactory`, `GoetyFangCasting` `GoetyCombatEffects` `GoetyLocationEffects` `GoetyHazardEffects` `GoetyWeatherEffects` `GoetyAreaEffects`, `GoetyNatureEffects` `GoetyServantCasting`, `GoetyUtilityEffects`, `GoetySpikeCasting`, `GoetyServantProfiles`, `GoetyHoggingCasting`, `GoetyBarricadeCasting`, `GoetyPersonalEffects` `GoetyBreathingCasting`, `GoetyRainCasting`, `GoetyCorruptedBeamCasting`, `GoetyWaterJetCasting`, `GoetyGroundChannelCasting`, `GoetyWindStormCasting`, `GoetyTelekinesisCasting`, `GoetyBurrowingCasting`, `NativeTelekinesisMixin`, `NativeBurrowingRecipientMixin`, `NativeBurrowingEnchantMixin`, `NativeWindRecipientMixin`, `NativeWhirlwindLiftMixin`, `NativeShockingGeometryMixin`, `NativeShockingDamageMixin`, `NativeQuakingMixin`, `NativeVoidShockGeometryMixin`, `NativeWardingRecipientMixin`, `GoetyEndWalkCasting`, `NativeEndWalkMixin`, `GoetyGrappleCasting`, `NativeGrappleSpellMixin`, `NativeGrappleLeaseMixin`, `NativeGrappleStateMixin`, `NativeGrappleEnchantMixin`, `NativeGrappleHandMixin`, `GoetyTunnelCasting`, `GoetyTunnelHoles`, `NativeTunnelSpellMixin`, `NativeTunnelHoleMixin`, `NativeTunnelSaveMixin`, `GoetyGeomancyCasting`, `NativePulverizeMixin`, `NativeSelectedBlockFaceMixin`, `NativeScatterSpellMixin`, `NativeScatterMineMixin`, `NativeScatterEnchantMixin`, `GoetyPrismaBeamCasting`, `ChannelBeamRenderer`, `NativePrismaGeometryMixin`, `NativeWaterDamageMixin`, `NativeBeamExecutionMixin`, `NativeCorruptedBeamDamageMixin`, `NativeBeamStateMixin`, `GoetyThunderstormCasting`, `GoetyTridentCasting`, `NativeSpellGeometryMixin`, `NativeBombardmentGeometryMixin`, `NativeThunderstormGeometryMixin`, `NativeTridentGeometryMixin`, `NativeTridentLifetimeMixin`, `NativeRainResultMixin`, `NativeBreathingGeometryMixin`, `NativeBreathingResultMixin`, `NativeBreathingDamageMixin` and `GoetyDashTask` adapt parts of Polarice3's original MIT-licensed spell implementations, including Frost Nova, Soul Heal, Haunted Skull, Iron Hide, Chill Hide, Weakening, Lightning, Updraft, Magic Bolt, Fireball, Poison Dart, Wither Skull, Feast, Bulwark, Discharge, Earth Fist, IceBouquet/Ghost Fire, Leeching, Launch, Eruption, BioMine, Teeth, Thunderbolt, Hail, Monsoon, Fire Blast, Sonic Boom, Flame Strike, Void Rift, Ice Spike, Lavaball, Steam, Bouncy Bubble, Electro Orb, Surging, Smack Stone, Blossom, Magma Bomb, Void Bomb, Sword, Razor Wind Wind Blast, Overgrowth, Glow Light/SoulLightSpell/SoulLight/LightProjectile flight and particles, Zombie, Skeleton, Vex, Leaping, Sensing, Rotation, Wind Horn, Charge, Spike, Barricade, Hunting, Mauling, Reaper, Wraith, Phantom, Slimy, Carrion, Whisper, IceGolem, Sprite, Vanguard, Blackguard, Blaze, Ghast, Guardian, Watchling, Snareling, Blastling, Ministrous, Hogging, ArrowRain, Blizzard, MeteorShower, Bombardment, Thunderstorm, TridentStorm/CastSpellTrap, CorruptedBeamSpell/AbstractBeam/CorruptedBeam, WaterJetSpell/WaterJetRenderer, VoidShockSpell/VoidShock, QuakingSpell, WardingSpell, FlyingSpell, WhirlwindSpell, ShockingSpell, TelekinesisSpell, BurrowingSpell, CycloneSpell/AbstractCyclone/Cyclone/FireTornado, TidalSpell/AbstractWave/TidalSurge, GulfTentacleSpell/GulfTentacle, EndWalkSpell/PotionEvents, GrappleSpell/VineHook/VineHookRenderer/SoulEnergyEvents, IlluminateSpell, TunnelSpell/HoleBlockEntity/SaveBlockEntity, PulverizeSpell/BlockFinder, ScatterSpell/ScatterMine, PrismaBeamSpell/PrismaBeamRenderer, IceStorm, FireBreath, FrostBreath, Swarm/IBreathingSpell, Killing, Bolting, Illusion and FangSpell/WandUtil geometry; their full license is retained below.

- Goety 1.20 source: https://github.com/Polarice3/Goety-2 — MIT, Polarice3 and contributors.
- Goety 1.21.1 source: https://github.com/Vivideru/Goety-3 — original `com/Polarice3` code/assets retain MIT; Vivideru-authored additions have separate reserved-rights terms. This addon references the dependency and does not redistribute those additions. The NeoForge-only Breeze glyph uses the native focus/entity APIs and shared servant lifecycle; its model references the native focus resource. No Breeze AI, model, texture or spell source is bundled.
- Ars Nouveau: https://github.com/baileyholl/Ars-Nouveau.
- Ars Conflux: MIT, EndXiom.

The included Gradle wrappers retain their original copyright headers and use Apache License 2.0. Its full text is included beside each wrapper JAR in `gradle/wrapper/LICENSE` and `forge/gradle/wrapper/LICENSE`. Glyph models reference Goety's native focus models by resource location; they do not copy the upstream models or textures. Any future copied or modified upstream assets must retain their required license notices.

## Original Goety implementation license

MIT License

Copyright (c) 2023 Polarice3

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
