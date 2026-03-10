# 功能一：十万年孤竹魂环 —— 超详细实现 Prompt

## 一、功能概述

在 NeoForge 模组中实现以下效果：

1. **原版竹子更名为"十万年孤竹"**（含竹笋/竹子苗）
2. **破坏竹子不再掉落竹子物品**，而是掉落一个自定义物品——"魂环"
3. **魂环掉落物的 3D 形态**：平躺在地面（与地面水平），厚度极薄，像一个躺在地上的光盘/戒指
4. **竹子免疫火烧**：火焰无法点燃竹子
5. **魂环有附魔光效**：手持、地面掉落物、GUI 中都显示附魔闪光

---

## 二、项目环境（必须严格匹配）

| 项目 | 值 |
|---|---|
| Minecraft 版本 | **1.21.10** |
| NeoForge 版本 | **21.10.64** |
| Mod Loader | **NeoForge**（不是 Forge！不是 Fabric！） |
| Java 版本 | **21** |
| Mod ID | `aimod` |
| 主包名 | `com.xingduansuzhao.aimod` |
| 构建工具 | Gradle + `net.neoforged.moddev` 插件 |
| Mapping | Parchment (`1.21.10` / `2025.10.12`) |

> **重要**：NeoForge 1.21.10 的 API 与旧版 Forge/NeoForge 有显著区别。请确保所有 import、事件类名、注解方式都匹配 NeoForge 21.10.x。

---

## 三、文件结构

实现此功能需要创建/修改以下文件：

```
src/main/java/com/xingduansuzhao/aimod/
├── AiMod.java                          # [修改] 注册物品、设置竹子防火
├── AiModClient.java                    # [修改] 注册自定义渲染器
├── spiritring/
│   ├── SpiritRingItem.java             # [新建] 魂环物品类
│   ├── BambooDropHandler.java          # [新建] 竹子掉落物替换逻辑
│   └── client/
│       ├── SpiritRingItemEntityRenderer.java   # [新建] 魂环掉落物自定义渲染
│       └── SpiritRingRenderState.java          # [新建] 渲染状态辅助类
src/main/resources/
├── assets/aimod/
│   ├── items/spirit_ring.json          # [新建] 物品模型引用
│   ├── models/item/spirit_ring.json    # [新建] 物品模型定义
│   ├── textures/item/spirit_ring.png   # [新建] 魂环材质贴图（需自备）
│   └── lang/
│       ├── en_us.json                  # [修改] 英文翻译
│       └── zh_cn.json                  # [修改] 中文翻译
```

---

## 四、逐文件详细实现

### 4.1 SpiritRingItem.java

**路径**：`src/main/java/com/xingduansuzhao/aimod/spiritring/SpiritRingItem.java`

**功能**：自定义魂环物品，覆写 `isFoil` 使其始终显示附魔光效。

```java
package com.xingduansuzhao.aimod.spiritring;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class SpiritRingItem extends Item {

    public SpiritRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true; // 始终显示附魔光效
    }
}
```

**要点**：
- 继承 `net.minecraft.world.item.Item`
- 构造函数接收 `Item.Properties` 参数
- `isFoil(ItemStack)` 返回 `true` = 始终附魔光效（不需要真的附魔）

---

### 4.2 BambooDropHandler.java

**路径**：`src/main/java/com/xingduansuzhao/aimod/spiritring/BambooDropHandler.java`

**功能**：
1. 拦截竹子方块的掉落物事件，取消原版竹子掉落
2. 拦截竹子方块被破坏事件，生成魂环掉落物
3. 竹子是多节的，连锁破坏会快速触发多次事件，需要用冷却机制保证每次破坏只掉一个魂环

