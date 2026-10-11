# 移动区域法术核对（dev.38）

依据固定 NeoForge Goety 3.2.0 / Forge Goety 2.5.58.4 的 CycloneSpell、TidalSpell 以及完整原生实体/父类/渲染器。新增两个效果魔符，不替换 Ars 发射形态，也不把持续存在的区域实体误做成持续施法。触发成功一次付一份聚晶费用；实体随后自行运动、攻击和结束。

## 旋风

使用真正的 Cyclone；原施法者完整 Unholy 装备选择 FireTornado。原施法者提供 WandStats、Wind staff、聚晶附魔与付款；显式 damageOwner 仍是实际伤害来源。基础寿命 600 tick、半格发射偏移、原生拉扯、云/火轨迹、声音和结束粒子均由原实体负责。Ars 命中点替代原施法者发射位置，选中的敌人替代原施法者重新射线选敌；弹射物方向（包括纵向）优先，没有时使用施法者朝向。

Amplify/Dampen 修改原生 potency，AOE 修改半径，时间修饰按一份原生寿命加减。原生 Wind staff、附魔倍率/整数 velocity 保持各自计算。不把 Cyclone 自身的区域命中映射成额外 Ars 编排触发。

原生实体会在追踪/拉扯时删除旧实体并生成新实体。补充持久化的原施法者 UUID，随实际 native fakeRemove 复制；同时保留原生重建漏拷的 boltSpeed，但只影响本附属标记的实体。显式 owner 是 Mob 时，不允许其原有 AI target 替换 Ars 已选目标。原生 owner 友军排除照旧，另外排除原施法者及其友军的拉扯和伤害。FireTornado 的 hellfire 装备判断读取原施法者，实际伤害仍归显式 owner。

## 潮汐

使用真正的 TidalSurge，保留原生渲染器、推进加速度、重力、碰撞、drench、水敏感加伤、击退、slam/shrink 与删除。Ars 命中点就是出生位置，入射方向决定水平朝向；纯纵向入射时使用施法者朝向。方块落点沿命中面微移，避免直接埋在方块内。Abyss staff 在这里生成 -10/0/+10 度三浪，一整批付费一次。

原版寿命包含“从施法者走到目标的距离 + 10 tick + duration”。Ars 这里已在命中点出生，因此明确使用 10 tick + 原生 duration，再吃 Ars 时间倍率；不会凭空再添加已走过的射程。原生 potency / 2 决定 wave scale。原 owner 友军与原施法者友军同时排除。

## 验证和边界

两端各四个 goety_nouveau_moving_areas GameTest 全部通过：真实旋风目标/显式 owner、原生重建保留速度和来源、NBT 保存读取、实际伤害和寿命；原始 Unholy gear 的火焰变体/Burn Hex；真实潮汐推进/击退伤害/slam/消失、原施法者保护与 Abyss 三浪；零余额、创造、零时长和已移除目标。

Native 实体、同步字段和原始 renderer 均未换壳。服务器测试不证明人工观看后的粒子/声音质量，客户端效果仍留待测试目录检查。原生 Goety 未标记的实体使用原有逻辑；不新增另一套区域 AI 或魔力账户。

注册聚晶审计剩余十二个键见 native-focus-coverage.md。水鞭的 GulfTentacle 同时用 owner 作附着者和伤害来源，还会在命中后生成额外触手；尚未接入，不能只生成一条触手便声称完整支持。
