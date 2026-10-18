# Goety 版本兼容检查：dev.56

核对日期：2026-10-18。依据实际发布 JAR，而非仅核对上游分支源码。

| 平台 | 原构建依赖 | 新构建依赖 |
| --- | --- | --- |
| 1.21.1 NeoForge | Goety 3.2.0 | [Goety 3.2.1](https://modrinth.com/mod/goety/version/3.2.1) |
| 1.20.1 Forge | Goety 2.5.58.4 | [Goety 2.5.59.2](https://modrinth.com/mod/goety/version/2.5.59.2) |

两端继续共享 `VERSION`，本次为 `0.1.0-dev.56`。Conflux 验证基线固定为 `0fe0e82d8788463878e2a6b7736c6cb7b9e0d25a`（0.1.27），未修改 Core 受版本控制的文件。最低 Core 要求仍为 0.1.24。

## 冰矛、毒刺的正式环境调用

反馈指向 dev.53 的冰矛和毒刺；dev.55 仍保留相同调用。旧 Mixin 用 `remap=false` 的 `@Invoker("resetPiercedEntities")` 调用 Goety 的私有方法。但 Forge 的实际发行包里，`IceSpear` 和 `PoisonQuill` 的这个方法都叫 `m_36723_`；Goety 2.5.58.4 和 2.5.59.2 均如此。开发依赖经过反混淆，能够掩盖这种正式环境目标名错误。因此不能把问题简单归因为新版 Goety 删除了接口。

改为一个共享 `NativePiercingHistoryMixin`，直接访问 Goety 自有且未被重命名的 `piercingIgnoreEntityIds` 和 `piercedAndKilledEntities` 字段。清除两份记录的行为与原生私有方法一致；不访问同名的原版箭记录，也不反射调用混淆方法。原生箭的伤害、状态、飞行和普通穿透仍由 Goety 执行，重复载体只在新的付费命中前重置记录。

新增用例验证带对应法杖的毒刺、冰矛对同一目标连续两次付费命中，仍能造成原生伤害并各续接一次 Ars 编排。普通毒刺飞行／多目标穿透以及副手冰霜法杖的自然飞行穿透也纳入本次定向检查。

## 发射聚晶的版本变化

- Goety 3.2.1 新增 `LaunchBasePower`、`LaunchPotencyPower`。Neo 适配读取实时配置；3.2.0 缺少这两个配置时沿用原生常数。
- Forge 2.5.59 起，非匹配法杖基础动力变为 1.0，装备效能附魔减半。对仍受支持的 2.5.58.4 保留旧版的 1.5／附魔全强行为。
- 接受者始终按自己的朝向移动，装备参数仍取原始施法者。非法接受者、资源不足等规则没有变化。
- 吟唱／冷却依然通过原生聚晶接口读取，不以旧持续时间硬编码覆盖新版。

Forge 的 Goety 构建依赖改用包含实际版本的本地坐标，避免替换同名 JAR 后误用 ForgeGradle 的旧反混淆缓存。发行声明没有无故提高 Goety 最低版本；旧依赖的完整行为矩阵未重跑。

## 检查范围

既有 Neo 发行包直接引用了 173 个 Goety 类，Forge dev.53 发行包引用了 174 个。对新旧原生 JAR 的类成员及继承关系比较，未发现这些直接调用中有新增的缺失成员；另检查了现有私有访问器。本结论不等于新版所有原生行为均已人工验证，也不自动接入新增内容或重做后的原生法杖攻击阵形。

针对正式包的检查工具：

```powershell
python tools/check_piercing_runtime.py build/libs/goety-nouveau-0.1.0-dev.56-mc1.21.1-neoforge.jar libs/goety.jar
python tools/check_piercing_runtime.py forge/build/libs/goety-nouveau-0.1.0-dev.56-mc1.20.1-forge.jar forge/libs/goety-2.5.59.2-forge1201.jar
```

它直接读取未经反混淆的 Goety 发行包，核对字段名和类型、附属访问器签名、旧 Invoker 是否已移除，以及是否意外包含 Goety 类；不替代完整 Mixin 启动检查和客户端观察。

定向服务端验证使用 `goety_nouveau_projectiles,goety_nouveau_variants,goety_nouveau_locations` 三个 namespace：NeoForge + Goety 3.2.1、Forge + Goety 2.5.59.2 均为 14/14 通过。命令见 [构建说明](building.md)。客户端外观、多人与原始反馈者环境未人工验证。

两端最终 JAR 均通过上述发行字段检查，同时对照了各自旧基线的原始 Goety JAR。打包检查确认含 MIT 许可与第三方声明，未包含 Goety／Conflux 类或 GameTest 类；Forge 已完成 `reobfJar`。

最终产物 SHA-256：

| 产物 | SHA-256 |
| --- | --- |
| `goety-nouveau-0.1.0-dev.56-mc1.21.1-neoforge.jar` | `e19220ca2fdff3e858083c1c28476ebdc6a737524462d479f069e0806d09261d` |
| `goety-nouveau-0.1.0-dev.56-mc1.20.1-forge.jar` | `fa2c610b75ed741e3562e5daeb39825a685292a16ca61d6c52e9e5ca40f0ebc2` |
