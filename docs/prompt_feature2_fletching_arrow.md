# 功能二：制箭台自动生成箭矢 —— 超详细实现 Prompt

## 一、功能概述

在 NeoForge 模组中实现以下效果：

1. **玩家站在制箭台（Fletching Table）上时**，制箭台持续生成箭矢掉落物
2. **箭矢跟随玩家位置生成**，在玩家脚下附近出现，立即可拾取
3. **生成速率**：每秒约 10 支箭矢（每 100ms 生成一支）
4. **箭矢不会残留在制箭台上**：因为箭矢直接生成在玩家身上，拾取延迟为 0，玩家离开时不会有残留
5. **玩家退出游戏时清理数据**：防止内存泄漏

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
├── AiMod.java                          # [修改] 注册玩家退出清理逻辑
└── fletching/
    └── FletchingArrowGenerator.java    # [新建] 制箭台箭矢生成核心逻辑
```

此功能不需要任何资源文件（不需要模型、贴图、语言文件），因为使用的是原版箭矢物品。

---

## 四、逐文件详细实现

### 4.1 FletchingArrowGenerator.java（核心逻辑）

**路径**：`src/main/java/com/xingduansuzhao/aimod/fletching/FletchingArrowGenerator.java`

**功能**：
- 每个服务端 tick 检查所有在线玩家
- 如果玩家脚下方块是制箭台，则生成箭矢
- 箭矢直接生成在玩家位置附近，无拾取延迟
- 使用系统时间控制生成频率（每 100ms 一支）
- 玩家离开制箭台时自动停止并清理计时数据

```java
package com.xingduansuzhao.aimod.fletching;

import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@EventBusSubscriber(modid = AiMod.MODID)
public class FletchingArrowGenerator {

    // 记录每个玩家上次生成箭矢的时间戳（毫秒）
    private static final Map<Player, Long> lastGenerationTime = new HashMap<>();

    // 生成间隔：100 毫秒 = 每秒 10 支箭矢
    private static final long GENERATION_INTERVAL = 100L;

    private static final Random random = new Random();

