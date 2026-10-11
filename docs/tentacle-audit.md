# 水鞭原生附着行为核对（dev.39）

固定 NeoForge Goety 3.2.0 / Forge Goety 2.5.58.4。依据两端完整 GulfTentacleSpell、GulfTentacle、Owned / IOwned 和 GulfTentacleRenderer。这里是短暂的一次性挥击，并非持续保持右键的 channel；不添加每秒重复挥击或额外维持费用。

## 三个角色

Ars 选中的自身或友军生物是实际施放者；方块、敌人、死亡、移除或不同维度对象均拒绝。原施法者提供聚晶附魔、WandStats、Abyss staff 和费用。显式 damageOwner 提供实际伤害来源。

GulfTentacle 的原生 getTrueOwner 同时决定附着位置、朝向、伤害和后续触手所有者。这里保留 native 所有者为实际施放者，使用真实 setTrueOwner 同时同步 UUID 和客户端 owner ID，让原 renderer / 实体 tick 可以正常工作。另以持久化 UUID 保存原施法者和 damageOwner，仅在标记的触手伤害调用中替换 indirectDrench 来源，不篡改客户端空间所有者。没有新建触手实体类型或另一套伤害/动画逻辑。

## 原生行为

原生身体朝向、左右臂侧偏半格、向前一格、眼部下移 0.45 与潜行偏移保留；Ars 发起物品默认主手，实际正在使用副手时用副手侧。每 tick 的跟随与朝向继续执行原 GulfTentacle.tick。初始原生 spawn event 4、STentacleRangePacket、SRepositionPacket 和原 renderer 均保留，不以新增自制粒子代替。

原生基础 range 为 4，原焦点 range 附魔与 WandStats 继续增加距离。Amplify / Dampen 修改 potency；正 potency 通过原生 BUFF（potency - 1，限制 0–10）影响攻击属性。原生仅支持 potency/range，不虚构可拉长二十 tick 动画的时间增强。

在生命 tick 8–11 内使用原生视线/方块遮挡和伤害循环，二十 tick 后删除实体。真正的 Abyss staff 命中成功后由原方法生成额外 GulfTentacle；保留 native range、offset、BUFF、实际附着者与 false staff 标记，额外触手不会再递归生触手。注入只为新增实体复制原施法者与 damageOwner，不自行重新实施命中。

原施放者友军判断仍由原生 tick 完成；伤害入口补充原施法者/显式 damageOwner 的友军过滤。每 tick 检查实际附着者与原施法者的存活/维度/友军关系，失效时删除，不让它继续成为敌方攻击。没有标记的原版 Goety 触手保留原行为。

## 验证

两端各四个 goety_nouveau_tentacles GameTest 通过：原生类型/owner 与距离；七 tick 无伤害、实际攻击 trace/伤害归属、跟随转向/移动、二十 tick 删除；真实 Abyss-staff 追加触手、buff/范围/归属继承、不递归和一次费用、NBT 保存读取；真实友军/显式 owner 伤害排除、失去友军关系、未标记原生实体隔离；敌人/方块/空余额拒绝、创造免费、目标移除和 native animation event。

测试使用真实 native 方法与实体，并不证明客户端实际观感与声音；这些仍需人工在测试目录确认。第一次测试的原生隔离探针用了刚创建的 FakePlayer，受到新玩家保护影响；改用真实 Cow 验证原生伤害归属，并另用真实 Cow 检查友军过滤。

完整聚晶入口覆盖仍未完成。Illuminate、Soul Light 等候选，以及不属于简单区域/持续行为的菜单/传送/双次交互，见 native-focus-coverage.md。
