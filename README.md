# 要你命三千

CF 风格的 Minecraft 武器与动画模组。

本分支对应模组 **要你命三千**，适用于 Minecraft 1.21.10、NeoForge 21.10.64。

## 模组信息

| 项目 | 内容 |
| --- | --- |
| Mod ID | `yaoniming3000` |
| 版本 | `1.0.0` |
| Minecraft | `1.21.10` |
| NeoForge | `21.10.64` |
| Java | `21` |
| 构建产物 | `yaoniming3000-1.0.0.jar` |

## 内容

- CF 风格的近战武器与专属第一人称动画。
- 使用 GeckoLib 渲染武器模型和动画。
- 使用 Player Animation Library 支持第三人称玩家动画。
- 包含擎天、军用铁锹及其变体、拳套、激光短刃、警棍、收割者、惨叫鸡、尼泊尔军刀、马来剑、棒球棒、小刀、军用手斧、扳手和鹰爪等武器。
- 擎天支持武器变换和恢复；连杀系统提供连杀提示与音效。
- 擎天的红色发光区域使用 `qingtian_glowmask.png` 核心遮罩和 `qingtian_glowhalo.png` 光晕遮罩，使用兼容 Iris 光影的原版 `RenderType.eyes` 全亮路径，并带有全天候呼吸灯变化。
- 鹰爪支持双持，并使用手别专属动画。

## 依赖

- NeoForge `21.10.64` 或兼容版本
- Minecraft `1.21.10`
- GeckoLib `5.3-alpha-3` 或更高兼容版本
- Player Animation Library `1.1.3+mc.1.21.9` 或更高兼容版本

## 构建

需要 Java 21，在项目根目录执行：

```bash
./gradlew build
```

构建完成后，JAR 位于 `build/libs/yaoniming3000-1.0.0.jar`。

擎天发光资源位于 `src/main/resources/assets/yaoniming3000/textures/item/qingtian_glowmask.png` 和 `qingtian_glowhalo.png`，属于运行时资源。`build.gradle` 不生成这些文件；修改发光区域时应直接编辑并替换对应 PNG。

## 源码

[Neoforge-Lazypack · cf 分支](https://github.com/xingduansuzhao/Neoforge-Lazypack/tree/cf)
