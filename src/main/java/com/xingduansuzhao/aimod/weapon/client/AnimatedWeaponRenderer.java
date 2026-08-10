package com.xingduansuzhao.aimod.weapon.client;

import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** Keeps displayed armor-stand weapons isolated from player-held animation instances. */
public class AnimatedWeaponRenderer<T extends AnimatedWeaponItem> extends GeoItemRenderer<T> {
    private static final int PERSPECTIVE_BITS = 8;

    public AnimatedWeaponRenderer(T weapon) {
        super(weapon);
    }

    @Override
    public long getInstanceId(T weapon, RenderData renderData) {
        ItemOwner itemOwner = renderData.itemOwner();
        LivingEntity livingOwner = itemOwner == null ? null : itemOwner.asLivingEntity();

        if (livingOwner instanceof ArmorStand armorStand) {
            return armorStandInstanceId(armorStand, renderData.renderPerspective());
        }

        return super.getInstanceId(weapon, renderData);
    }

    private static long armorStandInstanceId(ArmorStand armorStand, ItemDisplayContext perspective) {
        long entityId = Integer.toUnsignedLong(armorStand.getId());

        // Stack animation IDs are non-negative. Reserve the negative range for static armor-stand
        // managers and include the perspective so main/offhand weapons never share a controller.
        return Long.MIN_VALUE | (entityId << PERSPECTIVE_BITS) | perspective.ordinal();
    }
}
