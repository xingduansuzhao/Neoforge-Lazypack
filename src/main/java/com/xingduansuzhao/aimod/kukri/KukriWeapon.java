package com.xingduansuzhao.aimod.kukri;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class KukriWeapon extends AnimatedWeaponItem {
    public KukriWeapon(Item.Properties properties) {
        super(
                "kukri",
                properties,
                AiMod.KUKRI_SWITCH,
                AiMod.KUKRI_HEAVY_ATTACK,
                AiMod.KUKRI_LIGHT_ATTACK_1,
                AiMod.KUKRI_LIGHT_ATTACK_2
        );
    }
}
