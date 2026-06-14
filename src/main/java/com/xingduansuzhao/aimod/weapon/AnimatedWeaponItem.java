package com.xingduansuzhao.aimod.weapon;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
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

public class AnimatedWeaponItem extends Item implements GeoItem {
    private static final String TRIGGER_HEAVY_ATTACK = "heavy_attack";
    private static final String TRIGGER_SWITCH = "switch";
    private static final String TRIGGER_LIGHT_ATTACK_1 = "light_attack1";
    private static final String TRIGGER_LIGHT_ATTACK_2 = "light_attack2";
    private static final RawAnimation HEAVY_ATTACK = RawAnimation.begin().thenPlay("heavy_attack");
    private static final RawAnimation SWITCH = RawAnimation.begin().thenPlay("switch");
    private static final RawAnimation LIGHT_ATTACK_1 = RawAnimation.begin().thenPlay("light_attack1");
    private static final RawAnimation LIGHT_ATTACK_2 = RawAnimation.begin().thenPlay("light_attack2");
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
    private static final Map<UUID, Boolean> NEXT_LIGHT_ATTACK_USES_SECOND_ANIMATION = new ConcurrentHashMap<>();

    public final MutableObject<GeoRenderProvider> geoRenderProvider = new MutableObject<>();

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final String controllerName;
    private final Supplier<? extends SoundEvent> switchSound;
    private final Supplier<? extends SoundEvent> heavyAttackSound;
    private final Supplier<? extends SoundEvent> lightAttackSound1;
    private final Supplier<? extends SoundEvent> lightAttackSound2;
    private final boolean heavyAttackEnabled;
    private final boolean lightAttackAnimationsEnabled;
    private final boolean suppressVanillaLightSwing;

    public AnimatedWeaponItem(
            String weaponId,
            Properties properties,
            Supplier<? extends SoundEvent> switchSound,
            Supplier<? extends SoundEvent> heavyAttackSound,
            Supplier<? extends SoundEvent> lightAttackSound1,
            Supplier<? extends SoundEvent> lightAttackSound2
    ) {
        this(
                weaponId,
                properties,
                switchSound,
                heavyAttackSound,
                lightAttackSound1,
                lightAttackSound2,
                true,
                false,
                false
        );
    }

