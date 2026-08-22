# AGENTS.md

本项目是 NeoForge 1.21.1 Minecraft 模组。

## 项目概述

- 模组ID：`aimod`
- 使用 GeckoLib 实现第一人称武器动画
- 使用 Player Animation Library (PAL) 实现第三人称玩家动画
- 武器系统基于 `AnimatedWeaponItem` 基类

## 音效处理

用户提供的 `.ogg` 音效文件通常是 OGG 容器内 FLAC 编码，Minecraft 只支持 OGG Vorbis。需要用 ffmpeg 转换：

```bash
ffmpeg -y -i "源文件.ogg" -c:a vorbis -ar 44100 -strict -2 "目标.ogg"
```

注意：当前环境的 ffmpeg 没有 `libvorbis`，只有原生 `vorbis` 编码器，且只支持立体声（stereo），不支持 `-ac 1` 单声道。

## 武器渲染架构

每把武器可能涉及多个 Blockbench 导出：

1. **GeckoLib Item 项目** → 第一人称模型、动画、display 设置
2. **Player/Steve 动画项目** → PAL 第三人称玩家骨骼动画
3. **Java Block/Item 项目** → 第三人称物品显示修正

不是所有武器都需要全部三个。鹰爪（karambit）是唯一的双持武器，不使用第三人称 Java 模型或 PAL。

## 资源布局

```
assets/aimod/items/<weapon>.json                          # 物品模型调度
assets/aimod/models/item/<weapon>.json                    # GeckoLib display 基础模型
assets/aimod/models/item/<weapon>_third_person.json       # 第三人称 Java 模型（如有）
assets/aimod/models/item/<weapon>_gui.json                # GUI 平面模型（如有）
assets/aimod/geckolib/models/item/<weapon>.geo.json       # GeckoLib 几何模型
assets/aimod/geckolib/animations/item/<weapon>.animation.json  # 第一人称武器动画
assets/aimod/player_animations/<weapon>_animations.json   # PAL 玩家动画（如有）
assets/aimod/textures/item/<weapon>.png                   # 贴图
assets/aimod/textures/item/<weapon>_gui.png               # GUI裁剪贴图（如物品使用独立 _gui 模型）
assets/aimod/sounds/item/<weapon>/switch.ogg              # 切换音效
assets/aimod/sounds/item/<weapon>/heavy_attack.ogg        # 重击音效
assets/aimod/sounds/item/<weapon>/light_attack_1.ogg      # 轻击音效1
assets/aimod/sounds/item/<weapon>/light_attack_2.ogg      # 轻击音效2
```

## 擎天发光效果

擎天的发光是客户端运行时效果，不依赖世界时间，并分为核心自发光、外部光晕和客户端动态环境光：

- `src/main/java/com/xingduansuzhao/aimod/AiModClient.java` 为擎天创建 `QingtianRenderer`。
- `QingtianRenderer` 注册 `QingtianHaloLayer` 和 `QingtianEmissiveLayer`。核心层虽然继承 GeckoLib 的 `AutoGlowingGeoLayer` 以复用遮罩流程，但必须覆写为原版 `RenderType.eyes(GLOW_MASK)`，让 Iris/Sodium 开启光影时走全亮实体路径；不要恢复为 GeckoLib 自定义 `geckolib_emissive` 管线。
- `src/main/resources/assets/yaoniming3000/textures/item/qingtian_glowmask.png` 是核心发光遮罩，`qingtian_glowhalo.png` 是外部光晕遮罩。透明像素不渲染，非透明像素使用满亮度材质。
- `QingtianGlowController` 使用 `Util.getMillis()` 做约 1.8 秒周期的呼吸灯变化，核心、光晕和动态环境光同步变化。不要加入 `isMoonVisible()`、昼夜时间或世界亮度判断。

发光遮罩是已经生成并提交的静态资源。当前 `build.gradle` 不负责生成它，也不应为此加入图片处理业务逻辑。`qingtian_glow_areas.png` 只是过去用于标记发光像素的编辑辅助图，不参与运行时；如果不再维护遮罩生成流程，可以删除它。

修改擎天基础贴图或发光区域后，需要使用图像编辑工具直接更新 `qingtian_glowmask.png`，并确保最终资源路径仍为 `assets/yaoniming3000/textures/item/qingtian_glowmask.png`。删除遮罩、改错命名空间，或移除 `QingtianEmissiveLayer` 的注册，都会使擎天不发光。

## 物品模型调度（items/*.json）

使用 `minecraft:display_context` 可以为不同渲染上下文分配不同模型：

- `gui` → 可用平面图标或 GeckoLib
- `thirdperson_righthand` / `thirdperson_lefthand` → 用 Java Block/Item 模型避免漂移
- 其余 → GeckoLib special renderer

如果武器在第三人称不漂移，可以全部用 GeckoLib。

## 添加新武器音效的步骤

1. 将源 ogg 通过 ffmpeg 转为 OGG Vorbis，放入 `sounds/item/<weapon>/`
2. 在 `sounds.json` 添加对应条目
3. 在 `AiMod.java` 注册 `SoundEvent`
4. 在武器类的 `super()` 构造中传入音效引用

## 鹰爪（Karambit）特殊机制

鹰爪是唯一的双持武器（`rendersPairedOffhand() = true`）：
- 服务端每 tick 将主手鹰爪镜像复制到副手
- 客户端副手渲染由主手渲染事件统一绘制，副手独立渲染事件被取消
- 使用手别专属动画触发器：`switch_main`/`switch_off`、`heavy_attack_main`/`heavy_attack_off`

## 轻击系统

- 所有武器的轻击有冷却锁（防止疯狂点击打断）
- 连招窗口机制：快速连点触发 attack1→attack2 交替，慢点则每次都从 attack1 开始
- 军用手斧（combat_axe）有 `lightAttackAnimationsEnabled = true` + `suppressVanillaLightSwing = true`

## 构建

```bash
./gradlew build
```
