package com.xingduansuzhao.aimod.bamboocicada.item;

import com.xingduansuzhao.aimod.bamboocicada.client.BambooCicadaRenderer;
import com.xingduansuzhao.aimod.bamboocicada.event.BambooCicadaLoopHandler;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class BambooCicadaItem extends Item implements GeoItem {
    public static final String CONTROLLER_NAME = "bamboo_cicada_controller";
    public static final String ROTATE_TRIGGER = "rotate";
    private static final RawAnimation ROTATE_ANIMATION = RawAnimation.begin().thenLoop("rotate");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);

    public BambooCicadaItem(Properties properties) {
        super(properties);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            BambooCicadaLoopHandler.toggle(serverPlayer, serverLevel, hand, stack, this);
        }

        // CONSUME sends the use action to the server without starting vanilla's
        // hand-swing animation. Only the authored GeckoLib animation should play.
        return InteractionResult.CONSUME;
    }

    public void startLoop(ServerPlayer player, ServerLevel level, ItemStack stack) {
        long instanceId = GeoItem.getOrAssignId(stack, level);
        triggerAnim(player, instanceId, CONTROLLER_NAME, ROTATE_TRIGGER);
    }

    public void stopLoop(Entity player, long instanceId) {
        stopTriggeredAnim(player, instanceId, CONTROLLER_NAME, ROTATE_TRIGGER);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(CONTROLLER_NAME, 0, state -> PlayState.STOP)
                .triggerableAnim(ROTATE_TRIGGER, ROTATE_ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BambooCicadaRenderer renderer;

            @Override
            @Nullable
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new BambooCicadaRenderer(BambooCicadaItem.this);
                }

                return this.renderer;
            }
        });
    }
}
