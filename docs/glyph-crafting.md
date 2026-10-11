# 魔符制作（dev.51）

装置：Ars 原版抄写台（`ars_nouveau:scribes_table`）。手持法术书右键，选中要制作的魔符，投入材料或让台子从附近容器取材料。得到魔符后右键学习。每个配方消耗 27 点经验，与 Iron 当前的一阶魔符配方一致，不新增魔源或灵魂能量制作费用。

## 材料

效果与弹射物替换魔符：对应的 Goety 聚晶 ×1＋魔源宝石 ×1。守护法术没有实际注册的对应聚晶，使用 `goety:cursed_ingot`（诅咒锭）×1＋魔源宝石 ×1；原版残留的 warding_charm 贴图不代表物品存在。不改变聚晶的原版制作方式、NBT、附魔或装备行为；此制作会消耗那一枚聚晶，聚晶上的附魔和绑定不会转移到魔符。

| 额外增强魔符 | 材料（另需魔源宝石 ×1） |
| --- | --- |
| 燃烧 | 烈焰粉 `minecraft:blaze_powder` ×1 |
| 延展 | 野蛮之牙 `goety:savage_tooth` ×1 |
| 生命吸收 | 灵质 `goety:ectoplasm` ×1 |
| 噬魂 | 浓缩绿宝石 `goety:soul_emerald` ×1 |

每个平台生成 124 个配方，覆盖其所有已注册魔符。NeoForge 有 Breeze，Forge 有 Temblor Clap，另一端不会生成对应的无效配方。未接入的命令、敕令、召集、军团和回溯不新增魔符。

## 数据与 JEI

共享定义：`common/recipes/glyphs.json`。`gradle/glyph-recipes.gradle` 只负责生成两个 Ars 版本各自的原生配方格式。配方不由 Java 动态注册，原版数据包重载及 JEI 的 Ars glyph 类别直接读取。

- NeoForge 1.21.1：`data/goety_nouveau/recipe/glyph_<name>.json`，使用 `inputs` 与堆栈 `output`。
- Forge 1.20.1：`data/goety_nouveau/recipes/glyph_<name>.json`，使用 Ars 4 的 `inputItems` 与字符串 `output`。

可以用同 ID 数据包覆盖配方。源码包包含共享定义和本平台生成的 JSON。新法术接入时应同时补配方；制作与学习遵循 Ars 原生流程，不增加第二套解锁记录。

## 验证范围

`goety_nouveau_recipes` 的单个针对性用例核对运行时全部 124 个真实 Ars 配方及其物品，然后向真正的抄写台投放火球聚晶／灵质与宝石。等待原版制作完成，验证材料消耗、经验扣除、魔符产出与右键学习。两个平台共用该用例；JEI 客户端画面复用原版类别，仍由人工确认外观。

2026-10-11：NeoForge 1.21.1 与 Forge 1.20.1 均通过上述 1 个必需用例及构建。测试直接调用原版方块实体 ticker，避免无正常玩家的 GameTest 世界模拟距离差异；未修改正式抄写台行为。两端二进制／源码包中的 124 份配方、版本、许可证与平台隔离也已核对。
