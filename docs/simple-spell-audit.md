# 简单效果／原生弹射物接入记录

版本：`0.1.0-dev.7`，包含 dev.6 与 dev.7 两批记录。固定依赖：NeoForge Goety 3.2.0、Forge Goety 2.5.58.4。通用核对方法见 Ars Conflux 的 [`spell-adapter-checklist.md`](https://github.com/DXENDBucket/Ars-Conflux/blob/main/docs/spell-adapter-checklist.md)，端侧事故见 [`side-boundaries.md`](https://github.com/DXENDBucket/Ars-Conflux/blob/main/docs/side-boundaries.md)。

## 这次实现的范围

| 聚晶／魔符 | 原生行为及适配决定 | 易遗漏的细节 |
| --- | --- | --- |
| `magic_bolt_focus`／魔法箭 | 替换 Ars 载体，创建真实 `MagicBolt`。普通载体保留 Goety 飞行；特殊载体使用 Core 的 Ars 轨迹。 | 原生渐进转向、尾迹、声音、实体伤害与诅咒，以及实体／方块命中后的 `MagicGround`。命中后继续 Ars 一次。 |
| `iron_hide_focus`／铁皮 | 为 Ars 选中的自身／友军添加真实铁皮状态，增益检查针对接受者。 | 原生隐藏粒子但显示状态图标；一个时长单位是一分钟。不能因施法者已受保护就拒绝保护另一友军。 |
| `chilling_focus`／寒冰护体 | 为 Ars 选中的自身／友军添加原生状态；寒霜法杖以接受者为中心向四格内友军扩散。 | 一个时长单位为 45 秒，扩散按原施法者阵营判断，保留寒冰命中声音。 |
| `weakening_focus`／虚弱 | 命中点范围攻击。选中实体时使用实体包围盒，否则以实际位置为中心。 | 不只有虚弱：同时添加原生侵蚀（`SAPPED`），其放大等级为 potency＋4；保留冲击波、方形粒子和诅咒／吟唱声音，排除原施法者及其友军。 |
| `lightning_focus`／闪电 | 在 Ars 目标／方块创建 `SpellLightningBolt`，伤害、闪光、声音和方块／实体交互由原生实体执行。 | 原生 `BlockFinder.findLightningRod` 寻找并偏转到暴露的避雷针，风暴法杖取消偏转；不改用原生视线重新选人。 |
| `updraft_focus`／上升气流 | 在实体位置或方块上方创建原生 `UpdraftBlast`。 | 保留延迟伤害、抬升、气流粒子和原生声音；风之法杖额外＋0.5 范围。不要只加一个瞬时速度。 |

增益只接受自身、原生友军或原施法者拥有的生物，方块和敌对目标不收费。虚弱是范围释放，即使范围内没有敌人也存在有效的波纹施放，因此沿用一次成功施放的费用。每个聚晶有独立费用，报价无副作用，付款与伤害来源不会变成接受者。

魔法箭的强化／抑制对应威力，延长／缩短时效对应额外 duration。原生诅咒和魔法区域是 `100 × (额外 duration＋1)` tick；魔法箭自身仍保留五秒的原生寿命。延长时效不延长飞行寿命。

灵魂箭和魔法箭共用 `GoetyBoltCasting`，共享前提是它们都属于原生 `SpellHurtingProjectile`。魔法箭不继承灵魂箭的野性／下界／无名法杖变体，也不额外套用原生灵魂箭的速度附魔。普通 Ars 弹射物已经位于眼高减 0.1，原生箭出生于眼高减 0.2，因此转换只再下移 0.1，不能重复减去完整原生偏移。

## 适配边界

- Ars 负责位置、方向和选取距离。原生 range 附魔和法杖选取距离加成不再重新驱动一次 Goety 视线搜索；上升气流的风之法杖范围加成仍保留。
- 魔法箭的 NeoForge 原生依赖还声明了移植侧追踪附魔。原生实体及其依赖自身的钩子继续运行，本附属不复制移植者新增实现；特殊 Ars 轨迹仍以 Ars 运动为准。
- 这批不增加 Goety 原生吟唱与冷却，不改变生存获取流程。
- 原生实体负责自动发生的区域伤害、状态效果和自然结束；它们不是每 tick 再次执行魔符，所以不会逐 tick 重付魔符的触发费用。
- dev.6 时未加入火球、毒镖；dev.7 已通过分别管理原生实体家族接入，见下节。
- 连续光束／吐息、普通仆从、位移和复杂条件法术仍需逐项接入，不批量盲调结果方法。

## 客户端／服务端核对

`GoetyBoltCasting.replace` 先确认 `ServerLevel`，再读取解析器与施法者；效果入口同样先确认服务端。原生实体在客户端由 Goety 渲染和更新，Goety 提供者不会在客户端生成包中再创建载荷或支付资源。

管理状态、Ars 接管运动与速度通过原生实体同步数据传输；普通飞行不取消原生 tick，特殊飞行也保留原生子类的尾迹处理。客户端碰撞不进行服务端事务。

共享 `GoetyMovementMixin` 仍仅适用于 `WaterHurtingProjectile`／`SpellHurtingProjectile` 这一家族。以后接入不同实体，不应通过过宽的实体 Mixin 强行共用碰撞、同步或移除规则。

## 验证记录

共享的 `SimpleSpellCases` 检查真实原生世界行为：

- 魔法箭与原生 `SpellResult` 产生的箭逐 tick 对照出生位置、速度和转向；实体命中保留诅咒、区域及 Ars 接续，方块命中也生成区域；资源不足不能发射。
- 铁皮的接受者、隐藏粒子和分钟时长；寒冰护体的法杖友军扩散；虚弱的两种负面状态、友军排除和逐聚晶费用。
- 闪电真正伤害目标；上升气流在原生延迟后造成伤害并抬升目标，实体和方块位置正确。

服务端自动检查不代表客户端目视验证。粒子形状、尾迹完整性、模型动画、图标、多人同步，以及避雷针偏转的实机场景仍须在游戏内观察。每次发布把实际执行的构建和测试结果补进本节，不把计划用例写成已经通过。

2026-10-11 实际结果：

- NeoForge 1.21.1：`:jar :sourcesJar :runGameTestServer -PwithIron=true`，8 项共存测试通过。日志 `build/simple-spells-coexistence.log`；包含本批三项共享用例和原有五项共存／归属／召唤／排序检查。
- Forge 1.20.1：`:jar :sourcesJar :runGameTestServer`，无 Iron 环境下 15 项测试通过。日志 `forge/build/simple-spells-gametest.log`；本批三项与原有十二项一起执行。
- 两版本全部十二个魔符的原生聚晶模型引用都能在固定 Goety 依赖 JAR 中找到。
- 本批没有启动真实客户端，也没有重放客户端生成包；客户端／服务端边界为代码核对，服务端启动和行为为自动验证。

原生飞行对照首先发现了重复计算出生高度偏移，已修复；方块命中用例扩大挡板以容纳原版 Ars 发射误差，没有移除误差或改变散射方向。

## 0.1.0-dev.7：不同弹射物家族与燃烧增强

| 魔符 | 实现与边界 |
| --- | --- |
| 火球 | 普通 SmallFireball 飞行、点火、潜行不引燃方块、法袍伤害来源和 Unholy 套装的 HellBolt/Hellfire 保留。Nether 法杖三发一组收费；Forge 独有 Helm 分支按 Ars 发射参考点／方向计算，Neo 不引入不存在的分支。 |
| 毒镖 | 保留 Arrow 的重力、转向与水中阻力，原生 Wild 法杖穿透和 Wild 法袍 Acid Venom 分支继续由原生实体处理。同一个普通毒镖的已命中集合防止对同一目标重复执行 Ars 后续。 |
| 冲击波 | Ars 选定中心的原生 SpellExplosion、随机伤害、环／球粒子、灵魂粒子和声音；强化与扩散修改伤害及半径。 |
| 风垫 | 选中实体的位置或方块上方创建真实 Cushion，范围与寿命使用原生方法，移动、作用和自然结束仍由原生实体负责。 |
| 燃烧 | 同一编排段落内只接受火球或幽灵头骨。每个增加一个原生 burning 单位，费用纳入触发报价与实际激活，默认额外 5 单位资源，可配置；不是新追加一个 Ars 点燃效果。 |

新增 `GoetyNativeProjectileCasting` 负责这些不同继承链的实体及原生多发批次；已有 Soul/Magic 的适配不被强行改成 SmallFireball 或 Arrow。网络同步状态只添加到对应 Goety 实体。原生普通碰撞仍先遵循加载器的 ProjectileImpact 事件；虚拟命中完整调用原生实体／方块处理，再继续 Ars。客户端不查询施法者、不收费。特殊载体仅取消基类运动，保留原生子类动画／尾迹／生命周期。

特殊轨迹的法杖齐射明确按“一组共享 Ars 轨迹的载荷”处理，侧弹作为随动外观，各原生载荷在该轨迹命中时执行并分别接续 Ars；不声称有三个独立的原生轨迹。它每次齐射命中只付一次费用。特殊毒镖在每次载体触发时重置原生穿透历史，命中方块后清除原生地面锁定，允许 Ars 原有环绕、弹跳等轨迹继续工作。资源不足则关闭本次执行和全部随动实体。

共享 `ProjectileBatchCases` 检查默认火球与原生法术产生的实体逐 tick 对照、三发收费、燃烧与原生命中、无资源和创造模式、毒镖原生重力／穿透／毒性与接续去重、冲击波实际伤害、风垫原生范围／寿命、幽灵头骨燃烧与费用，以及重复载体耗尽后真正移除。

2026-10-11 dev.7 实际验证结果：

- NeoForge 1.21.1：`:jar :sourcesJar :runGameTestServer -PwithIron=true`，12 项共存检查通过，日志 `build/native-projectiles-coexistence.log`。
- Forge 1.20.1：`:jar :sourcesJar :runGameTestServer`，无 Iron 的 19 项检查通过，日志 `forge/build/native-projectiles-gametest.log`。
- 两端 17 个魔符模型、全部 JSON 与原生模型父引用核对通过；发布 JAR 含 MIT 和第三方声明，未打包 GameTest 类或上游依赖。

客户端真实粒子观感、第三方投射物事件取消、Curios 套装装备组合与多人同步仍不等同于这些服务端用例已经验证。本批没有启动真实客户端。

## dev.8 special-carrier native ticks

The previous special-carrier hooks cancelled `AbstractHurtingProjectile.tick` and `WaterHurtingProjectile.tick` completely. This lost the fireball self-burning flag, vanilla smoke/bubbles and WaterHurtingProjectile virtual `trailParticle` calls. Core correctly retained the Ars controller; the fault was in the Goety provider's native movement hook.

For vanilla hurting projectiles, keep the original tick and suppress only its hit query, velocity integration and server position write while Ars-driven. Client position prediction, native fire state, bubbles and virtual trail particle selection still run. Ordinary managed shots retain native speed scaling, and unrelated projectiles are untouched. WaterHurtingProjectile retains its complete tick; only `travel` and `hitDetection` are intercepted. Arrow-family movement remains separate.

The regression uses the actual optional NEG MissileProjectile when present, otherwise Ars Orbit. It checks the network-visible burning state, no duplicate movement/acceleration, actual NEG splash callbacks and native target damage/ignition, plus native fireball/Water trail calls. Client particle appearance and multiplayer interpolation still require visual checking.

2026-10-11 dev.8 verification: NeoForge 1.21.1 with Iron and Not Enough Glyphs passed all 13 required tests (`build/special-projectile-coexistence.log`). Forge 1.20.1 without Iron/NEG passed all 20 required tests, using Ars Orbit for the special-carrier regression (`forge/build/special-projectile-gametest.log`). Both release/source JARs built; no real client was launched.


## 0.1.0-dev.9: Fangs, Ignite and dedicated native augments

Fangs adapts the MIT upstream FangSpell/WandUtil placement to the actual Ars hit point and incoming direction. The first central fang starts at that point, the rest follow in 1.25-block increments. Sneaking creates the native rings; Ominous staff adds side rows/outer rings, with the original angular strides, delays and grounded placement. Extension adds one straight-row fang per unit, without changing Ars targeting or the sneaking rings.

The real native Fangs entity owns animation, delayed damage, ally checks and expiry. Its normal seven-argument constructor is essential: the totem constructor disables ordinary spell soul gains. Life Absorb sets the actual native flag so the original global damage handler heals the player owner. Soul Eater sets the native level (maximum ten) and leaves native hit eligibility/configured gains intact. It returns Goety souls even when the trigger cost uses Ars mana. Life Absorb is binary and excludes Amplify/native positive potency; it does not introduce a second damage/healing event.

Ignite calls the complete native entity touch result at the Ars-selected entity. Block hits use the native fire-charge use context at the actual hit face, retaining campfire/candle/fire placement rules without consuming or altering the caster's held item or tracing their view again. No stronger ignition/no successful block interaction means no charge.

Burning now supports Fangs and Ignite in addition to Fireball and Haunted Skull. Extension, Life Absorb and Soul Eater are opt-in dedicated augments, currently for Fangs only. Their extra trigger fees are independently configurable, default five selected-resource units each before native soul discounts. Quotes and successful activations use the same augment list. Shared validation and server execution both reject unsupported payloads, duplicate absorption, excess Soul Eater and Absorb/Amplify combinations; generated spells cannot bypass the editor rules.

Presentation uses Core's provider secondary key to put Goety ordinary augments before Goety projectile replacements. The key is evaluated inside the provider group, so it does not move all providers' projectiles to the global end or change effect/augment categories. Both Ars registry and client category sorting are covered.

The new world checks cover the actual fang entities, staff geometry and one batch fee, native expiry, delayed damage/global lifesteal/soul gain, invalid/no-ground/denied/creative casts and real campfire ignition. Their elevated scene remains inside GameTest's already entity-ticking area: laying blocks in a remote area loads chunk data but does not immediately make all entity sections visible to native collision queries. This is a test-fixture constraint, not a production chunk loader.


2026-10-11 dev.9 verification:

- NeoForge 1.21.1 with Iron and Not Enough Glyphs: all 16 required tests passed (`build/fangs-coexistence.log`).
- Forge 1.20.1 without Iron/NEG: all 23 required tests passed (`forge/build/fangs-gametest.log`). Both versions built release and source JARs.
- All 24 JSON resources, 22 glyph models, Chinese/English name/description entries and referenced native focus parents were checked against both pinned Goety JARs.
- Test-scene fixes precede these results: use entity-ticking GameTest chunks, explicitly clear the no-ground case in Forge's natural-terrain world, and avoid redundant configuration writes during Forge's file-watcher startup.
- No real client was launched; native particle/animation appearance and multiplayer visual synchronization still need in-game observation. No new casting animation/cooldown or survival acquisition behavior is claimed.


## 0.1.0-dev.10: Wither Skull and Feast

Wither Skull uses the original ModWitherSkull rather than a visual wrapper around another bolt. Its ordinary launch position, acceleration and yaw/pitch remain native on each loader; Ars supplies the aim and speed multiplier. Sneaking selects the native dangerous/blue state. Native potency, radius and Burning enchantments are read unconditionally as in WitherSkullSpell. Nether staff emits the primary and two randomly offset side shots, preserving the original extra radius application to side shots. A salvo has one trigger fee.

Intercept ModWitherSkull's own complete onHit override, not vanilla WitherSkull.onHit or an entity-only callback. The original body applies Wither II, direct-kill healing, enchantment interactions, game events, fiery explosions, griefing/loot decisions and removal. Remaining Ars effects resume after this body. Special carriers defer only removal during continuing activations, retain renderer flags/native tick effects, and pay per whole triggered salvo until resources fail. Native explosions retain their own target rules; this addon does not replace them with a new damage handler.

The native skull's duration handling is unusual: its hit code reads the victim player's native Duration enchantment, not the caster's SpellStat.duration. This adapter does not pretend Ars Extend Time changes that debuff. Ars trajectory lifetime modifiers still belong to the carrier. No caster charging animation or Goety cooldown enforcement is added in this batch.

Feast uses the selected Ars entity/location as the native search-volume center. Native alliance filtering still refers to the actual caster, while fang ownership refers to the context's damage owner. One central fang is placed at each enemy with warmup 1; Ominous staff adds five radius-1.5 fangs with warmup 0 and native angles. Its Radius enchantment multiplies the search radius, rather than using the generic additive equipment conversion. Native block-dust circles execute once at activation and native poof particles accompany successful spawning; the repeated charging prelude is not implemented.

Actual normal Fangs entities handle damage, player-owner absorption, soul-event eligibility and expiry. Feast accepts Burning and binary Life Absorb (incompatible with Amplify), while Extension/Soul Eater glyphs remain dedicated to Fangs. Native focus gear can still reach the original shared spawnFangs consumer. No enemies, only allies, unsupported ground, denied resource payment or invalid generated augments yields no fee. A successful multi-enemy/staff batch costs once.

Player-facing Burning descriptions no longer enumerate a support list. Dedicated augments retain the short scope information needed to understand their narrow use. Core compatibility validation remains in place.


2026-10-11 dev.10 verification:

- NeoForge 1.21.1 with Iron and Not Enough Glyphs: all 19 required checks passed (`build/skull-feast-coexistence.log`).
- Forge 1.20.1 without Iron/NEG: all 26 required checks passed (`forge/build/skull-feast-gametest.log`). Both targets built release/source JARs.
- Wither checks compare surviving native flights tick by tick against Goety's own SpellResult, run serially to avoid projectile interference. They verify primary/side-shot radius, blue/fiery flags, full entity Wither/kill healing, block explosion damage, one-batch charges, denied/creative salvos and repeated Orbit exhaustion/cleanup.
- Feast checks cover selected-region enemies, actual native ally exclusion, warmup/damage/global absorption/Burning, Ominous rings, one charge, allies-only/denied/creative casts and generated-recipe validation.
- Initial Forge fixtures encountered natural terrain above GameTest's below-ground origin: the correct fang batch and charge appeared, but the surface was higher than the artificial floor. Final fixtures use a preloaded horizontal area near the build-height ceiling, an explicit clear corridor/floor and surviving-flight assertions. No production chunk loading or damage rules were changed to pass these checks.
- All 24 glyph models, Chinese/English names/descriptions, JSON and native Goety parent references match both pinned dependencies. Release/source JARs include current MIT/third-party notices; release bytecode targets Java 21/17 correctly, excludes tests/dependencies, and Forge includes the complete native-hit refmap.
- No real client was launched. Renderer appearance, particle shape and multiplayer visual synchronization still need in-game observation.

## 0.1.0-dev.11: palette eligibility and generated recipes

The Ars spell editor disables a candidate only when validation reports an error at the candidate's newly appended position. Goety's validator formerly reported unsupported native parameters at the phrase action instead. This rejected completed recipes but left the Burning palette button lit for incompatible projectile replacements. Errors now identify the actual offending augment slots; Life Absorb/Amplify incompatibility identifies both sides so either insertion order is handled.

Burning remains opt-in for native consumers. Carrier-wide augment registration only permits composing a supported projectile replacement and Burning in the same phrase; it does not permit Burning on bare Ars projectiles, unsupported Goety payloads or a later unrelated effect. Both native projectile entry paths reject unsupported Burning and effect-only native parameters before creating projectiles or charging resources, including raw/generated recipes that bypass the spell editor. Player-facing descriptions remain unchanged and do not enumerate the support list.

2026-10-11 dev.11: one focused GameTest passed on each platform (`build/burning-eligibility.log` and `forge/build/burning-eligibility.log`). It exercises the real Ars editor validator and appended-slot filter for all 20 current payload/effect glyphs, phrase isolation, both sides of native absorption/potency exclusion, and raw unsupported projectile recipes with no spawn/payment. Both targets built release/source JARs. No client was launched.
