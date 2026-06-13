package com.xingduansuzhao.aimod;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;
import com.xingduansuzhao.aimod.qingtian.QingtianClientAnimations;
import com.xingduansuzhao.aimod.spiritring.client.SpiritRingItemEntityRenderer;
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
    private static boolean isQingtianMainHandActive;
    private static boolean hasTriggeredQingtianSwitchAnimation;
    private static ItemStack activeQingtianMainHandStack = ItemStack.EMPTY;

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
        AiMod.QINGTIAN.get().geoRenderProvider.setValue(new GeoRenderProvider() {
            private final Supplier<GeoItemRenderer<?>> renderer = Suppliers.memoize(() -> new GeoItemRenderer<>(AiMod.QINGTIAN.get()));

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                return this.renderer.get();
            }
        });
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            clearQingtianSwitchState();
            QingtianClientAnimations.resetHeavyAttackLock();
            return;
        }

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        boolean isHoldingQingtian = mainHandItem.is(AiMod.QINGTIAN.get());
        boolean isHoldingQingtianInEitherHand = isHoldingQingtian
                || minecraft.player.getOffhandItem().is(AiMod.QINGTIAN.get());
        if (!isHoldingQingtian) {
            stopQingtianSwitchAnimation(minecraft);
            clearQingtianSwitchState();
        }

        if (!isHoldingQingtianInEitherHand) {
            QingtianClientAnimations.resetHeavyAttackLock();
        }
    }

    @SubscribeEvent
    static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.isUseItem() || event.getHand() != InteractionHand.OFF_HAND || minecraft.player == null) {
            return;
        }

        if (minecraft.player.getMainHandItem().is(AiMod.QINGTIAN.get())) {
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || event.getItemStack().isEmpty() || !event.getItemStack().is(AiMod.QINGTIAN.get())) {
            return;
        }

        if (!minecraft.player.getItemInHand(event.getHand()).is(AiMod.QINGTIAN.get())) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                stopQingtianSwitchAnimation(minecraft);
                clearQingtianSwitchState();
            }

            event.setCanceled(true);
            return;
        }

        event.setCanceled(true);

        boolean triggeredSwitchAnimationNow = event.getHand() == InteractionHand.MAIN_HAND
                && triggerQingtianSwitchAnimation(minecraft, event.getItemStack());
        if (triggeredSwitchAnimationNow) {
            return;
        }

        renderQingtianWithoutVanillaEquipAnimation(minecraft, event);
    }

    private static boolean triggerQingtianSwitchAnimation(Minecraft minecraft, ItemStack stack) {
        if (minecraft.player == null || hasTriggeredQingtianSwitchAnimation || !stack.is(AiMod.QINGTIAN.get())) {
            return false;
        }

        isQingtianMainHandActive = true;
        activeQingtianMainHandStack = stack.copy();
        MyCustomWeapon.triggerClientSwitchAnimation(minecraft.player, stack);
        hasTriggeredQingtianSwitchAnimation = true;
        minecraft.level.playLocalSound(
                minecraft.player.getX(),
                minecraft.player.getY(),
                minecraft.player.getZ(),
                AiMod.QINGTIAN_SWITCH.get(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f,
                false
        );
        AiMod.LOGGER.debug("Played qingtian switch sound");
        return true;
    }

    private static void stopQingtianSwitchAnimation(Minecraft minecraft) {
        if (minecraft.player != null && isQingtianMainHandActive && !activeQingtianMainHandStack.isEmpty()) {
            MyCustomWeapon.stopClientSwitchAnimation(minecraft.player, activeQingtianMainHandStack);
        }
    }

    private static void clearQingtianSwitchState() {
        isQingtianMainHandActive = false;
        hasTriggeredQingtianSwitchAnimation = false;
        activeQingtianMainHandStack = ItemStack.EMPTY;
    }

    private static void renderQingtianWithoutVanillaEquipAnimation(Minecraft minecraft, RenderHandEvent event) {
        boolean isMainHand = event.getHand() == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? minecraft.player.getMainArm() : minecraft.player.getMainArm().getOpposite();
        boolean isRightArm = arm == HumanoidArm.RIGHT;
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        applyQingtianSwingTransform(poseStack, arm, event.getSwingProgress());
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

    private static void applyQingtianSwingTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float horizontalSwing = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
        float verticalSwing = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
        float depthSwing = -0.2F * Mth.sin(swingProgress * (float) Math.PI);

        poseStack.translate(direction * horizontalSwing, verticalSwing, depthSwing);
        applyQingtianArmTransform(poseStack, arm);
        applyQingtianAttackTransform(poseStack, arm, swingProgress);
    }

    private static void applyQingtianArmTransform(PoseStack poseStack, HumanoidArm arm) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        poseStack.translate(direction * 0.56F, -0.52F, -0.72F);
    }

    private static void applyQingtianAttackTransform(PoseStack poseStack, HumanoidArm arm, float swingProgress) {
        int direction = arm == HumanoidArm.RIGHT ? 1 : -1;
        float swingSquared = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
        float swingRoot = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);

        poseStack.mulPose(Axis.YP.rotationDegrees(direction * (45.0F + swingSquared * -20.0F)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(direction * swingRoot * -20.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(swingRoot * -80.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(direction * -45.0F));
    }
}
