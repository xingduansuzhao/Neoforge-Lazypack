package com.xingduansuzhao.aimod.bamboocicada.event;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.bamboocicada.item.BambooCicadaItem;
import com.xingduansuzhao.aimod.bamboocicada.network.BambooCicadaSoundPayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import software.bernie.geckolib.animatable.GeoItem;

public final class BambooCicadaLoopHandler {
    private static final Map<UUID, ActiveLoop> ACTIVE_LOOPS = new HashMap<>();

    public static void toggle(
            ServerPlayer player,
            ServerLevel level,
            InteractionHand hand,
            ItemStack stack,
            BambooCicadaItem item
    ) {
        long instanceId = GeoItem.getOrAssignId(stack, level);
        ActiveLoop current = ACTIVE_LOOPS.get(player.getUUID());
        if (current != null) {
            stop(player, current);
            if (current.instanceId() == instanceId && current.hand() == hand) {
                return;
            }
        }

        item.startLoop(player, level, stack);
        ACTIVE_LOOPS.put(player.getUUID(), new ActiveLoop(instanceId, hand));
        syncSound(player, true, hand);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ActiveLoop active = ACTIVE_LOOPS.get(player.getUUID());
        if (active == null) {
            return;
        }

        ItemStack heldStack = player.getItemInHand(active.hand());
        if (!player.isAlive()
                || heldStack.getItem() != AiMod.BAMBOO_CICADA.get()
                || GeoItem.getId(heldStack) != active.instanceId()) {
            stop(player, active);
        }
    }

    public static void stopFromInput(ServerPlayer player) {
        ActiveLoop active = ACTIVE_LOOPS.get(player.getUUID());
        if (active != null) {
            stop(player, active);
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ActiveLoop active = ACTIVE_LOOPS.remove(player.getUUID());
            if (active != null) {
                PacketDistributor.sendToPlayersTrackingEntity(
                        player,
                        soundPayload(player, false, active.hand())
                );
            }
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        ActiveLoop active = ACTIVE_LOOPS.remove(event.getOriginal().getUUID());
        if (active != null && event.getOriginal() instanceof ServerPlayer original) {
            PacketDistributor.sendToPlayersTrackingEntity(
                    original,
                    soundPayload(original, false, active.hand())
            );
        }
    }

    private static void stop(ServerPlayer player, ActiveLoop active) {
        ((BambooCicadaItem) AiMod.BAMBOO_CICADA.get()).stopLoop(player, active.instanceId());
        syncSound(player, false, active.hand());
        ACTIVE_LOOPS.remove(player.getUUID());
    }

    private static void syncSound(ServerPlayer player, boolean playing, InteractionHand hand) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                player,
                soundPayload(player, playing, hand)
        );
    }

    private static BambooCicadaSoundPayload soundPayload(
            ServerPlayer player,
            boolean playing,
            InteractionHand hand
    ) {
        return new BambooCicadaSoundPayload(player.getId(), playing, hand.ordinal());
    }

    private record ActiveLoop(long instanceId, InteractionHand hand) {
    }

    private BambooCicadaLoopHandler() {
    }
}
