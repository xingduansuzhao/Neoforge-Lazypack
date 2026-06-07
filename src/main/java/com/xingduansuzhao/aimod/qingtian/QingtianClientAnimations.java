package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.AiMod;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class QingtianClientAnimations {
    public static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "qingtian_heavy_attack_layer");
    public static final ResourceLocation HEAVY_ATTACK = ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "heavy_attack");
    private static final int HEAVY_ATTACK_LOCK_TICKS = 20;
    private static int heavyAttackLockedUntil;

    private QingtianClientAnimations() {
    }

    public static void registerLayer() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                LAYER_ID,
                2000,
                player -> new PlayerAnimationController(player, (controller, state, animSetter) -> PlayState.STOP)
        );
    }

    public static void playHeavyAttack(Player player) {
        if (player.tickCount < heavyAttackLockedUntil) {
            return;
        }

        heavyAttackLockedUntil = player.tickCount + HEAVY_ATTACK_LOCK_TICKS;
        if (PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER_ID) instanceof PlayerAnimationController controller) {
            controller.triggerAnimation(HEAVY_ATTACK);
        }
    }

    public static void resetHeavyAttackLock() {
        heavyAttackLockedUntil = 0;
    }
}
