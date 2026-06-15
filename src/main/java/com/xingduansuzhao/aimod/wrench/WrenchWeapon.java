package com.xingduansuzhao.aimod.wrench;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class WrenchWeapon extends AnimatedWeaponItem {
    public WrenchWeapon(Item.Properties properties) {
        super(
                "wrench",
                properties,
                AiMod.WRENCH_SWITCH,
                AiMod.WRENCH_HEAVY_ATTACK,
                AiMod.WRENCH_LIGHT_ATTACK_1,
                AiMod.WRENCH_LIGHT_ATTACK_2,
                true,
                false,
                false,
                6,
                8,
                11
        );
    }
}
