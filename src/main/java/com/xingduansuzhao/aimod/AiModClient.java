package com.xingduansuzhao.aimod;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xingduansuzhao.aimod.qingtian.QingtianClientAnimations;
import com.xingduansuzhao.aimod.spiritring.client.SpiritRingItemEntityRenderer;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

@Mod(value = AiMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AiMod.MODID, value = Dist.CLIENT)
public class AiModClient {
    private static boolean isAnimatedWeaponMainHandActive;
    private static boolean hasTriggeredAnimatedWeaponSwitchAnimation;
    private static ItemStack activeAnimatedWeaponMainHandStack = ItemStack.EMPTY;

    public AiModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AiMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        AiMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        event.enqueueWork(QingtianClientAnimations::registerLayer);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.ITEM, SpiritRingItemEntityRenderer::new);
        AiMod.ANIMATED_WEAPON_ITEMS.forEach(item -> item.get().geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<?>> renderer = Suppliers.memoize(() -> new GeoItemRenderer<>(item.get()));

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                return this.renderer.get();
            }
        }));
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clearAnimatedWeaponSwitchState();
            QingtianClientAnimations.resetHeavyAttackLock();
            return;
        }

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        boolean isHoldingAnimatedWeapon = mainHandItem.getItem() instanceof AnimatedWeaponItem;
        boolean isHoldingAnimatedWeaponInEitherHand = isHoldingAnimatedWeapon
                || minecraft.player.getOffhandItem().getItem() instanceof AnimatedWeaponItem;
        if (!isHoldingAnimatedWeapon || isSwitchingToDifferentAnimatedWeapon(mainHandItem)) {
            stopAnimatedWeaponSwitchAnimation(minecraft);
            clearAnimatedWeaponSwitchState();
        }

        if (!isHoldingAnimatedWeaponInEitherHand) {
            QingtianClientAnimations.resetHeavyAttackLock();
        }
    }

    @SubscribeEvent
    static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem() || event.getHand() != InteractionHand.OFF_HAND || minecraft.player == null) {
            return;
        }

        if (minecraft.player.getMainHandItem().getItem() instanceof AnimatedWeaponItem) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getItemStack().isEmpty()
                || !(event.getItemStack().getItem() instanceof AnimatedWeaponItem weapon)) {
            return;
        }

        if (minecraft.player.getItemInHand(event.getHand()).getItem() != weapon) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                stopAnimatedWeaponSwitchAnimation(minecraft);
                clearAnimatedWeaponSwitchState();
            }

            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);

        boolean triggeredSwitchAnimationNow = event.getHand() == InteractionHand.MAIN_HAND
                && triggerAnimatedWeaponSwitchAnimation(minecraft, weapon, event.getItemStack());
        if (triggeredSwitchAnimationNow) {
            return;
        }

        renderAnimatedWeaponWithoutVanillaEquipAnimation(minecraft, event);
    }

    private static boolean triggerAnimatedWeaponSwitchAnimation(Minecraft minecraft, AnimatedWeaponItem weapon, ItemStack stack) {
        if (minecraft.player == null || minecraft.level == null || hasTriggeredAnimatedWeaponSwitchAnimation) {
            return false;
        }

        isAnimatedWeaponMainHandActive = true;
        activeAnimatedWeaponMainHandStack = stack.copy();
        weapon.triggerClientSwitchAnimation(minecraft.player, stack);
        hasTriggeredAnimatedWeaponSwitchAnimation = true;
        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                weapon.getSwitchSound(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f,
                false
        );
        AiMod.LOGGER.debug("Played animated weapon switch sound");
        return true;
    }

    private static void stopAnimatedWeaponSwitchAnimation(Minecraft minecraft) {
        if (minecraft.player != null && isAnimatedWeaponMainHandActive
                && activeAnimatedWeaponMainHandStack.getItem() instanceof AnimatedWeaponItem weapon) {
            weapon.stopClientSwitchAnimation(minecraft.player, activeAnimatedWeaponMainHandStack);
        }
    }

    private static void clearAnimatedWeaponSwitchState() {
        isAnimatedWeaponMainHandActive = false;
        hasTriggeredAnimatedWeaponSwitchAnimation = false;
        activeAnimatedWeaponMainHandStack = ItemStack.EMPTY;
    }

    private static boolean isSwitchingToDifferentAnimatedWeapon(ItemStack mainHandItem) {
        return isAnimatedWeaponMainHandActive
                && activeAnimatedWeaponMainHandStack.getItem() instanceof AnimatedWeaponItem
                && activeAnimatedWeaponMainHandStack.getItem() != mainHandItem.getItem();
    }

    private static void renderAnimatedWeaponWithoutVanillaEquipAnimation(Minecraft minecraft, RenderHandEvent event) {
        boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? minecraft.player.getMainArm() : minecraft.player.getMainArm().getOpposite();
        boolean isRightArm = arm == HumanoidArm.RIGHT;
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        applyAnimatedWeaponSwingTransform(poseStack, arm, event.getSwingProgress());
        minecraft.gameRenderer.itemInHandRenderer.renderItem(
                minecraft.player,
                event.getItemStack(),
                isRightArm ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                poseStack,
                event.getSubmitNodeCollector(),
                event.getPackedLight()
        );
        poseStack.popPose();
    }

    private static void applyAnimatedWeaponSwingTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float horizontalSwing = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        float verticalSwing = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
        float depthSwing = -0.2F * Mth.sin(swingProgress * (float) Math.PI);

        poseStack.translate(direction * horizontalSwing, verticalSwing, depthSwing);
        applyAnimatedWeaponArmTransform(poseStack, arm);
        applyAnimatedWeaponAttackTransform(poseStack, arm, swingProgress);
    }

    private static void applyAnimatedWeaponArmTransform(PoseStack poseStack, HumanoidArm arm) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(direction * 0.56F, -0.52F, -0.72F);
    }

    private static void applyAnimatedWeaponAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float swingSquared = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingRoot = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);

        poseStack.mulPose(Axis.YP.rotationDegrees(direction * (45.0F + swingSquared * -20.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction * swingRoot * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(swingRoot * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(direction * -45.0F));
    }
}
