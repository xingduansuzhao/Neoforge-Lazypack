# 竹知了

一个以竹知了为主题的 Minecraft 小型乐器模组。

本分支对应模组 **竹知了**，适用于 Minecraft 1.21.10、NeoForge 21.10.64。

## 模组信息

| 项目 | 内容 |
| --- | --- |
| Mod ID | `bamboo_cicada` |
| 版本 | `1.0.0` |
| Minecraft | `1.21.10` |
| NeoForge | `21.10.64` |
| Java | `21` |
| 构建产物 | `bamboo_cicada-1.0.0.jar` |

## 内容

- 添加可手持的竹知了物品，以及用于交易和显示数量的特殊绿宝石。
- 右键使用竹知了可以开始或停止演奏循环音效。
- 使用 GeckoLib 驱动竹知了的第一人称模型和循环动画。
- 客户端与服务端同步演奏状态，切换物品、退出游戏和玩家克隆时会正确停止音效。
- 工具匠可以使用特殊绿宝石交易竹知了。
- 提供中文简体、中文繁体和英文翻译。

## 依赖

- NeoForge `21.10.64` 或兼容版本
- Minecraft `1.21.10`
- GeckoLib `5.3-alpha-3` 或更高兼容版本

## 构建

需要 Java 21，在项目根目录执行：

```bash
./gradlew build
```

构建完成后，JAR 位于 `build/libs/bamboo_cicada-1.0.0.jar`。

## 源码

[Neoforge-Lazypack · bamboo-cicada 分支](https://github.com/xingduansuzhao/Neoforge-Lazypack/tree/bamboo-cicada)