    public AnimatedWeaponItem(
            String weaponId,
            Properties properties,
            Supplier<? extends SoundEvent> switchSound,
            Supplier<? extends SoundEvent> heavyAttackSound,
            Supplier<? extends SoundEvent> lightAttackSound1,
            Supplier<? extends SoundEvent> lightAttackSound2,
            boolean heavyAttackEnabled,
            boolean lightAttackAnimationsEnabled,
            boolean suppressVanillaLightSwing
    ) {
        super(properties);
        this.controllerName = weaponId + "_first_person_controller";
        this.switchSound = switchSound;
        this.heavyAttackSound = heavyAttackSound;
        this.lightAttackSound1 = lightAttackSound1;
        this.lightAttackSound2 = lightAttackSound2;
        this.heavyAttackEnabled = heavyAttackEnabled;
        this.lightAttackAnimationsEnabled = lightAttackAnimationsEnabled;
        this.suppressVanillaLightSwing = suppressVanillaLightSwing;
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!this.heavyAttackEnabled) {
            return super.use(level, player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && lockHeavyAttack(player)) {
            long instanceId = GeoItem.getOrAssignId(stack, serverLevel);
            triggerAnim(player, instanceId, this.controllerName, TRIGGER_HEAVY_ATTACK);
            scheduleHeavyAttackHit(player, hand);
            playServerSound(serverLevel, player, this.heavyAttackSound.get());
        }

        if (level.isClientSide()) {
            onClientHeavyAttack(player);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && entity instanceof Player player) {
            if (this.lightAttackAnimationsEnabled && entity.level().isClientSide()) {
                boolean useSecondAnimation = NEXT_LIGHT_ATTACK_USES_SECOND_ANIMATION.getOrDefault(player.getUUID(), false);
                triggerLightAttackAnimation(player, stack, useSecondAnimation);
                NEXT_LIGHT_ATTACK_USES_SECOND_ANIMATION.put(player.getUUID(), !useSecondAnimation);
            }

            if (entity.level() instanceof ServerLevel serverLevel) {
                boolean useSecondSound = NEXT_LIGHT_ATTACK_USES_SECOND_SOUND.getOrDefault(player.getUUID(), false);
                if (this.lightAttackAnimationsEnabled) {
                    triggerLightAttackAnimation(player, stack, useSecondSound);
                }

                playServerSound(serverLevel, player, useSecondSound ? this.lightAttackSound2.get() : this.lightAttackSound1.get());
                NEXT_LIGHT_ATTACK_USES_SECOND_SOUND.put(player.getUUID(), !useSecondSound);
            }
        }

        return this.suppressVanillaLightSwing && hand == InteractionHand.MAIN_HAND;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(this.geoRenderProvider.getValue());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this.controllerName, state -> PlayState.STOP)
                .triggerableAnim(TRIGGER_HEAVY_ATTACK, HEAVY_ATTACK)
                .triggerableAnim(TRIGGER_SWITCH, SWITCH)
                .triggerableAnim(TRIGGER_LIGHT_ATTACK_1, LIGHT_ATTACK_1)
                .triggerableAnim(TRIGGER_LIGHT_ATTACK_2, LIGHT_ATTACK_2));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    public SoundEvent getSwitchSound() {
        return this.switchSound.get();
    }

    public void triggerClientSwitchAnimation(Player player, ItemStack stack) {
        triggerAnim(player, GeoItem.getId(stack), this.controllerName, TRIGGER_SWITCH);
    }

    public void stopClientSwitchAnimation(Player player, ItemStack stack) {
        stopTriggeredAnim(player, GeoItem.getId(stack), this.controllerName, TRIGGER_SWITCH);
    }

    protected void onClientHeavyAttack(Player player) {
    }

    private void triggerLightAttackAnimation(Player player, ItemStack stack, boolean useSecondAnimation) {
        long instanceId = player.level() instanceof ServerLevel serverLevel
                ? GeoItem.getOrAssignId(stack, serverLevel)
                : GeoItem.getId(stack);
        triggerAnim(player, instanceId, this.controllerName, useSecondAnimation ? TRIGGER_LIGHT_ATTACK_2 : TRIGGER_LIGHT_ATTACK_1);
    }

    public static void tickServerPlayers(Collection<ServerPlayer> players) {
        processPendingHeavyAttackHits(players);
        resetHeavyAttackLocksForPlayersNotHolding(players);
        PENDING_HEAVY_ATTACK_HITS.keySet().removeIf(uuid -> players.stream()
                .noneMatch(player -> player.getUUID().equals(uuid)));
    }

    public static boolean isHoldingAnimatedWeapon(Player player) {
        return player.getMainHandItem().getItem() instanceof AnimatedWeaponItem
                || player.getOffhandItem().getItem() instanceof AnimatedWeaponItem;
    }

    private static void processPendingHeavyAttackHits(Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            HeavyAttackHit pendingHit = PENDING_HEAVY_ATTACK_HITS.get(player.getUUID());
            if (pendingHit == null || player.tickCount < pendingHit.hitTick()) {
                continue;
            }

            PENDING_HEAVY_ATTACK_HITS.remove(player.getUUID());
            if (isHoldingAnimatedWeapon(player) && player.isAlive() && !player.isSpectator()) {
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
                .noneMatch(player -> player.getUUID().equals(entry.getKey()) && isHoldingAnimatedWeapon(player)));
        PENDING_HEAVY_ATTACK_HITS.entrySet().removeIf(entry -> players.stream()
                .noneMatch(player -> player.getUUID().equals(entry.getKey()) && isHoldingAnimatedWeapon(player)));
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

    private static void playServerSound(ServerLevel level, Player player, SoundEvent sound) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private record HeavyAttackHit(int hitTick, InteractionHand hand) {
    }
}
