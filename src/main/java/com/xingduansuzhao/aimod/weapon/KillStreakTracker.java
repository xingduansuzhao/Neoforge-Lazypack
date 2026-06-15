package com.xingduansuzhao.aimod.weapon;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.xingduansuzhao.aimod.AiMod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = AiMod.MODID)
public class KillStreakTracker {
    private static final int MAX_STREAK_GAP_TICKS = 80;
    private static final int MAX_STREAK_SOUNDS = 8;
    private static final float KILLSTREAK_VOLUME = 5.0f;
    private static final Map<UUID, StreakState> PLAYER_STREAKS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static final Supplier<? extends SoundEvent>[] STREAK_SOUNDS = new Supplier[]{
            AiMod.KILLSTREAK_1,
            AiMod.KILLSTREAK_2,
            AiMod.KILLSTREAK_3,
            AiMod.KILLSTREAK_4,
            AiMod.KILLSTREAK_5,
            AiMod.KILLSTREAK_6,
            AiMod.KILLSTREAK_7,
            AiMod.KILLSTREAK_8
    };

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity == null) {
            sourceEntity = event.getSource().getDirectEntity();
        }

        if (!(sourceEntity instanceof ServerPlayer killer)) {
            return;
        }

        if (!AnimatedWeaponItem.isHoldingAnimatedWeapon(killer)) {
            return;
        }

        if (!(killer.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        UUID killerId = killer.getUUID();
        int currentTick = (int) serverLevel.getGameTime();
        StreakState state = PLAYER_STREAKS.get(killerId);

        int killCount = 0;
        if (state != null && (currentTick - state.lastKillTick) <= MAX_STREAK_GAP_TICKS) {
            killCount = state.killCount;
        }

        int soundIndex = Math.min(killCount, MAX_STREAK_SOUNDS - 1);
        SoundEvent streakSound = STREAK_SOUNDS[soundIndex].get();
        SoundEvent iconSound = AiMod.KILLSTREAK_ICON.get();

        killer.playNotifySound(streakSound, SoundSource.PLAYERS, 999.0f, 1.0f);
        killer.playNotifySound(iconSound, SoundSource.PLAYERS, 0.2f, 1.0f);

        PLAYER_STREAKS.put(killerId, new StreakState(killCount + 1, currentTick));

        AiMod.LOGGER.debug("Kill streak: player={} killCount={} soundIndex={}",
                killer.getName().getString(), killCount + 1, soundIndex);
    }

    public static void cleanupPlayer(UUID playerId) {
        PLAYER_STREAKS.remove(playerId);
    }

    private record StreakState(int killCount, int lastKillTick) {
    }
}
