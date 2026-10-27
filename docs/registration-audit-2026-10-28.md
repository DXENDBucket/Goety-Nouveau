# 魔符注册审计：2026-10-28

## 结论与范围

接续 [48gxbGQ 报告](https://mclo.gs/48gxbGQ)的重复物品注册修复，检查 Core 与七个附属的注册时机、重复回调、动态生成魔符和 ID 边界。发现并修复 Goety 的另一处注册表边界问题，双平台版本更新为 `0.1.3`。其他项目未发现相同注册路径的缺陷；它们此次仅做源码审计，没有重新运行完整客户端矩阵。

| 项目 | 检查时版本 | 源码结论 |
| --- | --- | --- |
| Ars Conflux | 0.1.36 | 可选魔符由平台入口注册一次；Elemental 条件钩子不重复注册物品。其他兼容刷新不创建第二套物品。 |
| Iron & Nouveau | 0.17.28 | 只记录外部 Iron 法术 ID，过滤真实法术注册表；自身物品事件明确排在 Ars 之后，动态表按法术 ID 去重。没有此次 Goety 的提前注册后再被 Ars 扫描的问题。 |
| Goety & Nouveau | 0.1.2 → 0.1.3 | 提前／延后注册继续工作；新增只观察物品注册表的限制，详见下文。 |
| BOMD & Nouveau | 0.1.1 | 固定魔符目录在初始化时交给 Ars 注册，没有另一个手动物品注册入口。 |
| Cataclysm & Nouveau | 0.1.2 | 同上；独立注册的材料物品不是魔符。102 个能力字面 ID 无重复。 |
| Aquamirae & Nouveau | 0.1.1 | 固定目录；13 个能力字面 ID 无重复。 |
| Mowzie's & Nouveau | 0.1.1 | 固定目录；17 个能力字面 ID 无重复。 |
| Legendary Monsters & Nouveau | 0.1.0-dev.12 | 固定目录；独立材料物品不重复注册魔符。30 个能力字面 ID 无重复。 |

检查时的各仓库提交保存在 [构建记录](registration-audit-build.json)。本任务不修改 Core 或其他附属，也不增加附属之间的依赖。

## 新发现：其他注册表中的聚晶别名

NeoForge 的 `MappedRegistry.register` 与 Forge 的 `ForgeRegistry.register` 用于多种注册表。旧观察器仅判断值是否为 `IFocus` 物品，没有判断正在注册哪一种表。

若模组将一个已注册聚晶放入自定义的 Item 值目录，并使用另一个 ID 作为目录键，旧观察器会把目录键误认为新的物品 ID，生成额外魔符及引用不存在物品的 Scribe 配方。这与 0.1.2 修复的 Ars 二次注册不同：本次最小复现表现为多余魔符和配方解析失败。

复现夹具真的创建 `native_focus_fixture:focus_catalogue` 注册表，将已注册的 `warmup` 聚晶作为 `catalogue_alias` 条目。修复前：

- 游戏加载记录出现 `goety_nouveau:glyph_native/native_focus_fixture/catalogue_alias` 配方解析失败，原因是 `minecraft:item` 中没有此别名。
- 合并的注册测试因魔符目录多出条目而失败，实际为 1 项失败测试。

修复仅在两个平台的观察器入口加入 `minecraft:item` 注册表检查。原有按聚晶 ID 去重、生成配方时保留数据包覆盖、0.1.2 的物品实例身份检查继续保留。没有屏蔽真正的 ID 冲突，也不吞掉注册异常。

## 最小必要验证

共用测试命名空间 `goety_nouveau_registration`，每个平台运行 1 项合并测试，覆盖：

- 在 Ars 自身物品事件中提前注册的聚晶，以及稍后注册的三个聚晶，都有且仅有一个有效魔符物品。
- 四个魔符保留源模型引用，真实 Scribe 配方完成解析。
- 自定义目录别名不生成第五个魔符；重复观察同一聚晶不改变目录大小。
- Goety 本体聚晶不被通用发现机制重复生成。

| 环境 | 实际结果 | 日志 |
| --- | --- | --- |
| NeoForge 21.1.250 / MC 1.21.1 / Ars 5.13.3 / Goety 3.2.2 / GeckoLib 4.9.3 | 1/1 required tests passed | `build-registry-boundary-fixed-neoforge.log` |
| Forge 47.4.0 / MC 1.20.1 / Ars 4.12.7 / Goety 2.5.59.2 | 1/1 required tests passed | `forge/build-registry-boundary-fixed-forge.log` |

命令使用现有 wrapper，任务为 `:runGameTestServer :jar :sourcesJar`，参数为 `-PnativeSelfTests=true -PgameTestNamespaces=goety_nouveau_registration -PgameTestHeap=1000m --offline --no-daemon --max-workers=1`。Forge 使用 JDK 17，NeoForge 使用 JDK 21；Forge 发行 JAR 完成 `reobfJar`。

验证以实际测试数量和失败记录为准，不仅检查 Gradle 退出码。修复后两份日志均没有该目录别名的配方错误。两个平台的二进制与源码 JAR 完成 CRC、元数据、许可、Mixin 配置、测试夹具排除及上游素材排除检查，哈希见构建记录。

固定 Core 为 `b626e92705c9c7d4ff607c7b0a782e1766558f7d`（0.1.36），受跟踪文件保持不变。没有启动客户端、没有重跑所有法术，也没有加载报告者的完整模组列表；真实附属中的每个聚晶仍可能有独立的施法实现问题。
