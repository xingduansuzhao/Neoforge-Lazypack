package com.xingduansuzhao.aimod.qingtian;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.xingduansuzhao.aimod.AiMod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public final class QingtianTransformHandler {
    private static final Map<UUID, Map<Integer, ItemStack>> TRANSFORMED_SLOTS = new ConcurrentHashMap<>();

    private static final List<DeferredItem<? extends Item>> ALL_WEAPONS = List.of(
            AiMod.QINGTIAN, AiMod.COMBAT_AXE, AiMod.BASEBALL_BAT, AiMod.CANJIAOJI,
            AiMod.MILITARY_SHOVEL, AiMod.MILITARY_SHOVEL_GOLD, AiMod.MILITARY_SHOVEL_CHRISTMAS,
            AiMod.MILITARY_SHOVEL_HALLOWEEN, AiMod.MILITARY_SHOVEL_RED, AiMod.MILITARY_SHOVEL_H,
            AiMod.BOXING_GLOVE, AiMod.LASER_KNIFE, AiMod.BATON, AiMod.REAPER
    );

    private QingtianTransformHandler() {
    }

    public static void handleTransform(ServerPlayer player, boolean restore) {
        if (restore) {
            restoreQingtian(player);
        } else {
            randomTransform(player);
        }
    }

    private static void randomTransform(ServerPlayer player) {
        int slot = player.getInventory().getSelectedSlot();
        ItemStack currentStack = player.getInventory().getItem(slot);

        boolean isQingtian = currentStack.getItem() instanceof MyCustomWeapon;
        boolean isTransformed = isTransformedSlot(player.getUUID(), slot);

        if (!isQingtian && !isTransformed) {
            return;
        }

        UUID playerId = player.getUUID();
        if (isQingtian && !isTransformed) {
            TRANSFORMED_SLOTS.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                    .put(slot, currentStack.copy());
        }

        Item currentItem = currentStack.getItem();
        int currentIndex = -1;
        for (int i = 0; i < ALL_WEAPONS.size(); i++) {
            if (ALL_WEAPONS.get(i).get() == currentItem) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex < 0) {
            return;
        }

        DeferredItem<? extends Item> chosen = ALL_WEAPONS.get((currentIndex + 1) % ALL_WEAPONS.size());
        ItemStack newStack = new ItemStack(chosen.get());
        newStack.setCount(1);
        player.getInventory().setItem(slot, newStack);
    }

    private static void restoreQingtian(ServerPlayer player) {
        int slot = player.getInventory().getSelectedSlot();
        UUID playerId = player.getUUID();
        Map<Integer, ItemStack> playerSlots = TRANSFORMED_SLOTS.get(playerId);
        if (playerSlots == null) {
            return;
        }

        ItemStack original = playerSlots.remove(slot);
        if (original != null) {
            player.getInventory().setItem(slot, original);
        }

        if (playerSlots.isEmpty()) {
            TRANSFORMED_SLOTS.remove(playerId);
        }
    }

    public static boolean isTransformedSlot(UUID playerId, int slot) {
        Map<Integer, ItemStack> playerSlots = TRANSFORMED_SLOTS.get(playerId);
        return playerSlots != null && playerSlots.containsKey(slot);
    }

    public static void cleanupPlayer(UUID playerId) {
        TRANSFORMED_SLOTS.remove(playerId);
    }

    public static void onSlotChanged(UUID playerId, int slot, ItemStack newStack) {
        Map<Integer, ItemStack> playerSlots = TRANSFORMED_SLOTS.get(playerId);
        if (playerSlots == null) {
            return;
        }
        if (!playerSlots.containsKey(slot)) {
            return;
        }
        if (newStack.isEmpty()) {
            playerSlots.remove(slot);
            if (playerSlots.isEmpty()) {
                TRANSFORMED_SLOTS.remove(playerId);
            }
        }
    }
}
