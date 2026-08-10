package com.xingduansuzhao.aimod.militaryshovel;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class MilitaryShovelWeapon extends AnimatedWeaponItem {
    public MilitaryShovelWeapon(Item.Properties properties) {
        this("military_shovel", properties);
    }

    public MilitaryShovelWeapon(String weaponId, Item.Properties properties) {
        super(
                weaponId,
                properties,
                AiMod.QINGTIAN_SWITCH,
                AiMod.QINGTIAN_HEAVY_ATTACK,
                AiMod.QINGTIAN_LIGHT_ATTACK_1,
                AiMod.QINGTIAN_LIGHT_ATTACK_2
        );
    }
}
