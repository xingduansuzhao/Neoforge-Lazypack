package com.xingduansuzhao.aimod.weapon;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.xingduansuzhao.aimod.AiMod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;

public final class WeaponKillCycler {
    private static final int SWITCH_DELAY_TICKS = 7;

    private static final List<DeferredItem<? extends Item>> CYCLE_WEAPONS = List.of(
            AiMod.QINGTIAN, AiMod.MILITARY_SHOVEL, AiMod.MILITARY_SHOVEL_GOLD,
            AiMod.MILITARY_SHOVEL_CHRISTMAS, AiMod.MILITARY_SHOVEL_HALLOWEEN, AiMod.MILITARY_SHOVEL_RED,
            AiMod.MILITARY_SHOVEL_H, AiMod.BOXING_GLOVE, AiMod.LASER_KNIFE, AiMod.BATON, AiMod.REAPER,
            AiMod.CANJIAOJI, AiMod.KRIS,
            AiMod.BASEBALL_BAT, AiMod.KNIFE, AiMod.COMBAT_AXE,
            AiMod.WRENCH, AiMod.KARAMBIT
    );

    private static final Map<UUID, PendingSwitch> PENDING_SWITCHES = new ConcurrentHashMap<>();

    private WeaponKillCycler() {
    }

    /**
     * @param killCount the current streak kill count (0-based, before incrementing)
     */
    public static void onStreakKill(ServerPlayer killer, int gameTick, int killCount) {
        if (!killer.getMainHandItem().is(AiMod.QINGTIAN.get())) {
            return;
        }

        int nextIndex = (killCount + 1) % CYCLE_WEAPONS.size();
        PENDING_SWITCHES.put(killer.getUUID(), new PendingSwitch(gameTick + SWITCH_DELAY_TICKS, nextIndex));
    }

    public static void tickServer(Collection<ServerPlayer> players) {
        if (PENDING_SWITCHES.isEmpty()) {
            return;
        }

        for (ServerPlayer player : players) {
            PendingSwitch pending = PENDING_SWITCHES.get(player.getUUID());
            if (pending == null) {
                continue;
            }

            int gameTick = (int) ((ServerLevel) player.level()).getGameTime();
            if (gameTick < pending.switchTick) {
                continue;
            }

            PENDING_SWITCHES.remove(player.getUUID());

            if (!AnimatedWeaponItem.isHoldingAnimatedWeapon(player)) {
                continue;
            }

            int slot = player.getInventory().getSelectedSlot();
            Item targetItem = CYCLE_WEAPONS.get(pending.targetIndex).get();
            ItemStack newStack = new ItemStack(targetItem);
            newStack.setCount(1);
            player.getInventory().setItem(slot, newStack);

            AiMod.LOGGER.debug("Weapon kill cycle: player={} switched to weapon index {} ({})",
                    player.getName().getString(), pending.targetIndex,
                    CYCLE_WEAPONS.get(pending.targetIndex).getId().getPath());
        }

        PENDING_SWITCHES.keySet().removeIf(uuid -> players.stream()
                .noneMatch(p -> p.getUUID().equals(uuid)));
    }

    public static void cleanupPlayer(UUID playerId) {
        PENDING_SWITCHES.remove(playerId);
    }

    private record PendingSwitch(int switchTick, int targetIndex) {
    }
}
