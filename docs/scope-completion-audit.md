# 本轮召唤、区域、持续与友军执行范围审计（dev.48）

本轮目标是完成现有可明确接入的召唤、区域和持续法术，并将持续法术与 Iron 持续吐息／位移动作的实际执行者限制为自己或友军。这不是“所有 Goety 物品交互、界面与绑定聚晶全部重做”的目标；也不把某个已存在但没有原生入口的遗留类自动变成新玩家技能。

## 如何确定范围

两个固定依赖都在实际运行的注册表逐项读取 IFocus.getSpell，而非只按名称或部分静态 ModItems 供应器猜测。当前各 124 原生聚晶物品，各 117 个有魔符入口；七个缺项逐项检查实际代码：Crafting／Ender Chest 打开物品界面；CommandSpell 本身没有结果回调，command／order 由聚晶物品交互实现；Call／Recall／Troop 读取专用聚晶实例中保存的仆从、目的地或仆从类型。它们是绑定／指挥／界面流程，不是本轮要补的普通召唤或持续回调。

Forge 专有 Temblor Clap 由此清点发现并补齐，不能用 Neo 的列表代替 Forge 的列表。独立 superclass 清点确认两个原作者包各 125 非抽象 Spell 类；所有实际生成仆从的 SummonSpell 家族均有适配。Warding 没有聚晶但已经接入；ElderCurse／Neo WarStomp 没有注册聚晶也没有实际原生调用点，当前没有它们的原生玩家流程。详见 native-focus-coverage.md。

## 要求与证据

| 要求 | 当前实现与实际证据 |
| --- | --- |
| 现有可直接生成的召唤物 | GoetyServantCasting／Profiles、GoetySummonCasting、GoetyHoggingCasting 和平台 hooks 生成真实原生实体，保留独立 owner／focus 配额、SummonDown、buff、有限生命、维护与原生 AI；常规、野生、元素、末影、Ministrous／Hogging／Neo Breeze 的运行验证见下表。没有把仆从指挥聚晶改为凭空生成仆从的效果。 |
| 位置／目标区域与原生生命周期 | EffectCasting 的完整分派及各 Area／Location／Hazard／Weather／MovingArea／Geomancy 适配覆盖实际聚晶；保留原生实体、爆炸／碰撞／命中、粒子、寿命、持久数据。后续补齐 Trident Storm、Cyclone／Tidal、Tentacle、Illuminate、Pulverize／Scatter、Tunnel、Soul Light，以及 Forge Temblor Clap。原生显形、晚加入跟踪和动画同步问题有独立修复与服务器验证。 |
| 持续逻辑由共享 Core 执行 | GoetyChannelCasting／各 Beam／Prisma／Burrowing／Telekinesis 入口以 ExecutionSession／ExecutionSessions 管理固定持续段，原生正确回调决定动作和 cadence；保留按原生组付费、实际结果付费或蓄力完成付费的差异。空资源、无效目标、自然到期与移除会清理自己拥有的会话／实体／采掘状态。不是只生成一次实体便认为持续行为完成。 |
| 持续执行只能由自己／友军承担 | 每个持续入口均经过 GoetyChannelCasting.eligible，持续 tick／session running 再检查；它验证源和 host 存活／同维度／非旁观／区块加载及原生 Goety 关系。breath geometry、native geometry 不改变真实来源装备或支付者。多个频道的敌方拒绝、关系变化、中断和清理已有实际 GameTest。 |
| Iron 吐息、射线和位移为友军增益 | Targeting.alliedExecutor／beneficialHost 在 BreathAdapters、RayAdapters／NativeRayGeometry、MotionAdapters 中检查执行者；BreathPose.valid 持续复查。BeneficialExecutorCases 对五种 cone、两条 ray 和四种动作验证敌人不执行、不付费、不生成会话；实际友军 cone 付费执行且失去队友关系移除，友軍 Volt Strike 有实际运动，自身 ray 有实际持续会话。两端日志均有正数通过。方块固定位置的无实体发射源不伪装为敌方 executor。血步／霜步始终移动来源本人，选中敌人仅作为目的地方向。 |
| 保留角色与第三方共存 | caster、executor、damageOwner、resource account 明确分离；来源装备和实际付款独立，Goety 不把本次灵魂账户传播到其他提供者。Iron 和 Goety 共用 Core，无相互硬依赖。Temblor 实际归属测试发现原生 DS 关系排除不足，并补齐 source 与显式 owner 友军保护；普通原生回调无本作用域不修改。 |
| 双版本交付与版本控制 | 同一 VERSION，Neo Java 21／Forge Java 17；按依赖实际可用内容构造平台魔符，Neo Breeze／Forge Temblor 是不同的附加项。两端发布源码／二进制检查原生 parent、文本、字节码、Mixin／Forge refmap、Core 依赖、MIT／第三方声明，排除测试与依赖打包。逐批本地 commit／tag，Neo 测试实例部署且旧包备份。 |

