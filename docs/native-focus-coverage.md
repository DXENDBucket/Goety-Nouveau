# 原生聚晶与法术覆盖核对（dev.49）

## 运行时清点

固定 NeoForge Goety 3.2.0／Forge Goety 2.5.58.4。分别在真实加载器 GameTest 中遍历 BuiltInRegistries.ITEM 的 goety 物品，按实际 IFocus.getSpell 获取法术类，再对照已构造的 GlyphCatalog.ALL。此方法包含自定义聚晶自己的 getSpell 实现，不再只依赖 ModItems 的 lambda／BootstrapMethods。

两端各 124 个有效原生 IFocus 物品；各 119 个有魔符入口。剩余五项完全一致：

| 聚晶 | 实际法术 | 接入边界 |
| --- | --- | --- |
| command_focus | CommandSpell | 聚晶物品自身的指挥交互；此 Spell 类没有实际 SpellResult |
| order_focus | CommandSpell | 另一种原生指挥交互，同上 |
| call_focus | CallSpell | 依赖聚晶中已绑定的仆从数据，调用 CallFocus.call |
| recall_focus | RecallSpell | 依赖聚晶绑定的目的地，含原生有效性与物品专属冷却 |
| troop_focus | TroopSpell | 依赖聚晶绑定的仆从类型，调用 TroopFocus.call |

Crafting／Ender Chest 已在 dev.49 复用实际原生菜单接入。它们像 Ars 的个人界面效果一样只打开真实玩家施法者自己的容器；选中另一名玩家不会读取其背包。只有实际打开才支付资源，末影箱需实体触发。

以上不是尚未实现的普通“生成一批召唤物／创建区域／持续攻击”回调；不能把不含聚晶数据的 Ars 效果硬当成绑定聚晶实例。它们属于后续物品界面、绑定与仆从指挥设计。现有各类生成召唤物保留原生召唤限制、有限生命与维护机制。

旧文档的 118 项是 ModItems 静态绑定的局部提取，遗漏了动态聚晶和跨版本差异；不能作为全量分母。dev.48 的运行时清点修正此结论；dev.49 新增两个实际原生菜单入口。魔符数与原生聚晶数也不同：当前两端各 124 魔符，其中 123 共享，另有 Neo Breeze 或 Forge Temblor Clap；辅助魔符和部分共用聚晶入口不能用物品总数直接推算覆盖率。

## 非注册类核对

独立读取两个固定 JAR 的 class 常量池与 superclass 链，com/Polarice3 下各有 125 个非抽象 Spell 派生类。Warding 没有注册聚晶，但已以自己的魔符物品作为施法键接入。

ElderCurse、Neo WarStomp 没有注册聚晶；扫描所有原生 class 未找到对它们的实际调用点（WarStomp 仅被其匿名内部类引用）。因此当前依赖中没有它们可直接复用的原生玩家入口，不能误写成已有可用聚晶或断言它们目前由原生生物使用。将来把这些遗留类做成新魔符是另一项新增内容。

Forge TemblorClap 则是真实注册聚晶，已在 dev.48 补齐；此前用 Neo 的映射作为 Forge 的清点依据遗漏了它。两版 FireBreath 的包路径不同，但已由实际聚晶绑定接入。Neo Breeze 位于其移植作者包，由平台入口接入，不在上述原作者包 class 数量中。

## 持续与移动边界

所有 Goety 持续执行入口使用实际自身／原生友军作为 executor，原来源装备／费用独立，并持续复查存活、维度、友军关系、魔符开关与资源。各批实际回调、资源中断及结束清理证据见下列 audit 文档与 scope-completion-audit.md。

Grapple、End Walk 和 Blink 已接入，分别保留原生钩索、原生返回锚点／事件、完整三分支瞬间传送。只有自身或友军可执行；不接管未经附属标记的原生状态。Banish 是负面控制而非友军移动增益，作用于 Ars 选中的敌人，保留原生限制和传送事件，成功才付费。

Soul Light／Glow Light 保留实际 LightProjectile 飞行、两层粒子和完整无直接伤害的命中。Forge Temblor Clap 保留完整原生爆炸与眩晕；模型、Mixin 和原生类依赖不进入缺少此聚晶的 Neo 包。

客户端人工观看尚未作为全目录验收；各批服务器验证证明所述实际回调和状态，不能替代所有画面、音效、网络插值的人工评估。此清点不宣称全 Goety 的界面、物品交互和指挥行为均已联动。
