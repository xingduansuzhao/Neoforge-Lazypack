package com.xingduansuzhao.aimod.spiritring;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class SpiritRingItem extends Item {

    public SpiritRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
