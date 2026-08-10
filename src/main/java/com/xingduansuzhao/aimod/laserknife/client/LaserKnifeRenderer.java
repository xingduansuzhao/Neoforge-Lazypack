package com.xingduansuzhao.aimod.laserknife.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xingduansuzhao.aimod.laserknife.LaserKnifeWeapon;
import com.xingduansuzhao.aimod.weapon.client.AnimatedWeaponRenderer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.state.CameraRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/** Renders the weapon body normally while delegating the blade head to an emissive pass. */
public final class LaserKnifeRenderer extends AnimatedWeaponRenderer<LaserKnifeWeapon> {
    private static final String GLOWING_BONE = "head";
    private boolean renderingEmissiveHead;

    public LaserKnifeRenderer(LaserKnifeWeapon weapon) {
        super(weapon);
        withRenderLayer(new LaserKnifeEmissiveLayer(this));
    }

    @Override
    public void renderCubesOfBone(
            GeoRenderState renderState,
            GeoBone bone,
            PoseStack poseStack,
            VertexConsumer buffer,
            CameraRenderState cameraRenderState,
            int packedLight,
            int packedOverlay,
            int renderColor
    ) {
        boolean isHead = GLOWING_BONE.equals(bone.getName());
        if (this.renderingEmissiveHead != isHead) {
            return;
        }

        super.renderCubesOfBone(
                renderState,
                bone,
                poseStack,
                buffer,
                cameraRenderState,
                packedLight,
                packedOverlay,
                renderColor
        );
    }

    void renderEmissiveHead(
            GeoRenderState renderState,
            PoseStack.Pose submittedPose,
            BakedGeoModel bakedModel,
            VertexConsumer buffer,
            CameraRenderState cameraRenderState,
            int packedOverlay,
            int renderColor
    ) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(submittedPose);

        // GeckoLib's normal model task evaluates animations only when its deferred callback
        // executes. Re-evaluate this render state's manager here so the emissive geometry never
        // reads a pose left behind by another item (for example, an armor stand's static copy).
        getGeoModel().handleAnimations(createAnimationState(renderState));
        this.renderingEmissiveHead = true;

        try {
            for (GeoBone bone : bakedModel.topLevelBones()) {
                renderBone(
                        renderState,
                        poseStack,
                        bone,
                        buffer,
                        cameraRenderState,
                        LightTexture.FULL_BRIGHT,
                        packedOverlay,
                        renderColor
                );
            }
        } finally {
            this.renderingEmissiveHead = false;
        }
    }
}
