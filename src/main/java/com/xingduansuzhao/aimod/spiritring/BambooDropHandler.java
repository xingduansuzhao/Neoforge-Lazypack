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

/**
 * Breaking bamboo drops exactly one spirit ring and nothing else.
 * Uses a per-player cooldown so chain-breaking multi-segment bamboo
 * still only produces a single spirit ring.
 */
@EventBusSubscriber(modid = AiMod.MODID)
public class BambooDropHandler {

    private static final Map<UUID, Long> recentBreaks = new HashMap<>();
    private static final long COOLDOWN_TICKS = 5L;

    /**
     * Cancel all vanilla bamboo drops (bamboo items, etc.).
     */
    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!(event.getState().getBlock() instanceof BambooStalkBlock)) {
            return;
        }
        event.setCanceled(true);
    }

    /**
     * Spawn exactly one spirit ring when a player breaks bamboo,
     * with a short cooldown to prevent duplicates from chain breaks.
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

        Long lastBreakTick = recentBreaks.get(playerId);
        if (lastBreakTick != null && (currentTick - lastBreakTick) < COOLDOWN_TICKS) {
            return;
        }
        recentBreaks.put(playerId, currentTick);

        BlockPos pos = event.getPos();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        ItemStack spiritRingStack = new ItemStack(AiMod.SPIRIT_RING.get(), 1);
        ItemEntity itemEntity = new ItemEntity(serverLevel, x, y, z, spiritRingStack);
        itemEntity.setPickUpDelay(10);
        serverLevel.addFreshEntity(itemEntity);
    }
}
