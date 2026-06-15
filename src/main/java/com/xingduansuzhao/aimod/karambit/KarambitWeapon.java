package com.xingduansuzhao.aimod.karambit;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

public class KarambitWeapon extends AnimatedWeaponItem {
    private static final String SWITCH_MAIN = "switch_main";
    private static final String SWITCH_OFF = "switch_off";
    private static final String HEAVY_ATTACK_MAIN = "heavy_attack_main";
    private static final String HEAVY_ATTACK_OFF = "heavy_attack_off";
    private static final Map<UUID, ItemStack> PREVIOUS_OFFHAND_STACKS = new ConcurrentHashMap<>();

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
                7
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

    public static void tickServerPlayers(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            syncPairedOffhand(player);
        }

        PREVIOUS_OFFHAND_STACKS.keySet().removeIf(uuid -> players.stream()
                .noneMatch(player -> player.getUUID().equals(uuid)));
    }

    private static void syncPairedOffhand(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        UUID playerId = player.getUUID();
        boolean holdingKarambitInMainHand = isKarambit(mainHand);
        boolean hasMirroredOffhand = PREVIOUS_OFFHAND_STACKS.containsKey(playerId);

        if (holdingKarambitInMainHand) {
            if (player.level() instanceof ServerLevel serverLevel) {
                GeoItem.getOrAssignId(mainHand, serverLevel);
            }

            if (!hasMirroredOffhand) {
                PREVIOUS_OFFHAND_STACKS.put(playerId, player.getOffhandItem().copy());
            }

            ItemStack offhand = player.getOffhandItem();
            if (offhand.isEmpty() || isKarambit(offhand)) {
                ItemStack mirroredStack = mainHand.copy();
                mirroredStack.setCount(1);
                if (player.level() instanceof ServerLevel serverLevel) {
                    GeoItem.getOrAssignId(mirroredStack, serverLevel);
                }
                player.setItemInHand(InteractionHand.OFF_HAND, mirroredStack);
            }
            return;
        }

        if (hasMirroredOffhand) {
            ItemStack currentOffhand = player.getOffhandItem();
            ItemStack previousOffhand = PREVIOUS_OFFHAND_STACKS.remove(playerId);
            if (isKarambit(currentOffhand)) {
                player.setItemInHand(InteractionHand.OFF_HAND, previousOffhand);
            }
        }
    }

    private static boolean isKarambit(ItemStack stack) {
        return stack.getItem() instanceof KarambitWeapon;
    }
}
