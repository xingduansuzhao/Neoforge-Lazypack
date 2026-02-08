package com.xingduansuzhao.aimod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 制箭台箭矢生成器
 * 当玩家站在制箭台上时，每秒生成10个箭矢
 */
@EventBusSubscriber(modid = AiMod.MODID)
public class FletchingArrowGenerator {
    
    // 存储每个玩家的上次生成时间戳
    private static final Map<Player, Long> lastGenerationTime = new HashMap<>();
    // 生成间隔（毫秒）- 每秒10个箭矢 = 每100毫秒生成一次
    private static final long GENERATION_INTERVAL = 100L;
    // 随机数生成器
    private static final Random random = new Random();
    
    /**
     * 监听服务器tick事件，检查玩家是否需要生成箭矢
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 在服务器端处理所有在线玩家
        if (event.getServer().overworld() != null) {
            ServerLevel level = event.getServer().overworld();
            
            // 遍历所有在线玩家
            for (Player player : level.players()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    checkAndGenerateArrows(serverPlayer);
                }
            }
        }
    }
    
    /**
     * 检查玩家是否站在制箭台上并生成箭矢
     */
    private static void checkAndGenerateArrows(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        BlockPos feetBlockPos = playerPos.below(); // 获取玩家脚下的方块位置
        
        // 检查玩家脚下是否是制箭台
        if (player.level().getBlockState(feetBlockPos).getBlock() == Blocks.FLETCHING_TABLE) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastGenerationTime.getOrDefault(player, 0L);
            
            // 如果距离上次生成已经过了指定间隔时间
            if (currentTime - lastTime >= GENERATION_INTERVAL) {
                generateArrowAtFletchingTable(player, feetBlockPos);
                lastGenerationTime.put(player, currentTime);
            }
        } else {
            // 玩家不在制箭台上时，从记录中移除
            lastGenerationTime.remove(player);
        }
    }
    
    /**
     * 在玩家位置生成箭矢
     */
    private static void generateArrowAtFletchingTable(ServerPlayer player, BlockPos fletchingTablePos) {
        ServerLevel level = (ServerLevel) player.level();
        
        // 以玩家位置为中心生成箭矢，确保跟随玩家移动
        double x = player.getX() + (random.nextDouble() - 0.5) * 0.2; // 玩家周围小范围
        double y = player.getY() + 0.1; // 玩家脚下略上方
        double z = player.getZ() + (random.nextDouble() - 0.5) * 0.2; // 玩家周围小范围
        
        // 创建箭矢物品
        ItemStack arrowStack = new ItemStack(Items.ARROW, 1);
        
        // 创建物品实体
        ItemEntity arrowEntity = new ItemEntity(
            level,
            x, y, z,
            arrowStack
        );
        
        // 设置物品的一些属性
        arrowEntity.setPickUpDelay(5); // 缩短拾取延迟，提高玩家拾取率
        arrowEntity.setDeltaMovement(
            (random.nextDouble() - 0.5) * 0.05, // 减小水平扩散
            0.15 + random.nextDouble() * 0.05,   // 降低初始上升速度
            (random.nextDouble() - 0.5) * 0.05  // 减小水平扩散
        );
        
        // 添加轻微的旋转效果
        arrowEntity.setXRot(random.nextFloat() * 360);
        arrowEntity.setYRot(random.nextFloat() * 360);
        
        // 生成物品实体
        level.addFreshEntity(arrowEntity);
        
        // 记录日志（可选）
        if (level.getGameTime() % 200 == 0) { // 每10秒记录一次，减少日志量
            AiMod.LOGGER.info("在制箭台生成箭矢，玩家: {}", player.getName().getString());
        }
    }
    
    /**
     * 清理玩家数据（当玩家退出时调用）
     */
    public static void cleanupPlayerData(Player player) {
        lastGenerationTime.remove(player);
    }
}