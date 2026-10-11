# Forge 震击接入核对（dev.48）

固定 Forge Goety 2.5.58.4 的实际 TEMBLOR_CLAP_FOCUS／TemblorClapSpell。Neo Goety 3.2.0 没有此注册聚晶，不能用名相近的未注册 WarStomp 冒充；Neo 不构造该魔符，不打包其模型或平台 Mixin。

使用完整原生 SpellResult。在短暂同步作用域内，把此回调的位置／方块／粒子坐标读取改为 Ars 实际选中的实体或地点；原施法者的身体不移动。原生 findGroundY、地面颜色、SmashParticleOption、随机伤害、原生匿名 SpellExplosion 与三秒 STUNNED、Redstone 爆炸音效保留。作用域外的法杖仍从自身位置执行。

原始 WandUtil stats、聚晶附魔、onGround 加半径、Geomancy Staff 加半径均由原生回调处理；Ars Amplify／Dampen 修改 stats potency，AOE 修改 stats radius，各只加一次。不新增持续频道、维护扣费或独立眩晕计时。一次合法原生震波付费一次，即使范围中没有敌人也已经产生真实区域冲击和粒子；零资源不调用回调，创造免费。

显式 damageOwner 改写原生 mobAttack 的归属而保持伤害类型。原生 SpellExplosion 以 DamageSource 实体判断队友，直接换归属会误伤原来源队友。因此仅本次附属作用域在实际匿名 explodeHurt 之前补充原来源与显式 owner 的友军排除；其父类伤害／击退与后续眩晕一起排除。普通原生法杖无该作用域，不改变原规则。

入口拒绝移除／跨维度实体、非有限位置或参数、越界／未加载地点，以及非正半径。模型引用实际 goety:item/temblor_clap_focus，而不复制上游资产；发布源包与二进制均保留原生 MIT 声明。

验证：Forge 四项实际服务器用例覆盖 damage、60 tick Stunned、真实 mob_attack 归属和队友保护；同一随机种子的完整原生回调对照，验证 grounded／staff／Ars 参数与击退；资源、创造、实体／方块、未加载／越界拒绝；附属范围结束后的普通法杖隔离。Neo 一项验证不存在的聚晶不进入魔符列表／法术学派，Breeze 仍存在。

另在两端运行时遍历真实 IFocus.getSpell，确认各 124 原生聚晶物品，未覆盖项只有七个物品界面／带绑定数据的控制入口。此计数修正此前仅解析静态 ModItems 的局部清点；不是从名称推测法术。

客户端粒子资产与声音调用入口沿用原生；服务器不证明人工观看后的外观、听感和多人同步质量。没有重跑历史全目录。
