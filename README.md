# AI 模组开发脚手架

这是一个用于 AI 辅助开发 Minecraft 模组的 NeoForge 示例工程，对应仓库的 `master` 分支。

`master` 不是面向玩家发布的单一主题成品模组，而是保留了多组可运行示例，方便继续让 AI 协助开发和验证功能。

## 模组信息

| 项目 | 内容 |
| --- | --- |
| 显示名称 | `【懒人包】AI模组开发脚手架` |
| Mod ID | `aimod` |
| 版本 | `1.0.0` |
| Minecraft | `1.21.10` |
| NeoForge | `21.10.64` |
| Java | `21` |
| 构建产物 | `aimod-1.0.0.jar` |

## 当前示例功能

- 基础示例方块和示例物品注册，以及独立的创造模式物品栏。
- 番茄菜肴、巧克力食品和多种水果食物。
- 带有附魔光效的“十万年魂环”物品。
- 站在制箭台上时自动生成箭矢。
- 农民村民提供巧克力食品交易。
- 特殊物品掉落后会快速消失，部分指定物品例外。
- 配置文件示例：`config/aimod-common.toml`。

这些功能用于演示注册、食物属性、事件监听、村民交易、配置和自定义渲染等 NeoForge 开发方式，仍可继续扩展。

## 构建

需要 Java 21，在项目根目录执行：

```bash
./gradlew build
```

构建完成后，JAR 位于 `build/libs/aimod-1.0.0.jar`。

## 文档

- [制箭台生成箭矢](docs/制箭台生成箭矢.md)
- [十万年孤竹魂环](docs/十万年孤竹魂环.md)
- [测试指南](TESTING_GUIDE.md)

## 源码

[Neoforge-Lazypack · master 分支](https://github.com/xingduansuzhao/Neoforge-Lazypack/tree/master)
