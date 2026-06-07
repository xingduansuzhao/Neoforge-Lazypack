package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.AiMod;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MyCustomWeapon extends Item implements GeoItem {
    private static final String CONTROLLER = "qingtian_first_person_controller";
    private static final String TRIGGER_HEAVY_ATTACK = "heavy_attack";
    private static final RawAnimation HEAVY_ATTACK = RawAnimation.begin().thenPlay("heavy_attack");
    private static final int HEAVY_ATTACK_LOCK_TICKS = 20;
    private static final Map<UUID, Integer> HEAVY_ATTACK_LOCKED_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> NEXT_LIGHT_ATTACK_USES_SECOND_SOUND = new ConcurrentHashMap<>();

    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public MyCustomWeapon(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            if (lockHeavyAttack(player)) {
                long instanceId = GeoItem.getOrAssignId(stack, serverLevel);
                triggerAnim(player, instanceId, CONTROLLER, TRIGGER_HEAVY_ATTACK);
                serverLevel.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        AiMod.QINGTIAN_HEAVY_ATTACK.get(),
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );
            }
        }

        if (level.isClientSide()) {
            QingtianClientAnimations.playHeavyAttack(player);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && entity instanceof Player player && entity.level() instanceof ServerLevel serverLevel) {
            boolean useSecondSound = NEXT_LIGHT_ATTACK_USES_SECOND_SOUND.getOrDefault(player.getUUID(), false);
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    useSecondSound ? AiMod.QINGTIAN_LIGHT_ATTACK_2.get() : AiMod.QINGTIAN_LIGHT_ATTACK_1.get(),
                    SoundSource.PLAYERS,
                    1.0f,
                    1.0f
            );
            NEXT_LIGHT_ATTACK_USES_SECOND_SOUND.put(player.getUUID(), !useSecondSound);
        }

        return false;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(CONTROLLER, state -> PlayState.STOP)
                .triggerableAnim(TRIGGER_HEAVY_ATTACK, HEAVY_ATTACK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    public static void resetHeavyAttackLocksForPlayersNotHolding(Collection<ServerPlayer> players) {
        HEAVY_ATTACK_LOCKED_UNTIL.entrySet().removeIf(entry -> players.stream()
                .noneMatch(player -> player.getUUID().equals(entry.getKey()) && isHoldingQingtian(player)));
    }

    public static boolean isHoldingQingtian(Player player) {
        return player.getMainHandItem().getItem() instanceof MyCustomWeapon
                || player.getOffhandItem().getItem() instanceof MyCustomWeapon;
    }

    private static boolean lockHeavyAttack(Player player) {
        int currentTick = player.tickCount;
        Integer lockedUntil = HEAVY_ATTACK_LOCKED_UNTIL.get(player.getUUID());
        if (lockedUntil != null && currentTick < lockedUntil) {
            return false;
        }

        HEAVY_ATTACK_LOCKED_UNTIL.put(player.getUUID(), currentTick + HEAVY_ATTACK_LOCK_TICKS);
        return true;
    }
}
