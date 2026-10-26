# dev.58 附属聚晶通用自身施法交接

- 用户目标：先做通用附属聚晶，再尽可能保留特殊法杖修饰。
- 工作树：`parallel-workspaces/goety-native-self-dev58-20261026/Goety-Nouveau`，分支 `feature/native-focus-self-casting`，基线 `04d5e45b146a4a5945151b93d35e749e7832cf6e`。
- 固定 Core：`b46be94b17799573b825a8e4a11870fa3dc857d6`；源码及配置未修改。发行最低依赖调为 `0.1.25`，对应新使用的 GUI 图标 API。
- 目标：NeoForge 1.21.1 / Java 21 / Goety 3.2.2；Forge 1.20.1 / Java 17 / Goety 2.5.59.2。

## 最终实现

- `NativeFocusRegistrationMixin` 在物品真正登记时识别外部 `IFocus`，由 `NativeFocusIndex` 生成效果魔符、Ars 配置、物品；不依赖注册先后顺序，不重复 Goety 本体。
- `NativeFocusResources` 与平台 helper 提供原生 Scribe 配方、源聚晶父模型和 GUI 图标，全部运行时引用。
- `NativeFocusCasts` 使用 Conflux 会话和当前账户，执行原生开始／持续／结果／停止与施法事件、准备、收费、连发、冷却和清理。只接受真实玩家自身命中。
- `NativeFocusScope`／`NativeFocusWandMixin` 复制实际法杖、匹配聚晶，限定在回调作用域内接管原生公共查询；保留具体法杖类型、数据与匹配附魔，原装备不改写。
- 常规魔符保留原适配。全局开关与逐聚晶禁用项在 `native_self` 下；通用魔符只公开强化／抑制，Tier 1。
- 详见 [机制与边界](native-focus-self-casting.md)。未知法术不做远程原点、友军代施法、弹射物替换或原生命中续接；依赖真实主手／按住法杖／独立客户端状态或特殊 UI 的附属仍可能需要专门适配。没有实际逐个第三方附属的人工测试。

## 验证证据

两个平台在自己的隔离工作树运行相同 namespace `goety_nouveau_native_self`，最终均为 **5/5**，没有重跑所有旧玩法矩阵：

1. 后注册聚晶与物品、真实 Scribe 配方、父模型；本体不重复。
2. 实际原生寒霜法杖分支、强化统计、每回调独立副本、结束后公共查询恢复，真实法杖 Shots 不变。
3. 三连发只扣三次原生费用，共用原生聚晶冷却。
4. 远程目标和原生前置拒绝免费；everCharge 预付二十次一组，下一组无资源时停止并只清理一次。
5. Ars Mana 账户不扣灵魂能量；创造绕过已有冷却与资源收费，生命周期仍正常执行。

NeoForge 日志：`build/native-focus-final-neo.log`。Forge 日志：`forge/build/native-focus-final-forge.log`。Forge 账户测试使用真实 Ars capability；NeoForge 使用其 wrapped-caster 账户接口。两平台的接口不同，测试没有修改 Core 账户实现。

两端最小客户端均检查 3 个生成物品的烘焙模型与缺失 sprite，打开并查看 GUI 预览。测试聚晶引用原生灵魂箭／火球／冰刺的模型；截图在各自 `run/native-focus-preview/screenshots/native-focus-glyphs.png`，内容一致，SHA-256 为 `08d3e507835132a588f8c9acd77a7ecfeff5bd0b79d17f039a2530cd4e5d612c`。这是渲染与模拟附属验证，不代表实际外部附属全部效果、动画已验证。

命令：各平台目录使用对应 JDK，`:runGameTestServer :runClient :jar :sourcesJar --offline --console=plain --no-daemon -PnativeSelfTests=true -PnativeSelfPreview=true -PgameTestNamespaces=goety_nouveau_native_self -PgameTestHeap=900m`；Gradle daemon 堆上限 448m。最终依赖元数据提高最低 Core 后只重打包。Forge 最终 JAR 已执行 `reobfJar`。

## 产物与部署

发行包检查包含 MIT、没有测试夹具／界面，也没有复制 Goety 的类或资源；Forge refmap 有配方注入映射。

| 产物 | SHA-256 |
| --- | --- |
| NeoForge dev.58 | `e181e6f266245e3c4d94917293e3f441e2df236d1657ac8e272a272d606bba95` |
| NeoForge sources | `f9ab8b55f7ea16d3af4166e4cb97b26375f9de97abce4aafa2b29e76e540fa19` |
| Forge dev.58 | `df5eb8cdad3d1cccc7fe47820f51fe5bdd2f5b959aea4fbf2dd21317d589d463` |
| Forge sources | `afe02fd39c09ac619ae753a722165dfaed81ef1cd645b544641a6e9c84bd6a3f` |

协调者仅更新“万法归一”原有 Goety 联动，备份 dev.57；不修改实例内 Core、配置或“万途归零”。1.20.1 人工测试实例没有 Goety 本体，因此只保留构建产物，不向该实例添加缺依赖的联动。主检出目录的 `build/libs` 与 `forge/build/libs` 保留同一产物。没有推送或发布 CurseForge。