## 运行证据复核

读取保留的最终成功日志，要求实际“正数 required tests passed”并且 BUILD SUCCESSFUL；不把仅编译成功或 0 项通过当成行为验证。本次没有重跑历史全目录；当前生产变化是 Forge Temblor 及其注册，历史各适配的主要行为实现没有被本批改写。

| 家族 | Neo／Forge 通过项 | 本地日志名（两项目 build/ 下） |
| --- | --- | --- |
| 常规召唤／屏障 | 5／5 | creature-summon-tests-final.log、summon-wall-tests-final.log |
| 野生／元素／末影召唤 | 4／4、4／4、3／3 | nature-servants-tests-final.log、elemental-summon-tests-final.log、ender-summon-tests.log |
| 最后召唤批次 | 4／3 | remaining-summons-tests.log，差一项是 Neo Breeze |
| 雨幕／风暴 | 5／5、4／4 | rain-storm-final.log、storm-channel-tests.log |
| 腐蚀光束／水流与棱彩 | 4／4、5／5 | beam-channel-final.log、water-prism-tests-final.log |
| 虚空冲击／震地／守护与风系持续 | 6／6、5／5 | ground-channels-tests-r4.log、wind-storm-tests-r2.log |
| 念力／钻掘与原生交接 | 6／6、1／1 | utility-channel-tests-r2.log、mining-handoff-tests-final.log |
| 移动区域／水鞭／照明 | 4／4、4／4、4／4 | moving-area-tests-r2.log（Forge 为 r3）、tentacle-tests-r2.log、illuminate-tests-r2.log |
| 地卜／隧道与深度边界 | 4／4、4／4、1／1 | geomancy-tests-r3.log、tunnel-tests.log、tunnel-edge-tests.log |
| 钩索／融身入影／放逐／闪烁 | 各 4／4 | grapple-tests.log、endwalk-final-tests.log、banish-tests.log、blink-tests.log |
| Forge 震击及 Neo 缺项隔离 | 1／4 | temblor-tests.log，包含真实全 IFocus 清点 |
| Iron 友军执行边界 | 见本地正数通过日志 | Irons-Nouveau 两项目的 beneficial-host-tests.log，实际断言见 BeneficialExecutorCases |

每组真实断言和保留边界在对应 audit 文档／GameTest 源码中，不用一个新用例证明全部旧法术。客户端原生资产／渲染入口和同步状态已经按批核对，但没有声称全目录的人工画面、听感与联机插值已验收；这不把本轮已验证的服务器功能改称为未做，也不把一次服务器测试说成完整客户端测试。

剩余界面、带数据聚晶、指挥／选择流程，以及未来把休眠类设计成新的技能，需要另行定义交互与玩法，仍明确保留在覆盖文档中；本轮没有以删掉这些记录来制造全目录完成的结论。
