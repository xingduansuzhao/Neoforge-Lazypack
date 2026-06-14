package com.xingduansuzhao.aimod.baseballbat;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class BaseballBatWeapon extends AnimatedWeaponItem {
    public BaseballBatWeapon(Item.Properties properties) {
        super(
                "baseball_bat",
                properties,
                AiMod.BASEBALL_BAT_SWITCH,
                AiMod.BASEBALL_BAT_HEAVY_ATTACK,
                AiMod.BASEBALL_BAT_LIGHT_ATTACK_1,
                AiMod.BASEBALL_BAT_LIGHT_ATTACK_2
        );
    }
}
