package com.xingduansuzhao.aimod.bamboocicada.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public enum SpecialEmeraldDecorator implements IItemDecorator {
    INSTANCE;

    private static final float DISPLAY_SCALE = 0.50F;

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        guiGraphics.pose().pushMatrix();
        // Anchor the scaled label to the slot's bottom-right corner so the
        // fictional amount remains inside a 16x16 item cell.
        guiGraphics.pose().translate(xOffset + 16.0F, yOffset + 16.0F);
        guiGraphics.pose().scale(DISPLAY_SCALE, DISPLAY_SCALE);
        String displayCount = Component.translatable("item.bamboo_cicada.special_emerald.count").getString();
        guiGraphics.drawString(
                font,
                displayCount,
                -font.width(displayCount),
                -font.lineHeight,
                0xFFFFFF,
                true
        );
        guiGraphics.pose().popMatrix();
        return false;
    }
}
