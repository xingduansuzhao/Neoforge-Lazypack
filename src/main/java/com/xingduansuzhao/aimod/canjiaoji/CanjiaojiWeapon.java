package com.xingduansuzhao.aimod.canjiaoji;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class CanjiaojiWeapon extends AnimatedWeaponItem {
    public CanjiaojiWeapon(Item.Properties properties) {
        super(
                "canjiaoji",
                properties,
                AiMod.CANJIAOJI_SWITCH,
                AiMod.CANJIAOJI_HEAVY_ATTACK,
                AiMod.CANJIAOJI_LIGHT_ATTACK_1,
                AiMod.CANJIAOJI_LIGHT_ATTACK_2
        );
    }
}
