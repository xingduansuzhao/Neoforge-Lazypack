package com.xingduansuzhao.aimod.bamboocicada.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = AiMod.MODID, value = Dist.CLIENT)
public final class BambooCicadaInputHandler {
    private BambooCicadaInputHandler() {
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
