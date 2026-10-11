# 腐蚀光束与剩余射线审计（dev.33）

固定依赖为 NeoForge Goety 3.2.0、Forge Goety 2.5.58.4。本批依据实际 JAR 的 CorruptedBeamSpell、AbstractBeam、CorruptedBeam、CorruptedBeamRenderer 和 CBeamPacket 实现。没有新增面向 Goety 的 Core API 或 Iron 依赖。

## 原生实体与施法角色

腐蚀光束是效果魔符，只接受自己或 Goety 友军作为持续执行者。使用真正的原生 CORRUPTED_BEAM 实体；原生实体保留空间主人为执行者，当前朝向、偏移、64 格最大追踪、墙体阻挡、宽度、命中集合、无敌帧处理、击退速度还原与原生渲染器都继续生效。

威力读取原施法者的 SpellStat 与聚晶附魔。伤害来源使用显式 damageOwner，否则使用施法者，不把友军执行者当作伤害／资源主人。命中同时保留执行者原生排除，并排除原施法者、显式伤害主人和原施法者友军。NeoForge 原生法术伤害类型作用域仍经平台 Hook 保留；Forge 直接执行原生回调。

本批保留 CorruptionImmobile 原生配置对实际执行者的限制；不是把限制施加到远处原施法者。普通共享频道的玩家输入限制仍由现有独立 movementRestrictions 开关管理。

## 时间、费用与结束

原生 CorruptionDuration 默认为 0，即一直持杖施法；不能把它视为无效果，也不能让一次 Ars 右键创建永久射线。增加 channels.unboundedDurationTicks，默认 200 tick（10 秒）。仅当支持的原生频道没有有限时长时使用此基准，本批用于腐蚀光束；原生正数时长优先。Ars 时效增强乘算基准。

沿用 Conflux ExecutionSession 和共享 GoetyChannelCasting，每 20 个持续脉冲支付原生报价。第一次成功生成后立即执行原生首个伤害 tick，然后提交该次费用，避免恰好只有一次费用时生成实体却没有任何伤害。首发之后空资源即停止，不允许预付组留下免费伤害实体。创造免费；无效目标／无资源／生成失败不收费。

共享频道增加 BooleanSupplier 脉冲、存活判定和 close 回调，旧雨幕／吐息使用原构造入口。光束只在首次脉冲生成，此后不复制实体；世界中实际原生 tick 负责持续伤害。结束回调移除自己的光束并清理频道移动状态。实体在原生伤害前也检查存活／关系／资源／截止时间，避免等到服务器后置的会话 tick 才终止。

## 客户端与保存

AbstractBeam 不继承 SpellEntity，因此单独接入同步的 Ars 来源标记。真实执行者的原生 OWNER 元数据与 UUID 仍可用于渲染器。客户端 tick 会先用 getOptionalOwner 从实体 ID 解析，包括非玩家友军；不只依赖 getOwner 的玩家 UUID 分支。

Ars 生成的光束不发送原生持杖 CBeamPacket：位置／朝向来自真正主人与服务器，客户端保留原生插值和三层动态光束。普通 Goety 射线继续发送其原生数据包、保留 itemBase 与持杖检查。没有伪造玩家使用法杖能力或使用物品状态。

会话是临时的。Ars 光束 shouldBeSaved=false；若其他保存方式意外恢复了带来源标记的实体，其没有有效会话时在伤害前移除。不会把缺失会话当作永久非物品光束。

## 验证与余项

定向用例验证真实光束伤害／归属、存活靶的转向和墙体阻挡、友军／执行者排除、同步主人和来源、普通原生持杖取消、按秒费用、恰好一次费用的实际伤害、资源不足、关系变化、创造自然结束、双方移除与孤儿清理。包含前一批流星雨间隔／资源回归；未重跑整个目录。服务器验证不能证明客户端实际观感，光束外观、声音和多人插值仍需人工观察。

dev.33 时 Water Jet 与 Prisma Beam 仍待接入；现已于 dev.34 接入，见 water-prism-audit.md。以下保留当时对原生入口的审计：两者的原生渲染入口限定玩家并检查使用物品／聚晶，不能仅调用伤害回调。WaterJetRenderer 的 renderJetLayer 可复用原生几何与双层贴图；PrismaBeamRenderer 还依赖目标、施法时长和渐变动画。未来显示入口必须能为真实非玩家友军服务，并有可跟踪的持续状态，不能用替身玩家模拟。

区域／持续余项仍需逐项核对：Void Shock、Quaking、Warding，以及 Telekinesis、Shocking、Whirlwind、Flying、Burrowing 等。已有 Leeching／Surging／Steam／Feast 的效果或弹射物表示不等同于已复现全部持杖交互。这是接入范围记录，未据此宣布整个持续法术目标完成。
