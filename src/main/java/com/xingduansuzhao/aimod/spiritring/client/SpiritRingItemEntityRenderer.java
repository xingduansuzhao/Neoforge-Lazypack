package com.xingduansuzhao.aimod.spiritring.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.HashSet;
import java.util.Set;

public class SpiritRingItemEntityRenderer extends ItemEntityRenderer {

    private final RandomSource random = RandomSource.create();
    private static final Set<Item> SPECIAL_ITEM_SET = new HashSet<>();

    public SpiritRingItemEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static Set<Item> getSpecialItems() {
        if (SPECIAL_ITEM_SET.isEmpty()) {
            for (DeferredItem<? extends Item> item : AiMod.ALL_SPECIAL_ITEMS) {
                SPECIAL_ITEM_SET.add(item.get());
            }
        }
        return SPECIAL_ITEM_SET;
    }

    @Override
    public SpiritRingRenderState createRenderState() {
        return new SpiritRingRenderState();
    }

    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (state instanceof SpiritRingRenderState spiritState) {
            spiritState.isSpecialItem = getSpecialItems().contains(entity.getItem().getItem());
        }
    }

    @Override
    public void submit(ItemEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state instanceof SpiritRingRenderState spiritState && spiritState.isSpecialItem) {
            if (state.item.isEmpty()) {
                return;
            }

            poseStack.pushPose();

            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            float scale = 5.0f;
            poseStack.scale(scale, scale, scale);

            poseStack.translate(0.0, -0.125, 0.0);
            this.random.setSeed(state.seed);
            renderMultipleFromCount(poseStack, collector, state.lightCoords, state, this.random);

            poseStack.popPose();
        } else {
            super.submit(state, poseStack, collector, cameraState);
        }
    }
}