```java
package com.xingduansuzhao.aimod.spiritring;

import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = AiMod.MODID)
public class BambooDropHandler {

    // 玩家冷却 Map，防止竹子连锁破坏时重复掉落
    private static final Map<UUID, Long> recentBreaks = new HashMap<>();
    private static final long COOLDOWN_TICKS = 5L; // 5 tick 冷却

    /**
     * 取消竹子的所有原版掉落物（BlockDropsEvent）
     * 这样破坏竹子不会掉竹子物品了
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getState().getBlock() instanceof BambooStalkBlock)) {
            return;
        }
        event.setCanceled(true); // 取消所有掉落物
    }

    /**
     * 当竹子被破坏时，生成一个魂环掉落物
     * 使用冷却机制避免竹子连锁破坏导致重复掉落
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getState().getBlock() instanceof BambooStalkBlock)) {
            return;
        }

        Player player = event.getPlayer();
        UUID playerId = player.getUUID();
        long currentTick = serverLevel.getGameTime();

        // 冷却检测
        Long lastBreakTick = recentBreaks.get(playerId);
        if (lastBreakTick != null && (currentTick - lastBreakTick) < COOLDOWN_TICKS) {
            return; // 冷却中，不重复掉落
        }
        recentBreaks.put(playerId, currentTick);

        // 在方块位置中心生成魂环
        BlockPos pos = event.getPos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ItemStack spiritRingStack = new ItemStack(AiMod.SPIRIT_RING.get(), 1);
        ItemEntity itemEntity = new ItemEntity(serverLevel, x, y, z, spiritRingStack);
        itemEntity.setPickUpDelay(10); // 10 tick 后才能拾取
        serverLevel.addFreshEntity(itemEntity);
    }
}
```

**关键要点**：
- `BlockDropsEvent`：NeoForge 事件，在方块掉落物生成时触发。`setCanceled(true)` 取消所有掉落物。
- `BlockEvent.BreakEvent`：NeoForge 事件，在方块被玩家破坏时触发。
- `BambooStalkBlock`：原版竹子方块的类（不是 `BambooBlock`，在 1.21.10 中竹子茎用的是 `BambooStalkBlock`）。
- 冷却机制：竹子是多段结构，破坏底部会导致上面的竹节快速连锁断裂，每次断裂都触发事件。用 `Map<UUID, Long>` 记录玩家上次掉落时间，5 tick 内不重复掉落。
- `@EventBusSubscriber(modid = AiMod.MODID)` 自动注册到 **NeoForge 游戏事件总线**（不是 MOD 事件总线）。

---

### 4.3 SpiritRingRenderState.java

**路径**：`src/main/java/com/xingduansuzhao/aimod/spiritring/client/SpiritRingRenderState.java`

**功能**：扩展 `ItemEntityRenderState`，添加魂环专用的渲染状态字段。

```java
package com.xingduansuzhao.aimod.spiritring.client;

import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;

public class SpiritRingRenderState extends ItemEntityRenderState {
    public boolean isSpiritRing = false;  // 标记是否为魂环物品
    public float savedAge;                 // 保存动画用的年龄
    public float savedBob;                 // 保存上下浮动偏移
}
```

**要点**：
- 继承 `ItemEntityRenderState`（NeoForge 1.21.10 中物品实体渲染使用状态对象模式）
- 这是渲染管线需要的中间数据载体

---

### 4.4 SpiritRingItemEntityRenderer.java（客户端渲染核心）

**路径**：`src/main/java/com/xingduansuzhao/aimod/spiritring/client/SpiritRingItemEntityRenderer.java`

**功能**：自定义 ItemEntity（掉落物实体）的渲染方式，只对魂环物品生效，让它平躺在地面上、极薄、旋转漂浮。

