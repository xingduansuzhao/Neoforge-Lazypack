package com.xingduansuzhao.aimod.bamboocicada.client;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.bamboocicada.item.BambooCicadaItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BambooCicadaRenderer extends GeoItemRenderer<BambooCicadaItem> {
    private static final long GUI_INSTANCE_ID = Long.MIN_VALUE;

    public BambooCicadaRenderer(BambooCicadaItem item) {
        super(item);
    }

    @Override
    public long getInstanceId(BambooCicadaItem animatable, RenderData renderData) {
        // Inventory and hotbar rendering must not share the animated stack instance.
        // This reserved ID never receives the server-side animation trigger, so the
        // GUI copy remains in its static bind pose while both hand views still animate.
        if (renderData.renderPerspective() == ItemDisplayContext.GUI) {
            return GUI_INSTANCE_ID;
        }

        return super.getInstanceId(animatable, renderData);
    }
}
