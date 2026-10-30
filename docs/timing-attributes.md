# Conflux 通用时间属性（0.1.5）

需要 Conflux 0.1.40。标准主动吟唱由 Core 在汇总后应用 `ars_conflux:cast_time_reduction`；原生自身聚晶和棱镜光束的独立准备也读取同一个属性，且继续向原生回调提供已完成的准备进度。

`GoetyFocusCooldowns.atLeast` 在原生 Goety 冷却之后应用 `ars_conflux:cooldown_reduction`。只在原生自身聚晶作用域中，额外接住原生／附属聚晶直接调用 `SEHelper.addCooldown` 的自定义写入；本提供者已经缩减的写入使用短暂标记避免重复计算。作用域外的 Goety 原生法杖不受影响。

不缩短持续输出时长、连发间隔或 Ars 延迟，不改变灵魂能量费用。默认加成均为 0。完整 API 和调试命令见 [Core 文档](../../Ars-Conflux/docs/timing-attributes.md)。
