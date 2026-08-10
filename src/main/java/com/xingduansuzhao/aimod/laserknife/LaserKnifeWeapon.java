package com.xingduansuzhao.aimod.laserknife;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

/** Uses the standard knife attack sounds with its own switch sound. */
public class LaserKnifeWeapon extends AnimatedWeaponItem {
    public LaserKnifeWeapon(Item.Properties properties) {
        super(
                "laser_knife",
                properties,
                AiMod.LASER_KNIFE_SWITCH,
                AiMod.KNIFE_HEAVY_ATTACK,
                AiMod.KNIFE_LIGHT_ATTACK_1,
                AiMod.KNIFE_LIGHT_ATTACK_2,
                true,
                false,
                false,
                6,
                8,
                8
        );
    }
}
