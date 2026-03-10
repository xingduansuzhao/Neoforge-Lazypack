package com.xingduansuzhao.aimod.spiritring.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;

public class SpiritRingItemEntityRenderer extends ItemEntityRenderer {

    private final RandomSource random = RandomSource.create();

    public SpiritRingItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public SpiritRingRenderState createRenderState() {
        return new SpiritRingRenderState();
    }

    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof SpiritRingRenderState spiritState) {
            spiritState.isSpiritRing = entity.getItem().is(AiMod.SPIRIT_RING.get());
            if (spiritState.isSpiritRing) {
                spiritState.savedAge = state.ageInTicks;
                spiritState.savedBob = state.bobOffset;
            }
        }
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state instanceof SpiritRingRenderState spiritState && spiritState.isSpiritRing) {
            if (state.item.isEmpty()) {
                return;
            }

            poseStack.pushPose();

            float bob = Mth.sin(spiritState.savedAge / 10.0f + spiritState.savedBob) * 0.1f + 0.1f;
            poseStack.translate(0.0, bob, 0.0);

            float spin = spiritState.savedAge * 2.0f;
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));

            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            float ringScale = 7.0f;
            float thicknessScale = 0.15f;
            poseStack.scale(ringScale, ringScale, thicknessScale);

            poseStack.translate(0.0, -0.125, 0.0);
            this.random.setSeed(state.seed);
            renderMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random);

            poseStack.popPose();
        } else {
            super.submit(state, poseStack, collector, cameraState);
        }
    }
}
