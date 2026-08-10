package com.xingduansuzhao.aimod.qingtian.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;
import com.xingduansuzhao.aimod.weapon.client.AnimatedWeaponRenderer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.state.CameraRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.base.GeoRenderState;

/** Adds a constant full-bright pass whose texture contains only the marked Qingtian pixels. */
public final class QingtianRenderer extends AnimatedWeaponRenderer<MyCustomWeapon> {
    public QingtianRenderer(MyCustomWeapon weapon) {
        super(weapon);
        withRenderLayer(new QingtianEmissiveLayer(this));
    }

    void renderEmissiveMask(
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

        // Render tasks are deferred in 1.21.10. Re-evaluating this instance at draw time keeps
        // the glow mask attached to the same held item and prevents armor-stand/player bleed.
        getGeoModel().handleAnimations(createAnimationState(renderState));

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
    }
}
