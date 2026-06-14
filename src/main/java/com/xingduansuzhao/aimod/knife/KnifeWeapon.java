package com.xingduansuzhao.aimod.knife;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class KnifeWeapon extends AnimatedWeaponItem {
    public KnifeWeapon(Item.Properties properties) {
        super(
                "knife",
                properties,
                AiMod.KNIFE_SWITCH,
                AiMod.KNIFE_HEAVY_ATTACK,
                AiMod.KNIFE_LIGHT_ATTACK_1,
                AiMod.KNIFE_LIGHT_ATTACK_2
        );
    }
}
