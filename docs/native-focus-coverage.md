# 原生聚晶绑定覆盖核对（dev.46）

固定 NeoForge Goety 3.2.0。按实际 ModItems 的字节码与 BootstrapMethods 映射注册物品→供应器→法术类，再与 BoltFocus／EffectFocus 的有效键比较；不能仅依据物品名猜测对应法术。这个表是覆盖审计，未注册的原生辅助法术、跨版本差异和具体行为完整性仍需单独核对。

118 个注册聚晶法术绑定中，115 个键已有魔符入口；不同魔符可共用一个聚晶，也可能有不对应原生注册聚晶的辅助入口，所以此数字不能代替魔符数或完整行为验证。

以下 3 个聚晶键尚未接入。它们不是全部可直接实现的区域／持续效果；下一步要检查真实回调、目标与客户端入口，再决定接入方式。

| 聚晶键 | 原生法术类 |
| --- | --- |
| crafting_focus | CraftingSpell |
| ender_chest_focus | EnderChestSpell |
| blink_focus | BlinkSpell |

仆从命令、物品操作界面、两次交互、传送／连续寻路等不能仅靠注册一个效果魔符证明完成；需要保留原生行为的接入证据。现有召唤限制、区域回调与持续执行检查分别记录在各批 audit 文档中。

GrappleSpell 已接入：实际 VineHook、玩家原生牵引、再次触发免费收回与共享槽位归属已验证；原生绳索渲染器读取同步的 Ars 施法手。只对自己或友军玩家生效，不接管普通法杖钩索。客户端外观与冷却 HUD 仍需人工检查，详见 grapple-audit.md。

EndWalkSpell 已接入：实际融身入影效果与执行者自己的原生返回锚点、原生事件终止／返回处理。只有自己或友军玩家可作为执行者，不覆盖任何已有锚点。原来源装备、费用与 Ars 增强独立处理，详见 endwalk-audit.md。

SoulLightSpell 已作为原生光源弹射物接入（dev.46），与 Glow Light 共用 LightProjectile 的完整飞行／命中与两层粒子适配；效果是原生灵魂光方块和实体发光，不伪装为伤害法术。详见 soul-light-audit.md。

剩余三项的已核对边界：Crafting／Ender Chest 属于玩家物品操作界面；Blink 是原生自体／友军执行者的即时传送。

BanishSpell 已接入（dev.46）：原生完整目标限制与随机落点回调使用 Ars 实际选中的敌对生物，成功传送才扣费；自身／友军不作为控制目标。普通原生法杖保持视线选点，详见 banish-audit.md。
