# 灵魂光源原生接入核对（dev.45）

## 原生入口与表现

固定 NeoForge Goety 3.2.0／Forge Goety 2.5.58.4。SoulLightSpell 的原生 SpellResult 发射实际 SoulLight，继承 LightProjectile，而不是伤害用的 SoulBolt。它与已有 Glow Light 共用完整光源弹射物适配，不新增近似投射物／渲染器／粒子系统。

Ars 编排写作「弹射物 → 灵魂光源」，灵魂光源是投射物替换增强。生成实际原生 SoulLight，继承同步的 ManagedBolt 状态和原生实体注册／渲染。原生 LightProjectile 构造器起点是眼高 -0.1；原生 shootFromRotation 在标称 1.5 上加一个朝向向量，未强化为 2.5。适配使用 Ars 选出的方向和该真实速度，Ars 加速最后乘入一次。无重力、实际源粒子 SOUL_LIGHT_EFFECT、实际尾迹 BULLET_EFFECT 均复用原生虚方法。

普通载体交回完整原生 tick、碰撞和命中；特殊载体沿用 Conflux ArsTrajectory，既有 LightProjectile mixin 继续发出两层原生客户端粒子，不额外跑一套碰撞运动。实体真实注册类型与 native LightBlock 分别为 SOUL_LIGHT 与 SOUL_LIGHT_BLOCK，不能借用 GlowLight 的资产代替。

## 原生命中与资源

原生实体命中施加 200 tick 发光，不造成直接伤害；方块命中使用命中面相邻位置，原生可替换／无阻挡检查决定是否放置灵魂光。占用的实心相邻格不替换。完整原生命中之后执行 Ars 后续效果；重复手工回调已移除的普通弹射物不能重复执行后续编排。

普通发射一次原生费用。环绕等重复载体不预扣原生弹射物费用，每次成功原生命中事务扣费并执行后续内容；没资源即结束。真实原生 onHit 会 discard，既有重复命中 scope 暂缓删除以支持后续触发。创造沿用免费账户。Burning 不在原生支持范围，编辑与运行校验都拒绝，不因新增光源类扩大支持。

两种光源共享原有 LightProjectile 最大 lifetime 边界 600 tick；实际执行也受 Ars 载体时长限制。用 lighting() 表示已有辉光与灵魂光两种无直接伤害光源，原有伤害专属测试池也据此排除两者。没有改变其他投射物的飞行、计费或伤害语义。

## 验证

命名空间 goety_nouveau_soul_light，两端各四项通过：

1. 比较原生发射实际实体的起点；验证实际 SoulLight 类型／主人／速度／无重力／两层粒子与方块身份、一次费用与 transient；让真实世界 tick 驱动三 tick 飞行，确认不被提前删掉且不重复扣费。
2. 完整实际实体命中、200 tick 发光、无直接伤害、恰好一次后续编排；实际面向相邻格的方块放置，阻挡格保留原生失败行为。
3. 实际 Ars 环绕载体接管运动、源／尾迹虚方法仍由原生提供，不预扣费用；两份费用仅支持两次实际结果，随后中止。
4. 空账户、创造、原生不支持的 Burning 和实际加速倍数。

服务器能核对原生粒子身份和客户端执行入口，但不能证明视觉、音效和多人插值观感；这些留待客户端人工检查。没有重跑历史全量目录。
