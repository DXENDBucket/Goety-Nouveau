# 0.1.0 正式版本与 CurseForge 回执

2026-10-26，按用户要求去掉 dev 后缀，双版本统一为 `0.1.0`，CF 文件类型为 release。版本提交 `03aea14df245b621f0841a4b06c4a8ef73fe6146`；实现来自已验证的 dev.58 / `bbf912d026f2fc942882a9ecb92d365d28fb6590`。

本次只修改 VERSION、当前 README 与发行说明。隔离构建仍固定 Core `b46be94b17799573b825a8e4a11870fa3dc857d6`，其受版本控制文件未变化。NeoForge 使用 Java 21，Forge 使用 Java 17，并重新执行 `reobfJar`。未重跑游戏测试；此前两端各 5/5 及客户端模型检查的记录见 [dev.58 交接](handoff-dev58-native-focus.md)。重新打包后逐个比较类文件，NeoForge 236 个、Forge 241 个均与已验证 dev.58 完全一致。

| 发行文件 | SHA-256 | CF file ID |
| --- | --- | --- |
| `goety-nouveau-0.1.0-mc1.21.1-neoforge.jar` | `b1407bcf8e21a7e8dd32eb6315de38edcb8e21ba390d9f203341ae2c1d71a105` | `9333767` |
| `goety-nouveau-0.1.0-mc1.20.1-forge.jar` | `71e656ec187e06000ae7984723ec11a061f79f73c64240ebf46f34f6e494954b` | `9333768` |

MIT、modid、依赖、版本与双端标签均已核对；发行包不含测试夹具或复制的 Goety 素材。两份 sources JAR 也已生成，但发布工具只上传二进制发行包。

项目 ID `1739392`；计划为本机 Core 忽略目录 `build/curseforge/20261026-221751-dec8ce86/plan.json`。两份上传 API 均成功返回文件 ID，上传台账状态为 uploaded，进程退出码 0。`isMarkedForManualRelease=false`，审核通过后自动公开；API 接收不等于审核已经完成。

公开说明见 [0.1.0 更新日志](releases/0.1.0.md)。本地发布配置和说明副本位于本附属忽略的 `build/curseforge/`，不修改 Core 受版本控制的发布文件，也不把凭据写入仓库。代码与文档已经本地提交，没有推送 Git。
