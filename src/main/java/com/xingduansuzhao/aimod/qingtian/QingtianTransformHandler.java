package com.xingduansuzhao.aimod.qingtian;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public final class QingtianTransformHandler {
    private static final Map<UUID, Map<Integer, ItemStack>> TRANSFORMED_SLOTS = new ConcurrentHashMap<>();

    private static final List<DeferredItem<? extends AnimatedWeaponItem>> ALL_WEAPONS = AiMod.ANIMATED_WEAPON_ITEMS;

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

        List<DeferredItem<? extends AnimatedWeaponItem>> otherWeapons = ALL_WEAPONS.stream()
                .filter(weapon -> weapon.get() != currentStack.getItem())
                .toList();
        if (otherWeapons.isEmpty()) {
            return;
        }

        DeferredItem<? extends AnimatedWeaponItem> chosen = otherWeapons.get(
                ThreadLocalRandom.current().nextInt(otherWeapons.size()));
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
