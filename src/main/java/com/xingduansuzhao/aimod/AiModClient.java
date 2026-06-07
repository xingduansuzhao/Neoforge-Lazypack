package com.xingduansuzhao.aimod;

import com.google.common.base.Suppliers;
import com.xingduansuzhao.aimod.spiritring.client.SpiritRingItemEntityRenderer;
import com.xingduansuzhao.aimod.qingtian.QingtianClientAnimations;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
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
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoItemRenderer;

import java.util.function.Supplier;

@Mod(value = AiMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AiMod.MODID, value = Dist.CLIENT)
public class AiModClient {
    private static boolean hasObservedMainHand;
    private static boolean wasHoldingQingtian;

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
            hasObservedMainHand = false;
            wasHoldingQingtian = false;
            QingtianClientAnimations.resetHeavyAttackLock();
            return;
        }

        ItemStack mainHandItem = minecraft.player.getMainHandItem();
        boolean isHoldingQingtian = mainHandItem.is(AiMod.QINGTIAN.get());
        boolean isHoldingQingtianInEitherHand = isHoldingQingtian
                || minecraft.player.getOffhandItem().is(AiMod.QINGTIAN.get());
        if (!isHoldingQingtianInEitherHand) {
            QingtianClientAnimations.resetHeavyAttackLock();
        }

        if (hasObservedMainHand && isHoldingQingtian && !wasHoldingQingtian) {
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
        }
        hasObservedMainHand = true;
        wasHoldingQingtian = isHoldingQingtian;
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
}
