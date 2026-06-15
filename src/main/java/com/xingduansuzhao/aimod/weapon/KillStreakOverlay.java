package com.xingduansuzhao.aimod.weapon;

import com.xingduansuzhao.aimod.AiMod;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class KillStreakOverlay {
    private static final int ICON_COUNT = 6;
    private static final int DISPLAY_DURATION_MS = 2000;
    private static final int FADE_OUT_MS = 500;
    private static final int ICON_SIZE = 32;
    private static final int RENDER_SIZE = 80;

    private static final ResourceLocation[] ICON_TEXTURES = new ResourceLocation[ICON_COUNT];

    static {
        for (int i = 0; i < ICON_COUNT; i++) {
            ICON_TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "textures/gui/killstreak/" + (i + 1) + ".png");
        }
    }

    private static int currentIconIndex = -1;
    private static long showStartTime = -1;

    public static void triggerIcon(int streakIndex) {
        currentIconIndex = Math.min(streakIndex, ICON_COUNT - 1);
        showStartTime = System.currentTimeMillis();
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (currentIconIndex < 0 || showStartTime < 0) {
            return;
        }

        long elapsed = System.currentTimeMillis() - showStartTime;

        if (elapsed > DISPLAY_DURATION_MS) {
            currentIconIndex = -1;
            showStartTime = -1;
            return;
        }

        float alpha = 1.0f;
        if (elapsed > DISPLAY_DURATION_MS - FADE_OUT_MS) {
            alpha = (float) (DISPLAY_DURATION_MS - elapsed) / FADE_OUT_MS;
        }

        float scaleProgress = Math.min(1.0f, elapsed / 100.0f);
        float scale = 0.5f + 0.5f * scaleProgress;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int renderSize = (int) (RENDER_SIZE * scale);
        int x = (screenWidth - renderSize) / 2;
        int y = screenHeight / 2 + screenHeight / 6;

        int color = ARGB.colorFromFloat(alpha, 1.0f, 1.0f, 1.0f);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                ICON_TEXTURES[currentIconIndex],
                x, y,
                0f, 0f,
                renderSize, renderSize,
                ICON_SIZE, ICON_SIZE,
                ICON_SIZE, ICON_SIZE,
                color
        );
    }

    public static void reset() {
        currentIconIndex = -1;
        showStartTime = -1;
    }
}
