# Goety 弹射物接入核对

版本：`0.1.0-dev.14`。固定依赖：Goety 3.2.0（NeoForge 1.21.1）与 2.5.58.4（Forge 1.20.1）。判断依据同时包含这两个依赖 JAR 的实际实现，不能仅根据更新后的上游源码判断。

本批新增 12 个替换增强，现共 17 个弹射物替换、17 个效果魔符、4 个原生参数增强。魔符顺序仍为同一类别内普通 Ars／附属 → Goety 普通增强 → Goety 弹射物替换 → 下一提供者；没有把效果改成增强或子形态。

## 本批完整命中与变体

| 聚晶 | 原生载荷 | 必须保留的行为 |
| --- | --- | --- |
| ice_spike_focus | IceSpike / IceSpear | Goety 霜冻魔杖的速度、穿透；冰冻、重力和方块嵌入。重复载体每次激活前重置原生已穿透集合。 |
| lavaball_focus | Lavaball / HellBlast | 下界法杖升级与三发；不洁套装选取 HellBlast；火焰、爆炸、方块规则和命中特效。 |
| steaming_focus | SteamMissile | 实际原生飞行、渐进模型转向、尾迹和命中。本次一个 Ars 触发发射一枚，不复刻原生持续吟唱弹幕。 |
| bouncy_bubble_focus | BouncyBubble | 反弹、重力、大小、爆炸与深渊法杖三发。独立 travel 不能遗漏；重复载体寿终不能额外免费爆炸。 |
| electrocute_focus | ElectroOrb | 原生追踪目标、电击爆发和风暴法杖增益。初始候选沿 Ars 发射方向选取，不重新沿玩家视线选择目标。 |
| surging_focus | SurgingOrb | 追踪、连锁、下界颜色、原生尾迹。独立 travel 需要适配；特殊载体的原生追踪不能篡改 Ars 飞行方向。 |
| smack_stone_focus | SmackStone | 抛物线、火焰、土系法杖三发；原生基础 fiery 取自 duration 的行为保留。 |
| blossoming_focus | BlossomBall | 直接伤害之外还有毒性、荆棘、时长及野性法杖范围。不得只调用一次 onHitEntity。 |
| magma_bomb_focus | MagmaBomb | 爆炸与岩浆怪仆从、寿命、归属；下界法杖强化。 |
| void_flash_focus | VoidShockBomb | 命中后的增长动画、魔法火焰、八个 VoidShock 和虚空法杖三发。普通弹仅接续一次；重复载体在已付款命中内重置增长阶段。 |
| sword_focus | SwordProjectile | 主／副手符合原生标签的武器副本、附魔与伤害；成功发射磨损 10 耐久；缺武器不扣费。已经发射后手中武器断裂不影响该次命中。重复载体每次成功激活再磨损。 |
| razor_wind_focus | RazorWind / VoidSlash | 非标准 onHitEntity：按增长的包围盒筛选并调用原生 damageEntity；保留粒子、虚空变体、每目标一次及两秒寿命。 |

附魔统计沿用每个原生法术各自的计算条件与单位，不能统一一套“全部附魔都相加”。普通形式由 Goety 实体自己的物理和碰撞驱动；环绕／溅射／回响等使用 Core 轨迹并跳过重复的原生运动与碰撞，但仍运行原生子类的模型和粒子更新。

`SpellTargetProjectile` 有独立的同步 owner UUID 和 `setOwner(LivingEntity)` 重载：必须同时设置原生归属与 vanilla Projectile 归属，否则伤害与联盟检查可能仍读取旧施法者。转化后本次施法的装备／付费者和实际伤害归属仍使用 Core 的各自上下文。

## 风垫修正

`Cushion.getRadius()` 表示额外半径；0 是合法默认值，实体自带 3 格基础半径。旧联动将额外半径 `<= 0` 作为失败，导致默认风垫完全不生成。本版只排除负半径。

原生风垫会落向地面，默认约一秒后才开始 ENCHANT／CLOUD 粒子并清除范围内生物摔落距离。增加原生 duration 会同时延长暖机等待：这是固定依赖里的原生阈值行为，本次没有另建粒子或保护逻辑。

## 余项分类

以下仍未接入，不应当作为“少调用一次原生发射”就完成的普通弹射物：

| 内容 | 暂缓原因／适合的后续入口 |
| --- | --- |
| Cyclone | 原生会移除并重新创建旋风实体，还带捕获／吸附和移动行为。需要让执行会话随原生实体迁移，不能丢弃同步、费用及轨迹归属。 |
| Grapple | VineHook 与施法者之间的持续连接、拉动和释放行为，需要持续执行或重施接口。 |
| Arrow Rain / Blizzard / Bombardment / Meteor Shower / Trident Storm / Hail / Monsoon | 原生为区域生成器、云层或持续降落弹幕，适合 Ars 选点效果／持续执行；直接替换一颗弹射物会遗漏主要行为。 |
| Teeth | 天花板寻找与向下落牙陷阱，适合位置效果；并非沿 Ars 载体方向发射的常规弹。 |
| Earth Fist / Void Shock / 地雷等 | 位置型地面攻击、延迟或陷阱，后续逐个作为效果核对。 |

这是普通发射载荷清单，不代表 Goety 所有召唤、吐息、位移与区域法术已经完成。

## 验证范围

聚焦 `goety_nouveau_projectiles` GameTest：原生发射类型／出生点、实际原生命中／后续实体、重复载体扣费与耗尽、法杖批次、冰矛穿透、剑断裂后的命中、创造免费、默认风垫与旧灵魂箭／特殊载体视觉更新回归。测试使用真实依赖实体和实际伤害，不仅核对代理参数。

无界面的服务端测试不能确认实际客户端画面。粒子渲染、声效和模型的最终观感仍需要在客户端看一次；本批直接使用依赖的原生实体及资源，并未复制一个近似的特效。


## 副手与潜行判定（dev.14）

Goety 原生 `Spell.isShifting` 除潜行外还要求手持 `IWand`。Ars 已是本次施法工具，因此弹射物的潜行变体直接读取实际施法者的潜行状态；无需再带一根 Goety 法杖。此变更仅用于火球、凋灵头与熔岩炸弹，不改变原生召唤召回等流程。

冰刺使用原生法杖匹配：主手 Ars 法术书、副手 `goety:frost_staff` 会生成原生 IceSpear，保留 1.5 倍速度及穿透。`irons_spellbooks:ice_staff` 是另一模组的物品，不满足此原生判定。两版完整施法的服务端检查均验证了类型、速度和连续两目标实际伤害，未复现正确 Goety 法杖下仍为冰刺的情况；客户端最终画面仍需人工确认。

岩浆炸弹（`magma_bomb_focus`）与熔岩炸弹（`lavaball_focus`）不同。原生 MagmaBomb 爆炸直接生成 2 只限时岩浆怪，匹配法杖时为 3 只，不检查普通召唤法术的数量上限。联动调用原生爆炸，保留该行为和原生存活时限。
