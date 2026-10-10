# Goety & Nouveau：接入计划与源码核对

## 范围与边界

这是独立附属，modid 为 `goety_nouveau`，依赖 Ars Nouveau、Goety 和 Ars Conflux；不依赖 Iron & Nouveau。本轮是可编译的开发基础，不注册可游玩的 Goety 魔符，也不调整整合包流程。

Core 负责施法上下文、Ars 载体、延续编排、触发计费与物理资源池预留。Goety 附属负责 Goety 法术参数、实体、装备条件、资源访问和特有召唤行为。以后加入其他附属时，通过 Core 共用载体能力，不做 Goety 与 Iron 的互相依赖。

Not Enough Glyphs 等可选载体的接入继续由 Core 管理。Goety 附属不再复制一份附属弹射物兼容代码。

## 已核对的上游

| 平台 | 固定构建版本 | 官方源码 | 核对提交 |
| --- | --- | --- | --- |
| 1.21.1 NeoForge | Goety 3.2.0 | [Vivideru/Goety-3](https://github.com/Vivideru/Goety-3) | `2f42435123d2e781107f41a14dadd0d59cbd64ea` |
| 1.20.1 Forge | Goety 2.5.58.4 | [Polarice3/Goety-2，1.20 分支](https://github.com/Polarice3/Goety-2/tree/1.20) | `75a80b2d5a195b3b93efc12f14049b3ad5ada7d9` |

发布版本以带 SHA-512 的 `dependencies.lock.json` 为准；源码分支用于理解行为，不能替代对实际发行 JAR 的编译核对。Goety 的 [官方项目页](https://www.curseforge.com/minecraft/mc-mods/goety) 列出了两个 Minecraft 版本。

两边保留了 `IFocus`、`ISpell` 和 `SpellStat`。聚晶的 `IFocus.getSpell()` 是较稳定的入口，避免依赖已经有版本差异的内部法术注册表。准备阶段提供了 `FocusSpells` 与 `NativeSpellStats`，不把它们放进不依赖 Goety 的 Core。

## 施法参数与上下文

Goety 原生参数是 potency、duration、range、radius、burning、velocity，不强行套用 Iron 的“卷轴等级”。从 `ISpell.defaultStats()` 取得基准，然后按各法术实际用途映射 Ars 增强；每次执行使用新的 `SpellStat`，避免散射、延迟和持续触发互相改写参数。

Goety 的 `getTarget(caster)` 往往读取原生目标或玩家视线，发射方向也经常直接读取施法者朝向。不能只调用一个 `SpellResult` 就视为已经兼容了 Ars 选点与目标。

后续接入必须分别保留：

- 法术所有者、伤害来源与资源付款者；
- Ars 触发点、命中目标、方向和弹射物速度；
- 原生装备检查所用的法杖上下文；
- 原生法术实体的完整生命周期、命中表现及 Ars 后续效果。

`ISpell` 的 start/use/stop 生命周期和结果方法返回 `void`。真实触发是否成功，需要适配器提供可靠结果，不能在任意调用后无条件提交费用。

## 灵魂能量：用户已确定的默认值

服务端配置 `goety_nouveau-server.toml`：

```toml
[resources]
source = "SOUL_ENERGY"
```

另一个选项是 `ARS_MANA`。这只决定未来 Goety 魔符触发所用资源，不修改 Goety 原生法术或其他 Ars 魔符。当前配置已注册，实际扣费随第一个可游玩法术一同实现。

原生 `SEHelper` 与 `TotemFinder` 应用于读取 Goety 实际采用的灵魂容器，不能另建一份玩家灵魂能量数据。原生既可能使用玩家储存，也可能使用图腾；资源预留键必须对应同一个物理池，资源种类使用 `goety:soul_energy`，不能因为来自不同魔符就重复预留同一份能量。

需要先解决的细节：`ISpell.soulCost(...)` 有法术与装备修正，而 `SEHelper.decreaseSouls(...)` 内部还会执行灵魂折扣与灵魂损失事件。必须检查完整原生扣费链，让报价、预留和实际扣除一致，避免重复折扣或实际支出超过预留。不能直接套用 Iron 的扣蓝实现。

创造模式遵循免费施法规则。持续效果逐次触发检查资源，资源不足停止；散射按预算缩减数量，再由 Ars 生成对称散射。Goety 原生灵魂 API 主要面向玩家；非玩家施法的资源池适配是明确待做项，不能无提示地视为免费或伪造玩家。

## 第一批接入候选

这是逐个核对后的候选，不表示已经完成兼容；先做第一行的完整链路。

| 原生聚晶 ID | 原生法术 | 首先验证的行为 |
| --- | --- | --- |
| `goety:soul_bolt_focus` | Soul Bolt | Ars 载体替换、原生弹射物、完整命中与延续、灵魂计费 |
| `goety:fireball_focus` | Fireball | 原生爆炸、装备导致的多发与变体、实际成功计费 |
| `goety:poison_dart_focus` | Poison Dart | 原生毒镖与命中效果 |
| `goety:iceology_focus` | Ice Chunk | 原生冰块弹射物、速度与落点 |
| `goety:skull_focus` | Haunted Skull | 追踪目标、所有者与寿命 |
| `goety:frost_nova_focus` | Frost Nova | 指定位置的范围效果和目标阵营 |
| `goety:entangling_focus` | Entangling | Ars 实体目标与原生控制效果 |
| `goety:soul_heal_focus` | Soul Heal | 施法者与被治疗者区分，原生友军与治疗表现 |

Soul Bolt 原生会按法杖和装备选取不同弹射物，并使用施法者方向。适配应复用原生实体与命中处理，同时将发射几何替换为 Core 上下文；所有者仍为真正施法者。原生命中之后，Ars 的剩余效果只能执行一次。

第一批完成后再处理召唤、持续法术和特殊位移。Goety 的召唤物契约、灵魂维护与消失方式由本附属处理；它们与 Iron、Ars 召唤物的生命周期不能先验地视为相同。

## 分阶段落实

1. **当前基础**：独立双版本项目、共享聚晶读取/参数快照、资源配置、依赖锁定、许可和本地版本控制。
2. **首个可玩切片**：Soul Bolt + 灵魂账户适配 + 原生实体命中延续；同时验证无 Iron 的环境。
3. **触发组合**：普通弹射物、触摸、自身、散射、回响轨迹，以及 Core 已支持的可选附属载体；验证资源不足和创造模式。
4. **扩大法术池**：逐一加入上述候选，记录不支持的装备/目标条件；不通过批量盲调 `SpellResult` 宣称全兼容。
5. **生存获得方式**：在可玩链路稳定后单独决定魔符学习、聚晶与原生仪式之间的关系。准备阶段不擅自改变这部分流程。

后续 Ars 整合实机测试使用 `1.21.1-万法归一`，基础环境由用户部署。当前不自动复制开发基础 JAR 到该环境，也不把编译成功当作游戏行为验证。
