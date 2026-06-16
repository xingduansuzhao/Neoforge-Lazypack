package com.xingduansuzhao.aimod.karambit;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;

public class KarambitWeapon extends AnimatedWeaponItem {
    private static final String SWITCH_MAIN = "switch_main";
    private static final String SWITCH_OFF = "switch_off";
    private static final String HEAVY_ATTACK_MAIN = "heavy_attack_main";
    private static final String HEAVY_ATTACK_OFF = "heavy_attack_off";
    private static final Map<UUID, Boolean> MIRRORING_ACTIVE = new ConcurrentHashMap<>();

    public KarambitWeapon(Item.Properties properties) {
        super(
                "karambit",
                properties,
                AiMod.KARAMBIT_SWITCH,
                AiMod.KARAMBIT_HEAVY_ATTACK,
                AiMod.KARAMBIT_LIGHT_ATTACK_1,
                AiMod.KARAMBIT_LIGHT_ATTACK_2,
                true,
                false,
                false,
                6,
                8,
                7,
                10
        );
    }

    @Override
    public boolean rendersPairedOffhand() {
        return true;
    }

    @Override
    protected String getSwitchTrigger(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? SWITCH_OFF : SWITCH_MAIN;
    }

    @Override
    protected String getHeavyAttackTrigger(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? HEAVY_ATTACK_OFF : HEAVY_ATTACK_MAIN;
    }

    public static void onSwapHands(LivingSwapItemsEvent.Hands event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        if (isKarambit(entity.getMainHandItem()) || isKarambit(entity.getOffhandItem())) {
            event.setCanceled(true);
        }
    }

    public static void cleanupPlayer(UUID playerId) {
        MIRRORING_ACTIVE.remove(playerId);
    }

    public static void tickServerPlayers(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            syncPairedOffhand(player);
        }

        MIRRORING_ACTIVE.keySet().removeIf(uuid -> players.stream()
                .noneMatch(player -> player.getUUID().equals(uuid)));
    }

    private static void syncPairedOffhand(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        UUID playerId = player.getUUID();
        boolean holdingKarambitInMainHand = isKarambit(mainHand);

        if (holdingKarambitInMainHand) {
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty()) {
                displaceOffhandToInventory(player, offhand);
            }
            MIRRORING_ACTIVE.put(playerId, Boolean.TRUE);
            return;
        }

        MIRRORING_ACTIVE.remove(playerId);
    }

    private static void displaceOffhandToInventory(ServerPlayer player, ItemStack offhand) {
        ItemStack toInsert = offhand.copy();
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        if (!player.getInventory().add(toInsert)) {
            player.drop(toInsert, false);
        }
    }

    private static boolean isKarambit(ItemStack stack) {
        return stack.getItem() instanceof KarambitWeapon;
    }
}
