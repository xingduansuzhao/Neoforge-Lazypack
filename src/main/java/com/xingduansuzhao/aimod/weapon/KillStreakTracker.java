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
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = AiMod.MODID)
public class KillStreakTracker {
    private static final int MAX_STREAK_GAP_TICKS = 200;
    private static final int MAX_STREAK_SOUNDS = 8;
    private static final boolean STREAK_SOUNDS_ENABLED = false;
    private static final float KILLSTREAK_VOLUME = 2000.0f;
    private static final Map<UUID, StreakState> PLAYER_STREAKS = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static final Supplier<? extends SoundEvent>[] STREAK_SOUNDS = new Supplier[]{
            AiMod.KILLSTREAK_HEADSHOT,
            AiMod.KILLSTREAK_DOUBLE_KILL,
            AiMod.KILLSTREAK_MULTI_KILL,
            AiMod.KILLSTREAK_MEGA_KILL,
            AiMod.KILLSTREAK_ULTRA_KILL,
            AiMod.KILLSTREAK_MONSTER_KILL,
            AiMod.KILLSTREAK_LUDICROUS_KILL,
            AiMod.KILLSTREAK_HOLY_SHIT
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

        // Keep tracking the streak and icon notification, but temporarily suppress
        // the eight announcer sounds. Set STREAK_SOUNDS_ENABLED to true to restore them.
        if (STREAK_SOUNDS_ENABLED && killCount < MAX_STREAK_SOUNDS) {
            SoundEvent streakSound = STREAK_SOUNDS[killCount].get();
            killer.playNotifySound(streakSound, SoundSource.PLAYERS, KILLSTREAK_VOLUME, 1.0f);
        }

        SoundEvent iconSound = AiMod.KILLSTREAK_ICON.get();
        killer.playNotifySound(iconSound, SoundSource.PLAYERS, 0.2f, 1.0f);

        int iconIndex = Math.min(killCount, 5);
        PacketDistributor.sendToPlayer(killer, new KillStreakPayload(iconIndex));

        PLAYER_STREAKS.put(killerId, new StreakState(killCount + 1, currentTick));

        AiMod.LOGGER.debug("Kill streak: player={} killCount={} soundPlayed={}",
                killer.getName().getString(), killCount + 1,
                STREAK_SOUNDS_ENABLED && killCount < MAX_STREAK_SOUNDS);
    }

    public static void cleanupPlayer(UUID playerId) {
        PLAYER_STREAKS.remove(playerId);
    }

    private record StreakState(int killCount, int lastKillTick) {
    }
}
