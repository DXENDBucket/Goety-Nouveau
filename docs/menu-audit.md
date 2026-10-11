# 随身工作台与末影箱（dev.49）

对照固定依赖中 Ars EffectCraft／EffectEnderChest 和 Goety CraftingSpell／EnderChestSpell。Ars 工作台接受普通触发，末影箱使用实体触发；两者都为施法者本人打开界面，并排除 FakePlayer。Goety 原生工作台使用 CraftingFocusMenu，保留实际 CraftingMenu 槽位、配方更新、关闭返还及自己的客户端画面；原生末影箱使用 ChestMenu.threeRows 和真实 PlayerEnderChestContainer，并播放原生打开音效。

本作增加两个效果魔符，直接调用实际 Goety SpellResult，不复制容器、客户端界面、物品存储或配方处理。工作台接受方块或实体命中；末影箱只接受实体命中（通常自身→末影箱）。无论选中的实体、executor、damageOwner 是谁，容器始终属于原始 caster。怪物、FakePlayer、旁观玩家、失效／移除／不同世界来源不打开菜单。该语义不改变原生 Ars 或原生 Goety 的施法入口。

继续使用原本的 Core 触发事务和 Goety 资源配置。菜单真正打开、实际 player.containerMenu 更换且类型正确，才提交一次费用；原生打开被拒绝则不付费。资源不足不调用原生回调，创造模式免费。两个聚晶的默认费用、吟唱与冷却继续由原生 Goety 配置和现有准备／冷却适配读取；魔符本身没有额外 Ars 费用。没有新增增强魔符兼容。

针对性 GameTest 只运行 goety_nouveau_menus，使用真实 ServerPlayer 和原生容器；测试连接捕获真实 ClientboundOpenScreenPacket，避免 FakePlayer 原生拒绝导致假阳性。验证工作台在无实体工作台处保持有效、关闭后归还输入；末影箱读取与修改真实持久容器、选中另一玩家及改变 executor／归属也不泄露他人容器；普通 Goety 回调不变；缺资源、无效触发、旁观、假玩家／生物、原生打开被拒绝、创造免费。

测试会话的拒绝打开玩家只用于测试；生产代码不拦截 openMenu，不修改原生容器行为。服务器状态与发包验证不能代替客户端人工观察，但渲染及界面均复用依赖现有注册，不添加新画面或网络协议。

2026-10-11：两端各三项实际用例通过，日志分别为 build/menu-tests.log、forge/build/menu-tests.log，均有 All 3 required tests passed 与 BUILD SUCCESSFUL。Neo 同时加载现有 Iron／NEG；只测试本批新增菜单行为。发布产物检查两端各 124 个魔符模型、中文／英文文本、原生 parent 资源、Java 21／17、Core 依赖、源码及 MIT／第三方声明；不包含测试或依赖本体。
