# 无限连发弹射物与中文修复（dev.50）

核对两端固定 Goety 依赖：SurgingSpell 派生 EverChargeSpell，原生 IChargingSpell.everCharge 为 true；ElectroOrbSpell 是普通 Spell，SteamSpell 是有限组发射的 ChargingSpell。EverChargeSpell 也用于雨幕、光束、采掘等持续功能，所以不能单凭这个标志把所有持续能力的准备和冷却删除。

GoetyActiveCastProvider.rapidFire 同时要求原生 everCharge 与已接入的 BoltFocus 弹射物身份；当前匹配电涌雷球（glyph_surging）。其 preparationTicks 返回 0；allowed 不读取该聚晶冷却作为门槛；released 不写入该聚晶冷却。直接连发仍通过原本 Core 触发事务和原生资源账户逐发支付，不添加定时自动发射，不修改原生法杖或聚晶数据。已经存在的原生冷却记录不会删除／覆盖，仍由原生系统正常计时。

混合法术中其他魔符仍参与最大／累加吟唱，非豁免聚晶的冷却仍在准备前与释放前检查，正常在释放时写入。普通蓄力爆发、持续雨幕、光束和其他提供者不被识别为无限连发弹射物。

新增 goety_nouveau_rapid_fire 两个针对性服务器用例：在有旧原生冷却、准备与冷却开关全开启的生存玩家身上立即连续生成真实 SurgingOrb，逐发耗尽灵魂能量后不再生成，且原生冷却记录保持原实例；混编保留另一聚晶的吟唱、冷却门槛和释放冷却，最终真实生成原生弹射物。有限 Steam 蓄力与持续 Arrow Rain 不被豁免。测试连接不模拟施法结果，实际使用 Ars resolver 和 Conflux 主动入口。

中文问题在三个名称及描述（瓦解、分散地雷、末影漫步）：源 JSON 已经含有字面问号。使用 UTF-8 修复，发布检查拒绝问号／替代字符并核对两端实际产物文本，避免仅检查键存在而漏掉内容损坏。

两端均报告 `All 2 required tests passed` 与 `BUILD SUCCESSFUL`，Neo 同时安装 Iron 与 NEG。混编发射在实际 dispatch 时记录原生弹射物，避免把数个游戏刻后的原生消失／移动误判为未发射；Forge Ars 4 用例另行提供基础 Ars 魔力池。二进制和源码产物均通过 124 个模型、双语文本、许可、元数据、字节码及测试隔离检查。

`gradle/validate-localization.gradle` 已由两个构建应用，资源和源码打包均依赖 `verifyGlyphLocalization`；中文值包含连续 ASCII 问号或替换字符时直接失败。
