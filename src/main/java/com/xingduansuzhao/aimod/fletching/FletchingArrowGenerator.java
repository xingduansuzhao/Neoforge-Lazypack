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

/**
 * Generates arrows when a player stands on a fletching table.
 * Spawns 10 arrows per second (one every 100ms).
 */
@EventBusSubscriber(modid = AiMod.MODID)
public class FletchingArrowGenerator {

    private static final Map<Player, Long> lastGenerationTime = new HashMap<>();
    private static final long GENERATION_INTERVAL = 100L;
    private static final Random random = new Random();

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

    private static void checkAndGenerateArrows(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();
        BlockPos feetBlockPos = playerPos.below();

        if (player.level().getBlockState(feetBlockPos).getBlock() == Blocks.FLETCHING_TABLE) {
            long currentTime = System.currentTimeMillis();
            long lastTime = lastGenerationTime.getOrDefault(player, 0L);

            if (currentTime - lastTime >= GENERATION_INTERVAL) {
                generateArrowAtFletchingTable(player, feetBlockPos);
                lastGenerationTime.put(player, currentTime);
            }
        } else {
            lastGenerationTime.remove(player);
        }
    }

    private static void generateArrowAtFletchingTable(ServerPlayer player, BlockPos fletchingTablePos) {
        ServerLevel level = (ServerLevel) player.level();

        double x = player.getX() + (random.nextDouble() - 0.5) * 0.15;
        double y = player.getY() + 0.1;
        double z = player.getZ() + (random.nextDouble() - 0.5) * 0.15;

        ItemStack arrowStack = new ItemStack(Items.ARROW, 1);

        ItemEntity arrowEntity = new ItemEntity(level, x, y, z, arrowStack);
        arrowEntity.setPickUpDelay(0);
        arrowEntity.setNoGravity(true);
        arrowEntity.setDeltaMovement(0, 0, 0);

        level.addFreshEntity(arrowEntity);
    }

    public static void cleanupPlayerData(Player player) {
        lastGenerationTime.remove(player);
    }
}
