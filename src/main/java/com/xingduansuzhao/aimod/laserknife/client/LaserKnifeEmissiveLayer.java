package com.xingduansuzhao.aimod.laserknife.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.laserknife.LaserKnifeWeapon;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** Submits a fixed-brightness head pass that evaluates its own animation state at draw time. */
public final class LaserKnifeEmissiveLayer extends GeoRenderLayer<
        LaserKnifeWeapon, GeoItemRenderer.RenderData, GeoRenderState> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AiMod.MODID, "textures/item/laser_knife.png");
    private final LaserKnifeRenderer laserKnifeRenderer;

    public LaserKnifeEmissiveLayer(LaserKnifeRenderer renderer) {
        super(renderer);
        this.laserKnifeRenderer = renderer;
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
                RenderType.eyes(TEXTURE),
                (submittedPose, buffer) -> this.laserKnifeRenderer.renderEmissiveHead(
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
