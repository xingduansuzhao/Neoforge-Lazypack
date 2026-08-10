package com.xingduansuzhao.aimod.qingtian.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Fixed-brightness overlay; transparent pixels in the mask emit no geometry. */
public final class QingtianEmissiveLayer extends GeoRenderLayer<
        MyCustomWeapon, GeoItemRenderer.RenderData, GeoRenderState> {
    private static final ResourceLocation GLOW_MASK = ResourceLocation.fromNamespaceAndPath(
            AiMod.MODID, "textures/item/qingtian_glowmask.png");
    private final QingtianRenderer qingtianRenderer;

    public QingtianEmissiveLayer(QingtianRenderer renderer) {
        super(renderer);
        this.qingtianRenderer = renderer;
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
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderType.eyes(GLOW_MASK),
                (submittedPose, buffer) -> this.qingtianRenderer.renderEmissiveMask(
                        renderState,
                        submittedPose,
                        bakedModel,
                        buffer,
                        cameraRenderState,
                        packedOverlay,
                        renderColor
                )
        );
    }
}
