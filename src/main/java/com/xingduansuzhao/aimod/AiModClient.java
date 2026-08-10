package com.xingduansuzhao.aimod;

import com.mojang.blaze3d.platform.InputConstants;
import com.xingduansuzhao.aimod.bamboocicada.client.BambooCicadaSoundManager;
import com.xingduansuzhao.aimod.bamboocicada.client.SpecialEmeraldDecorator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

@Mod(value = AiMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = AiMod.MODID, value = Dist.CLIENT)
public class AiModClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        AiMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        AiMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(AiMod.SPECIAL_EMERALD.get(), SpecialEmeraldDecorator.INSTANCE);
    }

    @SubscribeEvent
    static void interruptBambooCicadaAnimation(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != InputConstants.PRESS
                || (event.getButton() != 0 && event.getButton() != 1)) {
            return;
        }

        InteractionHand interruptedHand = BambooCicadaSoundManager.interruptLocalAnimation();
        if (interruptedHand == null) {
            return;
        }

        if (event.getButton() == 1) {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.swing(interruptedHand);
            }
            event.setCanceled(true);
        }
    }
}
