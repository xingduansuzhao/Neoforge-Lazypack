package com.xingduansuzhao.aimod.bamboocicada.client;

import com.xingduansuzhao.aimod.bamboocicada.item.BambooCicadaItem;
import com.xingduansuzhao.aimod.bamboocicada.network.BambooCicadaStopPayload;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import software.bernie.geckolib.animatable.GeoItem;

public final class BambooCicadaSoundManager {
    private static final Map<Integer, BambooCicadaLoopSound> ACTIVE_SOUNDS = new HashMap<>();

    public static void setPlaying(int entityId, boolean playing, InteractionHand hand) {
        stop(entityId);
        if (!playing) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof Player player)) {
            return;
        }

        BambooCicadaLoopSound sound = new BambooCicadaLoopSound(player, hand);
        ACTIVE_SOUNDS.put(entityId, sound);
        minecraft.getSoundManager().play(sound);
    }

    public static InteractionHand interruptLocalAnimation() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return null;
        }

        BambooCicadaLoopSound sound = ACTIVE_SOUNDS.get(player.getId());
        if (sound == null) {
            return null;
        }

        InteractionHand hand = sound.hand();
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof BambooCicadaItem item) {
            item.stopLoop(player, GeoItem.getId(stack));
        }

        stop(player.getId());
        ClientPacketDistributor.sendToServer(BambooCicadaStopPayload.INSTANCE);
        return hand;
    }

    static void forget(int entityId, BambooCicadaLoopSound sound) {
        ACTIVE_SOUNDS.remove(entityId, sound);
    }

    private static void stop(int entityId) {
        BambooCicadaLoopSound sound = ACTIVE_SOUNDS.remove(entityId);
        if (sound == null) {
            return;
        }

        sound.stopImmediately();
        Minecraft.getInstance().getSoundManager().stop(sound);
    }

    private BambooCicadaSoundManager() {
    }
}
