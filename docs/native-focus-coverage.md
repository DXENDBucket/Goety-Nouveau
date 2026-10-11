# 原生聚晶绑定覆盖核对（dev.42）

固定 NeoForge Goety 3.2.0。按实际 ModItems 的字节码与 BootstrapMethods 映射注册物品→供应器→法术类，再与 BoltFocus／EffectFocus 的有效键比较；不能仅依据物品名猜测对应法术。这个表是覆盖审计，未注册的原生辅助法术、跨版本差异和具体行为完整性仍需单独核对。

118 个注册聚晶法术绑定中，111 个键已有魔符入口；不同魔符可共用一个聚晶，也可能有不对应原生注册聚晶的辅助入口，所以此数字不能代替魔符数或完整行为验证。

以下 7 个聚晶键尚未接入。它们不是全部可直接实现的区域／持续效果；下一步要检查真实回调、目标与客户端入口，再决定接入方式。

| 聚晶键 | 原生法术类 |
| --- | --- |
| soul_light_focus | SoulLightSpell |
| crafting_focus | CraftingSpell |
| grapple_focus | GrappleSpell |
| ender_chest_focus | EnderChestSpell |
| end_walk_focus | EndWalkSpell |
| blink_focus | BlinkSpell |
| banish_focus | BanishSpell |

仆从命令、物品操作界面、两次交互、传送／连续寻路等不能仅靠注册一个效果魔符证明完成；需要保留原生行为的接入证据。现有召唤限制、区域回调与持续执行检查分别记录在各批 audit 文档中。

GrappleSpell 已核对：原生发射与收回依赖玩家的 SEHelper grappling 引用，且再次触发会收回已有钩索并改变费用／冷却。它不能作为一个普通伤害弹射物入口宣称完成；还需核对原生 VineHook 的牵引、绳索显示、持物检查和共享状态接管。