```java
package com.xingduansuzhao.aimod.spiritring.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;

public class SpiritRingItemEntityRenderer extends ItemEntityRenderer {

    private final RandomSource random = RandomSource.create();

    public SpiritRingItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    /**
     * 覆写创建渲染状态，返回我们自定义的 SpiritRingRenderState
     */
    @Override
    public SpiritRingRenderState createRenderState() {
        return new SpiritRingRenderState();
    }

    /**
     * 从实体中提取渲染数据到状态对象
     * 如果是魂环，额外保存 age 和 bob 信息
     */
    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof SpiritRingRenderState spiritState) {
            spiritState.isSpiritRing = entity.getItem().is(AiMod.SPIRIT_RING.get());
            if (spiritState.isSpiritRing) {
                spiritState.savedAge = state.ageInTicks;
                spiritState.savedBob = state.bobOffset;
            }
        }
    }

    /**
     * 实际渲染逻辑
     * 对魂环做特殊变换：平躺 + 极薄 + 旋转 + 浮动
     * 对非魂环物品调用原版渲染
     */
    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state instanceof SpiritRingRenderState spiritState && spiritState.isSpiritRing) {
            if (state.item.isEmpty()) {
                return;
            }

            poseStack.pushPose();

            // 上下浮动动画（bob）
            float bob = Mth.sin(spiritState.savedAge / 10.0f + spiritState.savedBob) * 0.2f + 0.1f;
            poseStack.translate(0.0, bob, 0.0);

            // 绕 Y 轴旋转（自转）
            float spin = spiritState.savedAge * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));

            // 关键：绕 X 轴旋转 90°，让原本竖直的物品贴图变成水平（平躺）
            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            // 缩放：XY 方向放大（环的面积），Z 方向极度压缩（厚度极薄）
            float ringScale = 7.0f;       // 环的整体大小
            float thicknessScale = 0.15f;  // 厚度（越小越薄）
            poseStack.scale(ringScale, ringScale, thicknessScale);

            // 微调位置居中
            poseStack.translate(0.0, -0.125, 0.0);

            // 调用原版的多层渲染
            this.random.setSeed(state.seed);
            renderMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random);

            poseStack.popPose();
        } else {
            // 非魂环物品使用原版渲染
            super.submit(state, poseStack, collector, cameraState);
        }
    }
}
```

**渲染变换详解**：

1. **`poseStack.translate(0, bob, 0)`**：上下浮动，让魂环看起来像悬浮在空中
2. **`Axis.YP.rotationDegrees(spin)`**：绕 Y 轴自转，让魂环持续旋转
3. **`Axis.XP.rotationDegrees(90)`**：**这是让魂环平躺的关键**！原版掉落物是竖直显示的，旋转 X 轴 90° 后变成水平
4. **`poseStack.scale(7.0f, 7.0f, 0.15f)`**：XY 放大让环变大，Z 极度缩小让环变薄
5. **`renderMultipleFromCount`**：调用父类的渲染方法，处理物品堆叠时的多层渲染

**重要 API 说明（NeoForge 1.21.10 特有）**：
- 渲染方法签名是 `submit(ItemEntityRenderState, PoseStack, SubmitNodeCollector, CameraRenderState)`，不是旧版的 `render`
- 使用 `SubmitNodeCollector` 而非 `MultiBufferSource`
- 使用 `CameraRenderState` 而非 `float partialTick`
- `state.lightCoords` 替代了旧版的 `packedLight`

---

### 4.5 AiMod.java（主类修改）

**路径**：`src/main/java/com/xingduansuzhao/aimod/AiMod.java`

需要添加的内容：

#### 4.5.1 注册魂环物品

```java
import com.xingduansuzhao.aimod.spiritring.SpiritRingItem;

// 在类的静态字段区域添加：
public static final DeferredItem<Item> SPIRIT_RING = ITEMS.registerItem("spirit_ring", SpiritRingItem::new);
```

**解释**：
- `ITEMS.registerItem("spirit_ring", SpiritRingItem::new)` 注册物品到游戏注册表
- 注册 ID 为 `aimod:spirit_ring`
- 使用方法引用 `SpiritRingItem::new`，NeoForge 会自动传入 `Item.Properties`

#### 4.5.2 将魂环加入创造模式标签页

在 `EXAMPLE_TAB` 的 `displayItems` lambda 中添加：
```java
output.accept(SPIRIT_RING.get());
```

#### 4.5.3 设置竹子防火

在 `commonSetup` 方法中添加（必须在 `enqueueWork` 中执行，因为修改的是非线程安全的全局状态）：

```java
private void commonSetup(FMLCommonSetupEvent event) {
    // ... 其他代码 ...
    event.enqueueWork(() -> {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;
        fireBlock.setFlammable(Blocks.BAMBOO, 0, 0);         // 竹子茎
        fireBlock.setFlammable(Blocks.BAMBOO_SAPLING, 0, 0); // 竹子苗
    });
}
```

**解释**：
- `FireBlock.setFlammable(block, encouragement, flammability)`
- `encouragement = 0`：火焰不会主动蔓延到此方块
- `flammability = 0`：此方块不会被火焰点燃/烧毁
- 必须用 `enqueueWork` 包裹，保证在主线程执行

