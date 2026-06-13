package com.xingduansuzhao.aimod.kris;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class KrisWeapon extends AnimatedWeaponItem {
    public KrisWeapon(Item.Properties properties) {
        super(
                "kris",
                properties,
                AiMod.KRIS_SWITCH,
                AiMod.KRIS_HEAVY_ATTACK,
                AiMod.KRIS_LIGHT_ATTACK_1,
                AiMod.KRIS_LIGHT_ATTACK_2
        );
    }
}
