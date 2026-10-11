# 照明原生持续行为核对（dev.40）

固定 NeoForge Goety 3.2.0 / Forge Goety 2.5.58.4 的 IlluminateSpell。使用原生 useSpell 的完整搜索/放置/转换回调，不另写第二套光源放置规则。Ars 选择自身或友军生物为执行者；原施法者提供 WandStats、焦点附魔和原生资源账户。

## 时长与空间

原生 defaultCastDuration=72000 是保持使用物品的时长；实际准备阶段取 IlluminateChargeUp。Ars 触发后用现有 channels.unboundedDurationTicks（默认 200 tick）作为持续搜索的时长，时间增强按原生通道规则缩放。首次回调采用达到 charge-up 后的第一个合法周期；后续按原生 IlluminateDuration tick 间隔执行。零间隔会导致原方法取余异常，因此在建立会话前拒绝，原版未进本附属回调的配置行为不被改写。

空间参数与可见性使用真实执行者的实时位置/眼高，原生 radius=16、垂直半径为 radius/2；AOE 添加半径。焦点附魔检测和 RADIUS level 读取原施法者；原生存在的 radius *= level / 2 计算保留，不另换公式。

在本附属 native scope 内，扫描不读取未加载的候选/邻居/可见性路径区块，不超出真实构建高度。先做原生最终会检查的可存活/邻面支持短路，避免在明显不可能放置的位置重复执行八条可见性射线；真正的可见性、液体/可替换状态、动态支撑、光照阈值与最终 world.setBlock 仍交原方法处理。未 scoped 的 Goety 原版回调保持不变。

## 触发付款

一次原生扫描最多成功一个候选。通过实际 tryToPlaceGlowLight / replaceSoulLight 的成功返回观察原生方块变化；一个成功结果提交一份 focus 费用，失败搜索退回预留费用。不让观察器提前认定成功，不把无效范围或正在吟唱当作已放置。

本效果和 charged Prisma 一样直接建立 Core ExecutionSession，后续使用 session.activate / 原有 ResourceCharge 事务，不再经过外层“一开始就付款”的路径。空空间中可以继续免费搜索，目标移动后可产生新的合法结果；空蓝/灵魂能量不足、死亡、移除、敌我关系变化、禁用或有限时长到期停止。创造沿用原有免费账户。原生 gathering / glow 粒子、放置声音、Glow Light 方块和 Soul Light 转换都保留。有效工作结束时按现有配置写原生冷却记录；不创建另一套冷却或资源。

## 验证记录

主用 goety_nouveau_illuminate 四个针对性场景：真实远程执行者放置/一次费用/同 tick 不重复；空搜索免费、移动后按原间隔工作与资源不足中止；真实 Soul Light 转换、观察器、加载/高度边界及真正 charge-up；无效/敌对对象、创造、零时长、失去友军和有限时长到期。另以 goety_nouveau_illuminate_edge 单场景验证原生零周期不会进入异常回调。

第一次 Forge 空搜索场景受旧测试光源的邻面支持影响，清理扩大到实际 source/host 全扫描范围与支撑边界后复查；不是改变原生可放置规则以迎合断言。具体最终结果见 building.md。服务端测试不能证明人工观看后的客户端粒子/声音质量，仍留待测试目录检查。

入口覆盖仍有十个已注册聚晶键未接入，见 native-focus-coverage.md；菜单、传送、连续寻路和双次交互不以简单注册代替实现。