需要的 import：
```java
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
```

---

### 4.6 AiModClient.java（客户端类修改）

**路径**：`src/main/java/com/xingduansuzhao/aimod/AiModClient.java`

注册自定义的 ItemEntity 渲染器：

```java
package com.xingduansuzhao.aimod;

import com.xingduansuzhao.aimod.spiritring.client.SpiritRingItemEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = AiMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AiMod.MODID, value = Dist.CLIENT)
public class AiModClient {
    public AiModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AiMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        AiMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // 替换原版 ItemEntity 的渲染器为我们的自定义版本
        event.registerEntityRenderer(EntityType.ITEM, SpiritRingItemEntityRenderer::new);
    }
}
```

**关键说明**：
- `event.registerEntityRenderer(EntityType.ITEM, ...)` 替换的是 **所有** ItemEntity（掉落物）的渲染器
- 但我们的渲染器内部会判断：只有魂环才走自定义逻辑，其他物品走 `super.submit()`（原版逻辑）
- `@EventBusSubscriber` 不需要指定 `bus = EventBusSubscriber.Bus.MOD`，因为在 NeoForge 1.21.10 中，`@Mod` + `@EventBusSubscriber` 的静态方法默认注册到 MOD 事件总线
- **注意**：`EntityRenderersEvent.RegisterRenderers` 是 MOD 事件总线上的事件

---

### 4.7 资源文件

#### 4.7.1 物品模型引用 — `assets/aimod/items/spirit_ring.json`

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "aimod:item/spirit_ring"
  }
}
```

> 这是 1.21.10 的新格式，`items/` 目录下的 JSON 引用 `models/item/` 下的模型。

#### 4.7.2 物品模型定义 — `assets/aimod/models/item/spirit_ring.json`

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "aimod:item/spirit_ring"
  },
  "display": {
    "thirdperson_righthand": {
      "rotation": [0, 0, 0],
      "translation": [0, 3, 1],
      "scale": [0.55, 0.55, 0.05]
    },
    "thirdperson_lefthand": {
      "rotation": [0, 0, 0],
      "translation": [0, 3, 1],
      "scale": [0.55, 0.55, 0.05]
    },
    "firstperson_righthand": {
      "rotation": [0, -90, 25],
      "translation": [1.13, 3.2, 1.13],
      "scale": [0.68, 0.68, 0.05]
    },
    "firstperson_lefthand": {
      "rotation": [0, 90, -25],
      "translation": [1.13, 3.2, 1.13],
      "scale": [0.68, 0.68, 0.05]
    },
    "ground": {
      "rotation": [0, 0, 0],
      "translation": [0, 2, 0],
      "scale": [0.5, 0.5, 0.05]
    },
    "gui": {
      "rotation": [0, 0, 0],
      "translation": [0, 0, 0],
      "scale": [1, 1, 1]
    },
    "fixed": {
      "rotation": [0, 0, 0],
      "translation": [0, 0, 0],
      "scale": [1, 1, 0.05]
    }
  }
}
```

**display 说明**：
- `ground` 中 Z 轴 scale 为 `0.05`：掉落在地上时极薄
- `thirdperson` 中 Z 轴 scale 也为 `0.05`：第三人称手持时极薄
- `gui` 中 scale 为 `[1,1,1]`：背包/GUI 中正常显示
- 这些 display 设置配合渲染器的变换一起工作

#### 4.7.3 魂环贴图 — `assets/aimod/textures/item/spirit_ring.png`

需要一张 **透明背景** 的魂环图片（如红色环形图案），建议 64x64 或 128x128 像素。放置在此路径。

#### 4.7.4 语言文件

**`assets/aimod/lang/zh_cn.json`**（中文）：
```json
{
  "item.aimod.spirit_ring": "十万年魂环",
  "block.minecraft.bamboo": "十万年孤竹",
  "block.minecraft.bamboo_sapling": "十万年孤竹苗",
  "item.minecraft.bamboo": "十万年孤竹"
}
```