    /**
     * 监听服务端 tick 事件（Post 阶段）
     * 每 tick 检查所有在线玩家是否站在制箭台上
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().overworld() != null) {
            ServerLevel level = event.getServer().overworld();
            for (Player player : level.players()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    checkAndGenerateArrows(serverPlayer);
                }
            }
        }
    }

    /**
     * 检查单个玩家是否站在制箭台上，如果是则生成箭矢
     */
    private static void checkAndGenerateArrows(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        // 玩家脚下的方块 = 玩家位置下方一格
        BlockPos feetBlockPos = playerPos.below();

        if (player.level().getBlockState(feetBlockPos).getBlock() == Blocks.FLETCHING_TABLE) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastGenerationTime.getOrDefault(player, 0L);

            if (currentTime - lastTime >= GENERATION_INTERVAL) {
                generateArrowAtFletchingTable(player, feetBlockPos);
                lastGenerationTime.put(player, currentTime);
            }
        } else {
            // 玩家不在制箭台上时，清理该玩家的计时数据
            lastGenerationTime.remove(player);
        }
    }

    /**
     * 在玩家位置生成一支箭矢掉落物
     */
    private static void generateArrowAtFletchingTable(ServerPlayer player, BlockPos fletchingTablePos) {
        ServerLevel level = (ServerLevel) player.level();

        // 在玩家位置附近随机偏移一点点，避免全部堆在同一个点
        double x = player.getX() + (random.nextDouble() - 0.5) * 0.15;
        double y = player.getY() + 0.1; // 略高于脚底
        double z = player.getZ() + (random.nextDouble() - 0.5) * 0.15;

        ItemStack arrowStack = new ItemStack(Items.ARROW, 1);

        ItemEntity arrowEntity = new ItemEntity(level, x, y, z, arrowStack);
        arrowEntity.setPickUpDelay(0);       // 立即可拾取，不会残留
        arrowEntity.setNoGravity(true);      // 无重力，不会掉落到别处
        arrowEntity.setDeltaMovement(0, 0, 0); // 无初始速度

        level.addFreshEntity(arrowEntity);
    }

    /**
     * 清理玩家数据（玩家退出时调用）
     * 防止 Map 中的 Player 引用导致内存泄漏
     */
    public static void cleanupPlayerData(Player player) {
        lastGenerationTime.remove(player);
    }
}
```

**逐行关键点解释**：

#### 4.1.1 事件监听

```java
@EventBusSubscriber(modid = AiMod.MODID)
```
- 这个注解让 NeoForge 自动将类中的 `@SubscribeEvent` 静态方法注册到**游戏事件总线**
- `ServerTickEvent.Post` 是游戏事件总线上的事件
- 不需要手动调用 `NeoForge.EVENT_BUS.register()`

#### 4.1.2 ServerTickEvent.Post

```java
@SubscribeEvent
public static void onServerTick(ServerTickEvent.Post event) {
```
- `ServerTickEvent.Post`：每个服务端 tick 结束后触发（每秒 20 次）
- 通过 `event.getServer().overworld()` 获取主世界
- 遍历主世界的所有玩家

> **注意**：在 NeoForge 1.21.10 中，`ServerTickEvent` 分为 `Pre` 和 `Post`，没有旧版的 `Phase` 字段。

#### 4.1.3 玩家脚下方块检测

```java
BlockPos playerPos = player.blockPosition();
BlockPos feetBlockPos = playerPos.below();
```
- `player.blockPosition()` 返回玩家所在的方块坐标
- `.below()` 获取下方一格的坐标 = 玩家脚踩的方块
- 用 `getBlockState(pos).getBlock() == Blocks.FLETCHING_TABLE` 判断是否为制箭台

#### 4.1.4 生成频率控制

```java
long currentTime = System.currentTimeMillis();
long lastTime = lastGenerationTime.getOrDefault(player, 0L);
if (currentTime - lastTime >= GENERATION_INTERVAL) {
```
- 使用 `System.currentTimeMillis()` 而不是 tick 计数，更精确地控制 100ms 间隔
- 服务端 tick 每 50ms 一次（20 TPS），所以大约每 2 个 tick 生成一次箭矢
- `Map<Player, Long>` 为每个玩家独立计时

#### 4.1.5 箭矢生成细节

```java
double x = player.getX() + (random.nextDouble() - 0.5) * 0.15;
double y = player.getY() + 0.1;
double z = player.getZ() + (random.nextDouble() - 0.5) * 0.15;
```
- 箭矢生成在**玩家位置**而不是制箭台位置——这确保了箭矢总是在玩家身边
- 随机偏移 `±0.075` 格，避免全部堆在同一点
- Y 坐标比玩家脚底高 `0.1`，确保在拾取范围内

```java
arrowEntity.setPickUpDelay(0);       // 立即可拾取
arrowEntity.setNoGravity(true);      // 无重力
arrowEntity.setDeltaMovement(0, 0, 0); // 无速度
```
- `setPickUpDelay(0)` = 生成后立即可被玩家拾取，几乎瞬间进入背包
- `setNoGravity(true)` = 万一没被立即拾取，也不会掉到别的地方
- `setDeltaMovement(0,0,0)` = 无初始运动速度

**为什么箭矢不会残留？**
1. 箭矢生成在玩家碰撞箱内（`player.getX/Y/Z`）
2. 拾取延迟为 0，生成后下一个 tick 就会被拾取
3. 即使玩家满背包无法拾取，箭矢也无重力、无运动，会在 6000 tick 后自然消失

#### 4.1.6 自动清理

```java
} else {
    lastGenerationTime.remove(player);
}
```
- 玩家离开制箭台后，立即从 Map 中移除该玩家的计时数据
- 下次再站上制箭台时会重新开始计时

---

### 4.2 AiMod.java（主类修改）

**路径**：`src/main/java/com/xingduansuzhao/aimod/AiMod.java`

需要添加玩家退出游戏时的清理逻辑，防止内存泄漏。

#### 4.2.1 添加 import

```java
import com.xingduansuzhao.aimod.fletching.FletchingArrowGenerator;
```

#### 4.2.2 注册玩家退出事件

在 `AiMod` 类中添加以下方法：

```java
@SubscribeEvent
public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
    FletchingArrowGenerator.cleanupPlayerData(event.getEntity());
    LOGGER.info("清理玩家 {} 的制箭台数据", event.getEntity().getName().getString());
}
```

**为什么需要这个？**
- `FletchingArrowGenerator` 的 `lastGenerationTime` Map 以 `Player` 对象为 key
- 玩家退出后 `Player` 对象应该被 GC 回收
- 如果 Map 还持有引用，会导致内存泄漏
- 所以在玩家退出时主动清理

#### 4.2.3 确保主类注册了 NeoForge 游戏事件总线

在 `AiMod` 构造函数中，确保有这一行：

```java
NeoForge.EVENT_BUS.register(this);
```

这行代码将 `AiMod` 实例注册到游戏事件总线，使得 `onPlayerLogout` 等 `@SubscribeEvent` 实例方法能够接收到事件。

需要的 import：
```java
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
```

---

## 五、事件总线说明（极易出错）

| 类/方法 | 事件 | 所在总线 | 注册方式 |
|---|---|---|---|
| `FletchingArrowGenerator.onServerTick` | `ServerTickEvent.Post` | **游戏事件总线** | `@EventBusSubscriber(modid=...)` 自动注册静态方法 |
| `AiMod.onPlayerLogout` | `PlayerEvent.PlayerLoggedOutEvent` | **游戏事件总线** | `NeoForge.EVENT_BUS.register(this)` 注册实例方法 |

**关键区别**：
- `FletchingArrowGenerator` 用的是 `@EventBusSubscriber` 注解 + 静态方法 → 自动注册
- `AiMod.onPlayerLogout` 用的是实例方法 → 需要 `NeoForge.EVENT_BUS.register(this)` 手动注册

两种方式都能工作，但不能混用（静态方法不能用实例注册，实例方法不能用注解自动注册）。

---

## 六、完整的 AiMod.java 关键片段

以下展示 `AiMod.java` 中与此功能相关的完整代码结构：

```java
package com.xingduansuzhao.aimod;

