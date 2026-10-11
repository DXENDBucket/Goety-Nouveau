# 闪烁原生接入核对（dev.47）

## 三个原生分支

固定 NeoForge Goety 3.2.0／Forge Goety 2.5.58.4。使用完整 BlinkSpell.SpellResult；Ars 选中的自己／Goety 原生友军为执行者，不接受敌人或方块作为执行者。这里的实体目标是接受增益的主体，不是被强制拉走的敌人，也不是传送目的地。

执行者自身视线选中实体时，调用真正的 MobUtil.teleportTowards，最多尝试 64 次；没有视线实体、原施法者未潜行时，执行原生 findTeleportLocation 和 enderTeleportEvent；没有视线实体、原施法者潜行时，调用原生 MobUtil.teleport，最多尝试 64 次。潜行不会覆盖已经选中的实体靠近分支，这是原生顺序。

执行者提供身体、当前位置、视线、碰撞与随机数；原施法者提供 WandUtil stats、Range 附魔、Void Staff、潜行分支和资源账户。提前合入原施法者范围附魔后，仅在当前 Blink 作用域关闭原生再次读取执行者附魔。其他原生参数和回调不重写。已有 NativeSpellGeometryMixin 的 isShifting 作用域保留原施法者潜行，不要求其手中额外持有原生法杖。

## 结果与资源

前方分支保留原生 ledge/LOS 与 BlockFinder.SummonPosition、原生 EnderEntity 事件和 teleportTo、fall reset、实体事件 46、原生声音。随机分支保留原生地面／水／实际 randomTeleport 检查、事件、game event 与声音。原生各分支的行为并不完全相同，适配不把它们统一为一套自制传送引擎。

原生 SpellResult 不返回成功标记。MobUtil 实际返回 true 或实际前方 teleportTo 被执行后，才提交已有 Core 事务；取消事件、未成功搜索、零资源不扣费，创造免费。原生前方取消后仍可能播放声音／重置下落距离，保持原生行为，不用声音判断成功。

仅本附属的 Blink 作用域在随机候选和前方传送入口拒绝非有限、世界边界外和未加载的目标坐标。事件修改后的前方坐标也会再次验证。原生 native clip 的视线搜索仍使用自身实现，不声称所有视线查询都被替换为无区块读取的算法。验证原生 32+range 与 64+2*range 的整数算术，防止溢出；没有新加人为传送距离上限。

## 定向服务器验证

两端各四项 goety_nouveau_blink 通过：

1. 前方墙／台阶搜索与实际原生回调结果一致，fall reset，作用域前后普通原生法杖不变。FakePlayer 连接不执行真实客户端传送，测试专用 probe 用 setPos 观察原生 teleportTo 的坐标；正式代码无此替代。
2. 真正附魔的原施法者聚晶／Void Staff 范围只计算一次，原生参考回调与 Ars 友军牛落点相同，原施法者不动。
3. 实际原生实体靠近和随机传送，与相同随机种子的原生回调／helper 落点一致。Neo 初始测试未固定牛的头部朝向，因此视线夹具没有选中目标；显式设置头部方向后通过，未修改正式 native aim。
4. 敌人／方块拒绝、零资源、全部三个分支的原生事件取消、Ender Ground 事件拒绝，以及创造免费实际传送。

不重跑历史全目录。客户端实体事件／粒子和声音沿用原生，实际画面、声音听感与多人插值仍待人工检查。一次性传送不启动持续会话、不增加维护扣费或持杖移动减速。
