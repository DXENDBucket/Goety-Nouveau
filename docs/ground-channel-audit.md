# 虚空冲击、震地与守护（dev.35）

固定 NeoForge Goety 3.2.0 / Forge Goety 2.5.58.4，核对实际 JAR 的 ModItems 物品注册、VoidShockSpell、VoidShock、QuakingSpell、WardingSpell、WandUtil 和 SEHelper；不增加 Goety 专用 Core API 或 Iron 依赖。

## 虚空冲击

原生 VoidShockSpell 绑定 stellar_focus，而非 void_flash_focus 或不存在的 void_shock_focus；实际 ModItems 注册供应器已核对。Ars 效果在自己或原生友军身上授予有限的原生连发段。原始施法者提供法杖／装备／数值／费用，实际执行者提供位置、高度和原生目标，显式 damageOwner 为原生冲击实体主人。

执行原生 useSpell，从开始发射的 phase=10 起逐 tick 前进。普通每 5 tick 一发，虚空法杖每 2 tick 一发。原生实体保留随机上方云团位置、10 tick 悬停、追踪、转向／拖尾激活事件、真实碰撞爆炸／粒子、寿命和数值。不把它换成普通直线 Ars 弹射物，不重做它的实体 tick。

准备阶段只计算原生首发前的 10 tick；持续阶段基准为 native castDuration−10，Ars 时效调整持续阶段。第一发随实际触发支付，之后使用此前 Thunderstorm 相同的持续映射，每 20 活跃 tick 付费；这是 Ars 自动持续效果的费用映射，不声称原生普通 Spell 本来就逐秒收费。空资源、执行者失去友军关系或双方移除停止继续生成。已付费的实体按其原生寿命飞行结束，不清空已发射的攻击。

## 震地

原生伤害全部在 useSpell 中；只调用空的 SpellResult 会没有攻击。准备只算 defaultCastDuration，命中后按原生 range／radius 发出有限波段。普通三道前方震波、潜行周边环、地卜法杖两段弧形／额外环范围、盾牌处理、抬升、ModFallingBlock 和原生声音均由原生方法执行。

实际执行者仍限定自己或友军。原始施法者的 potency/range/radius 装备先按原生规则加入 stats；原生回调收到实际执行者时，跳过再次从执行者身上取装备。原始施法者的潜行决定环形分支。伤害来源单独保留；执行者、原始施法者及其友军不被原生震波伤害。不会移动或暂时改写原始玩家的位置。

surroundTremor 的原生向下找 MODEL 循环没有边界。仅在 Ars 回调 scope 内：读取未加载／越过建造高度的地面时返回真实空状态，并让循环条件在边界终止。不会创造假基岩或地面掉落，也不强制加载这些环形地面区块。普通 Goety 施法的循环行为不变。其他原生已有的有界地面寻找继续使用原方法。

## 守护

固定两版含 WardingSpell 和 warding_charm 贴图／模型，但没有注册对应聚晶，WardingCharmItem 本身也不在物品注册表中。不能靠猜测物品 ID 查找它。附属直接绑定原生 WardingSpell 实例，以自己的 goety_nouveau:glyph_warding 作为施法／冷却入口，引用已有的原生美术资源，不伪造 goety 命名空间物品。

原生能力仅有玩家的 SEHelper 护盾数据，因此只接受自己或原生友方玩家。实际 SpellResult 写入接受者的原生数据：最大 20、接受者 tickCount 每 5 tick 增加 1＋potency，伴随原生附魔粒子。原始施法者提供 potency，接受者装备不会被当作原始施法装备。保留原生 0 费用；无有限原生时长，采用 channels.unboundedDurationTicks，再应用 Ars 时效。结束停止补盾，但保留已有护盾交给 Goety 自身的承伤规则。

这不是对 WardingCharmItem 全部 Curios 行为的移植，也没有额外发明怪物的平行护盾 capability。

## 验证与余项

两版六项针对性服务器检查通过，证明原生虚空冲击实体、伤害与显式来源、悬停、普通／法杖间隔、按秒费用／创造、关系变化停止生成；震地真实伤害／三道及原生法杖碎块、潜行环形、最底层无地面的循环终止；实际友方玩家原生护盾／增益频率／停止后保留；敌方、非玩家护盾接受者和无资源的拒绝。

实际客户端外观、原生粒子与声音仍需人工观察。当前已有召唤适配仍保留原生配额／生命／召回机制。持续余项包括 Telekinesis、Shocking、Whirlwind、Flying、Burrowing，尚需逐项落实回调、目标、客户端入口与结束清理；本批不据此宣布整个持续法术目标完成。