// ... 其他 import ...
import com.xingduansuzhao.aimod.fletching.FletchingArrowGenerator;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;

@Mod(AiMod.MODID)
public class AiMod {
    public static final String MODID = "aimod";
    // ... 其他字段 ...

    public AiMod(IEventBus modEventBus, ModContainer modContainer) {
        // ... 其他注册 ...

        // 关键：注册到游戏事件总线，使 onPlayerLogout 生效
        NeoForge.EVENT_BUS.register(this);

        // ... 其他配置 ...
    }

    // ... 其他方法 ...

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FletchingArrowGenerator.cleanupPlayerData(event.getEntity());
        LOGGER.info("清理玩家 {} 的制箭台数据", event.getEntity().getName().getString());
    }
}
```

---

## 七、设计决策说明

### 7.1 为什么用 System.currentTimeMillis() 而不是 tick 计数？

Minecraft 服务端每秒 20 tick（每 tick 50ms），但如果用 tick 计数控制频率：
- 每 2 tick = 100ms 生成 1 支，需要维护一个 tick 计数器
- `System.currentTimeMillis()` 更简单直观，且不受 TPS 波动影响

### 7.2 为什么箭矢生成在玩家位置而不是制箭台位置？

如果生成在制箭台中心：
- 玩家站在制箭台边缘时可能拾取不到
- 玩家离开制箭台时，已生成但未拾取的箭矢会残留

生成在玩家位置 + `setPickUpDelay(0)`：
- 箭矢几乎瞬间被拾取
- 无论玩家站在制箭台哪个位置都能拾取
- 玩家离开时不会残留

### 7.3 为什么用 setNoGravity(true)？

极端情况下（如服务端卡顿），箭矢可能来不及被拾取。如果有重力，箭矢可能掉落到制箭台下面或旁边。无重力确保箭矢留在原地等待拾取或自然消失。

### 7.4 为什么只遍历 overworld() 的玩家？

当前实现只检查主世界的玩家。如果需要支持所有维度（下界、末地），需要遍历所有 `ServerLevel`：

```java
for (ServerLevel level : event.getServer().getAllLevels()) {
    for (Player player : level.players()) {
        // ...
    }
}
```

当前简单实现足够满足需求。

---

## 八、常见错误排查

1. **站在制箭台上没有箭矢生成**：
   - 检查 `FletchingArrowGenerator` 的 `@EventBusSubscriber(modid = AiMod.MODID)` 注解是否正确
   - 检查方法是否为 `public static`
   - 检查 `ServerTickEvent.Post` 的 import 是否为 `net.neoforged.neoforge.event.tick.ServerTickEvent`（不是旧版路径）
   - 确认玩家确实站在制箭台**上面**（不是旁边）

2. **箭矢残留在制箭台上**：
   - 确认 `setPickUpDelay(0)` 已设置
   - 确认箭矢生成坐标使用的是 `player.getX/Y/Z()` 而不是 `fletchingTablePos`

3. **内存泄漏 / 服务端卡顿**：
   - 确认 `onPlayerLogout` 事件处理器已注册
   - 确认 `NeoForge.EVENT_BUS.register(this)` 在 AiMod 构造函数中

4. **编译错误 - 找不到 ServerTickEvent.Post**：
   - NeoForge 1.21.10 中事件类在 `net.neoforged.neoforge.event.tick` 包下
   - 正确 import：`import net.neoforged.neoforge.event.tick.ServerTickEvent;`
   - 不要用旧版的 `net.neoforged.neoforge.event.TickEvent`

5. **箭矢只在主世界生成**：
   - 当前实现只遍历 `overworld()`，如需支持所有维度请参考上面 7.4 的说明

---

## 九、方块位置判断图解

```
      玩家站立位置
          |
          v
    +-----+-----+
    |   PLAYER   |  ← player.blockPosition() = (x, y, z)
    +-----+-----+
    |  FLETCHING |  ← playerPos.below() = (x, y-1, z) → 检测这个方块
    |   TABLE    |
    +-----+-----+
    |   GROUND   |
    +-----+-----+
