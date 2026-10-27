# 附属聚晶的早／晚注册顺序：0.1.2

2026-10-28，排查用户报告 https://mclo.gs/48gxbGQ 。报告中的 Goety & Nouveau 0.1.1、Ars 5.13.3、Goety 3.2.2、Goety Cataclysm 1.8.4 在物品注册阶段出现：

```text
Adding duplicate key 'ResourceKey[minecraft:item / goety_nouveau:glyph_native/goety_cataclysm/sandstorm_focus]' to registry
```

## 原因

NativeFocusIndex在外部IFocus真正注册时生成魔符，写入Ars魔符表并立即注册其物品，以支持晚于Ars的附属注册。若聚晶事件较早，Ars的ItemsRegistry.onItemRegistry稍后遍历同一魔符表，就会再次注册该魔符物品并中断物品注册。注册表破坏后多个其他模组引用尚未完成的物品，产生unbound value等连带错误；不能据此把8项注册错误都认定为独立缺陷。

此前原生聚晶测试只让夹具在本附属的较晚物品事件注册，未覆盖早于Ars的情况。

## 修复边界

两个平台共用NativeFocusArsRegistrationMixin，只给Ars本次物品注册提供过滤后的临时魔符表。仅在“条目是本附属生成的聚晶魔符、该ID已绑定同一个物品、supplier也仍返回这个相同实例”时跳过二次插入。

不修改Ars的全局魔符表，不删除自动聚晶、配方、模型、配置或已学记录。不同实例的真实ID碰撞继续由注册表报错，不按namespace笼统吞掉异常。较晚注册的附属仍立即注册一次自己的物品。没有改Core、Goety本体、其他附属或玩法配置。

## 定向复现与验证

测试夹具在Ars自身RegisterEvent的HIGHEST监听器里注册一个IFocus，另三个IFocus仍在本附属物品事件较晚注册；实际进入两种注册顺序，而不是仅在已冻住的注册表上模拟map调用。单项GameTest确认四种魔符物品均绑定、保留原生模型引用与真实抄写台配方，Goety本体聚晶未重复生成。

未修复的0.1.1源码在这一夹具下复现同类duplicate key，无法启动GameTest世界。注意这次NeoForge开发启动器仍返回退出码0、Gradle显示成功；实际注册阶段失败，没有任何测试通过，不能用构建退出码代替测试通过数。

修复后NeoForge基线Ars5.13.1／Goety3.2.1的1项测试通过；再按报告更新隔离依赖为Ars5.13.3／Goety3.2.2／Gecko4.9.3，同一项测试再次通过。NeoForge测试使用21.1.250，报告使用21.1.264；未宣称已完整复现报告中的全部模组组合。Forge 47.4.0／Ars4.12.7／Goety2.5.59.2 的同项测试也通过，发行包已执行reobfJar。最终产物哈希见 [构建记录](native-focus-registration-build.json)。Forge本体的marsh_wood_blocks缺失标签警告未影响测试；未改本体数据。

固定Core为 `b626e92705c9c7d4ff607c7b0a782e1766558f7d`（0.1.36），受跟踪文件只读；附属起点 `77b6f0fe15c5e72c4549e54206aebceb38acdcc3`，工作区 `parallel-workspaces/goety-glyph-registration-20261028`，分支 `fix/native-glyph-registration-order`。

报告还包含Mana Sub-Zero的缺失Mixin插件和Goety Mastery of Magic的GeckoLib类兼容错误。这些不是Goety & Nouveau的注入点；修复本次重复注册后仍需报告者重启复测，不承诺整包的全部问题已解决。没有处理这些第三方模组。

未做客户端外观测试，本次不修改渲染；不上传完整用户日志或个人环境信息到仓库。未推送或上传CurseForge。

## 本地交付

实现提交 `0fa5e222a792882da68d9ee618dc8139d3d46494` 已合并主分支。0.1.2 NeoForge包已替换万法归一的0.1.1，唯一modid与哈希核对通过；旧包备份 `C:\Users\57991\Desktop\.minecraft\versions\1.21.1-万法归一\codex-backups\goety-registration-order-20261028-030758\mods\goety-nouveau-0.1.1-mc1.21.1-neoforge.jar`。其他189个模组JAR的名称和哈希未改，Core与玩法配置未动。Forge发行包已完成重混淆，未替换Forge人工测试实例。

两端二进制、源码包、复现与定向测试日志及部署收据位于 `C:\Users\57991\Documents\MinecraftDev\releases\goety-registration-order-20261028`。未推送或上传CF。
