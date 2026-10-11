# 飞行、旋风与电击（dev.36）

固定 NeoForge Goety 3.2.0 / Forge Goety 2.5.58.4，核对实际 FlyingSpell、WhirlwindSpell、ShockingSpell、ModItems 注册供应器、MobUtil、WandUtil 和 SLightningPacket。三个聚晶分别为 flying_focus、whirlwind_focus、shocking_focus。共用既有 Core 执行会话，没有增加 Goety 专用 Core 接口。

## 角色与生命周期

效果命中自己或原生友军后启动有限的持续段。原始施法者提供法杖、装备、资源；实际执行者提供位置、朝向与运动；显式 damageOwner 保留。首次实际原生回调支付一次，此后每 20 次结果预付下一组。创造免费，空资源、失去友军关系、双方移除或时限结束停止后续回调。

正数 native shotsNumber 优先；原生无限持杖的飞行采用 channels.unboundedDurationTicks 默认 200 tick，Ars 时效调整这个基准。结束停止施法，没有自行清空原生运动速度，也没有删除原生已生效的效果。普通 Goety 持杖施法不变。

## 飞行

直接执行原生 SpellResult，设置实际接受者运动速度、离地、hurtMarked、hasImpulse、清空摔落距离并发送原生云粒子。沿接受者实时朝向飞行。NeoForge 基准速度是原生 0.5，Forge 使用原生 FlyingPower 配置；原始施法者的风法杖把基准翻倍。原生 potency/2 是整数除法，保留该行为；原始聚晶附魔替代基础 potency，然后叠加 Ars 增幅。接受者装备不再重复计入。

Goety 原生客户端持杖专用手臂 FLIGHT_POSE 不模拟，不声称 Ars 法术书拥有完全相同的手部姿势。

## 旋风

执行原生向上运动、减轻燃烧、周围生物/箭矢径向推动、风法杖反弹飞行箭矢和三层原生 windParticle。原生推开附近友军的非伤害运动也保留。

原生普通 lift=0.04，加聚晶 potency/100；Ars 增幅映射到同一 potency。下落时原生 lift=0.2 覆盖 potency，保留这一分支。原始风法杖增加范围 1，并将每 tick 减少燃烧时间从 1 提升到 4。范围先合并原始附魔与 Ars AOE，避免读接受者装备。

## 电击

原生 SpellResult 保留雷电数据包、伤害、Spasms、Burning、无名法杖颜色、风暴法杖连锁及雨雷天气分支。重定向眼睛、坐标、blockResult、实体选择和声音位置到真正执行者；伤害来源替换为显式 damageOwner，原始装备与阵营检查保留。不会自行实现一套缺少原生客户端数据包的电击粒子。

非风暴法杖的避雷针转移分支保留；沿实际执行者朝向附近搜索到避雷针时，原生电弧打向避雷针而不伤害实体。不能伤害原施法者、执行者或原施法者的友军。

## 验证与余项

定向服务器验证仅覆盖本批的真实运动/伤害、实时朝向、原始法杖、显式归属、原生灭火/下落/反箭分支、避雷针分支、持续收费、空资源、创造、友军关系和敌方接受者拒绝。实际客户端云粒子、风环、电弧与声音仍需人工观察。

Telekinesis 与 Burrowing 仍需分别处理原生可变目标状态、玩家采掘状态及结束清理，本批不据此宣布全部持续法术完成。