```

- `player.blockPosition()` 返回玩家脚所在的方块坐标
- `playerPos.below()` 下移一格 = 玩家脚踩的方块
- 判断这个方块是否为 `Blocks.FLETCHING_TABLE`

---

## 十、完整依赖关系图

```
AiMod.java
  ├── 构造函数 → NeoForge.EVENT_BUS.register(this)
  └── onPlayerLogout → FletchingArrowGenerator.cleanupPlayerData()

FletchingArrowGenerator.java（独立类，自动注册）
  ├── onServerTick → 每 tick 检查所有在线玩家
  ├── checkAndGenerateArrows → 判断是否站在制箭台上
  ├── generateArrowAtFletchingTable → 生成箭矢 ItemEntity
  └── cleanupPlayerData → 清理退出玩家数据
```

---

## 十一、测试验证清单

- [ ] 制作或找到一个制箭台（合成方式：2 燧石 + 4 木板）
- [ ] 站在制箭台上方，观察背包中箭矢数量持续增加
- [ ] 站在制箭台上约 10 秒，应获得约 100 支箭矢
- [ ] 离开制箭台，箭矢停止生成
- [ ] 制箭台上没有残留的箭矢掉落物
- [ ] 重新站上制箭台，箭矢继续生成
- [ ] 在制箭台边缘走来走去，箭矢始终被拾取，不会残留
- [ ] 背包满时站在制箭台上，箭矢生成但无法拾取（正常行为）
- [ ] 退出游戏再重进，功能正常无异常
- [ ] 多人游戏中，多个玩家分别站在不同制箭台上，各自独立生成箭矢
