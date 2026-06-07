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
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
    private static final int HEAVY_ATTACK_DAMAGE_DELAY_TICKS = 13;
    private static final double HEAVY_ATTACK_RANGE = 5.0;
    private static final double HEAVY_ATTACK_HALF_WIDTH = 1.5;
    private static final double HEAVY_ATTACK_VERTICAL_TOLERANCE = 2.25;
    private static final float HEAVY_ATTACK_DAMAGE_MULTIPLIER = 1.6f;
    private static final float HEAVY_ATTACK_BONUS_DAMAGE = 2.0f;
    private static final double HEAVY_ATTACK_KNOCKBACK = 0.75;
    private static final Map<UUID, Integer> HEAVY_ATTACK_LOCKED_UNTIL = new ConcurrentHashMap<>();
    private static final Map<UUID, HeavyAttackHit> PENDING_HEAVY_ATTACK_HITS = new ConcurrentHashMap<>();
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
                scheduleHeavyAttackHit(player, hand);
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

    public static void tickServerPlayers(Collection<ServerPlayer> players) {
        processPendingHeavyAttackHits(players);
        resetHeavyAttackLocksForPlayersNotHolding(players);
        PENDING_HEAVY_ATTACK_HITS.keySet().removeIf(uuid -> players.stream()
                .noneMatch(player -> player.getUUID().equals(uuid)));
    }

    private static void processPendingHeavyAttackHits(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            HeavyAttackHit pendingHit = PENDING_HEAVY_ATTACK_HITS.get(player.getUUID());
            if (pendingHit == null || player.tickCount < pendingHit.hitTick()) {
                continue;
            }

            PENDING_HEAVY_ATTACK_HITS.remove(player.getUUID());
            if (isHoldingQingtian(player) && player.isAlive() && !player.isSpectator()) {
                performHeavyAttackHit(player, pendingHit.hand());
            }
        }
    }

    private static void performHeavyAttackHit(ServerPlayer player, InteractionHand hand) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookAngle = player.getLookAngle().normalize();
        AABB searchBox = player.getBoundingBox()
                .expandTowards(lookAngle.scale(HEAVY_ATTACK_RANGE))
                .inflate(HEAVY_ATTACK_HALF_WIDTH, HEAVY_ATTACK_VERTICAL_TOLERANCE, HEAVY_ATTACK_HALF_WIDTH);
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * HEAVY_ATTACK_DAMAGE_MULTIPLIER
                + HEAVY_ATTACK_BONUS_DAMAGE;

        boolean hitAnyEntity = false;
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox, target -> canHeavyAttackHit(player, target))) {
            Vec3 targetOffset = target.getBoundingBox().getCenter().subtract(eyePosition);
            double forwardDistance = targetOffset.dot(lookAngle);
            if (forwardDistance < 0.25 || forwardDistance > HEAVY_ATTACK_RANGE) {
                continue;
            }

            Vec3 perpendicularOffset = targetOffset.subtract(lookAngle.scale(forwardDistance));
            if (perpendicularOffset.horizontalDistance() > HEAVY_ATTACK_HALF_WIDTH
                    || Math.abs(target.getY(0.5) - player.getEyeY()) > HEAVY_ATTACK_VERTICAL_TOLERANCE) {
                continue;
            }

            if (target.hurtServer(serverLevel, player.damageSources().playerAttack(player), damage)) {
                target.knockback(HEAVY_ATTACK_KNOCKBACK, player.getX() - target.getX(), player.getZ() - target.getZ());
                hitAnyEntity = true;
            }
        }

        if (hitAnyEntity) {
            player.sweepAttack();
            player.resetAttackStrengthTicker();
        }
    }

    private static boolean canHeavyAttackHit(Player player, LivingEntity target) {
        return target != player
                && target.isAlive()
                && target.isAttackable()
                && !target.skipAttackInteraction(player)
                && !player.isAlliedTo(target)
                && !(target instanceof Player targetPlayer && targetPlayer.isSpectator())
                && !(target instanceof ArmorStand armorStand && armorStand.isMarker());
    }

    private static void scheduleHeavyAttackHit(Player player, InteractionHand hand) {
        PENDING_HEAVY_ATTACK_HITS.put(player.getUUID(), new HeavyAttackHit(player.tickCount + HEAVY_ATTACK_DAMAGE_DELAY_TICKS, hand));
    }

    private static void resetHeavyAttackLocksForPlayersNotHolding(Collection<ServerPlayer> players) {
        HEAVY_ATTACK_LOCKED_UNTIL.entrySet().removeIf(entry -> players.stream()
                .noneMatch(player -> player.getUUID().equals(entry.getKey()) && isHoldingQingtian(player)));
        PENDING_HEAVY_ATTACK_HITS.entrySet().removeIf(entry -> players.stream()
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

    private record HeavyAttackHit(int hitTick, InteractionHand hand) {
    }
}