**`assets/aimod/lang/en_us.json`**（英文）：
```json
{
  "item.aimod.spirit_ring": "Spirit Ring",
  "block.minecraft.bamboo": "100,000-Year Lone Bamboo",
  "block.minecraft.bamboo_sapling": "100,000-Year Lone Bamboo Sapling",
  "item.minecraft.bamboo": "100,000-Year Lone Bamboo"
}
```

**说明**：
- `block.minecraft.bamboo` / `block.minecraft.bamboo_sapling` / `item.minecraft.bamboo` 是**覆写原版翻译键**，不需要修改原版代码，只需在你的语言文件中定义同名 key 即可覆盖

---

## 五、事件总线说明（极易出错）

NeoForge 有两条事件总线，用错了事件不会触发：

| 总线 | 注册方式 | 典型事件 |
|---|---|---|
| **MOD 事件总线** | `@EventBusSubscriber` 在 `@Mod` 类上 或 `modEventBus.addListener()` | `FMLCommonSetupEvent`, `EntityRenderersEvent`, `BuildCreativeModeTabContentsEvent` |
| **NeoForge 游戏事件总线** | `@EventBusSubscriber(modid=...)` 或 `NeoForge.EVENT_BUS.register()` | `BlockDropsEvent`, `BlockEvent.BreakEvent`, `ServerTickEvent`, `PlayerEvent` |

- `BambooDropHandler` 的 `BlockDropsEvent` 和 `BlockEvent.BreakEvent` 属于**游戏事件总线**
- `SpiritRingItemEntityRenderer` 的注册通过 `EntityRenderersEvent.RegisterRenderers` 属于 **MOD 事件总线**

---

## 六、常见错误排查

1. **魂环不掉落**：检查 `BambooDropHandler` 的 `@EventBusSubscriber` 是否正确注册。方法必须是 `public static`。
2. **竹子仍然掉竹子**：确认 `BlockDropsEvent` 的 `setCanceled(true)` 生效。确认判断的是 `BambooStalkBlock` 而不是其他类。
3. **渲染不生效**：确认 `AiModClient` 的 `@EventBusSubscriber` 在客户端注解了 `value = Dist.CLIENT`。确认 `EntityType.ITEM` 的渲染器被正确替换。
4. **火能烧竹子**：确认 `setFlammable` 调用在 `enqueueWork` 内，且参数都是 `0, 0`。
5. **附魔光效不显示**：确认 `SpiritRingItem.isFoil()` 返回 `true`。
6. **物品无贴图（紫黑方块）**：检查 `items/spirit_ring.json`、`models/item/spirit_ring.json` 和 `textures/item/spirit_ring.png` 路径是否正确。
7. **编译错误 - 找不到 SubmitNodeCollector**：确认使用的是 NeoForge 21.10.x，旧版本使用 `MultiBufferSource`。

---

## 七、完整依赖关系图

```
AiMod.java
  ├── 注册 SPIRIT_RING 物品 → SpiritRingItem.java
  ├── commonSetup → FireBlock.setFlammable（竹子防火）
  └── 引用 FletchingArrowGenerator（另一个功能，忽略）

AiModClient.java
  └── onRegisterRenderers → SpiritRingItemEntityRenderer.java
                              └── 使用 SpiritRingRenderState.java

BambooDropHandler.java
  ├── onBlockDrops → 取消竹子原版掉落
  └── onBlockBreak → 生成 SPIRIT_RING 物品实体

资源文件
  ├── items/spirit_ring.json → models/item/spirit_ring.json → textures/item/spirit_ring.png
  └── lang/zh_cn.json + lang/en_us.json（翻译覆盖）
```

---

## 八、测试验证清单

- [ ] 进入游戏，竹子名称显示为"十万年孤竹"
- [ ] 竹子苗名称显示为"十万年孤竹苗"
- [ ] 破坏竹子，不掉落原版竹子物品
- [ ] 破坏竹子，掉落一个魂环
- [ ] 魂环掉落物平躺在地面，与地面水平
- [ ] 魂环掉落物持续旋转并上下浮动
- [ ] 魂环掉落物有附魔光效
- [ ] 拾取魂环后，背包中显示附魔光效
- [ ] 在竹子旁边放火，竹子不被烧毁
- [ ] 用打火石点燃竹子，火焰不会蔓延到竹子上
- [ ] 其他物品的掉落物渲染不受影响（正常竖直显示）
