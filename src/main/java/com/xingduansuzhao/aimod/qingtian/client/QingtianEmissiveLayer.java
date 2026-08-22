package com.xingduansuzhao.aimod.qingtian.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Full-time fixed-brightness overlay; transparent pixels in the mask emit no geometry. */
public final class QingtianEmissiveLayer extends AutoGlowingGeoLayer<
        MyCustomWeapon, GeoItemRenderer.RenderData, GeoRenderState> {
    private static final long PULSE_PERIOD_MILLIS = 1800L;
    private static final float MIN_PULSE_INTENSITY = 0.65F;
    private static final ResourceLocation GLOW_MASK = ResourceLocation.fromNamespaceAndPath(
            AiMod.MODID, "textures/item/qingtian_glowmask.png");
    public QingtianEmissiveLayer(QingtianRenderer renderer) {
        super(renderer);
    }

    @Override
    protected ResourceLocation getTextureResource(GeoRenderState renderState) {
        return GLOW_MASK;
    }

    /**
     * Use vanilla's eyes pipeline instead of GeckoLib's custom emissive pipeline.
     * Iris maps this pipeline to its full-bright entity path when shader packs are active.
     */
    @Override
    protected RenderType getRenderType(GeoRenderState renderState) {
        return RenderType.eyes(GLOW_MASK);
    }

    @Override
    public void submitRenderTask(
            GeoRenderState renderState,
            PoseStack poseStack,
            BakedGeoModel bakedModel,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            int packedLight,
            int packedOverlay,
            int renderColor,
            boolean isReRender
    ) {
        var emissiveRenderType = getRenderType(renderState);
        if (emissiveRenderType == null) {
            return;
        }

        this.renderer.buildRenderTask(
                renderState,
                poseStack,
                bakedModel,
                getGeoModel(),
                submitNodeCollector.order(1),
                cameraRenderState,
                emissiveRenderType,
                getBrightness(renderState),
                packedOverlay,
                getPulseRenderColor(renderColor),
                null
        );
    }

    private static int getPulseRenderColor(int renderColor) {
        float phase = (Util.getMillis() % PULSE_PERIOD_MILLIS) / (float) PULSE_PERIOD_MILLIS;
        float wave = 0.5F + 0.5F * Mth.sin(phase * Mth.TWO_PI);
        float intensity = MIN_PULSE_INTENSITY + (1.0F - MIN_PULSE_INTENSITY) * wave;

        int alpha = (renderColor >>> 24) & 0xFF;
        int red = (renderColor >>> 16) & 0xFF;
        int green = (renderColor >>> 8) & 0xFF;
        int blue = renderColor & 0xFF;

        return ((int) (alpha * intensity) << 24)
                | ((int) (red * intensity) << 16)
                | ((int) (green * intensity) << 8)
                | (int) (blue * intensity);
    }
}
