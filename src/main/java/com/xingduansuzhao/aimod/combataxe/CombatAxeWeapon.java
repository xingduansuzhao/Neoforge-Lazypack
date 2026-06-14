package com.xingduansuzhao.aimod.combataxe;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.item.Item;

public class CombatAxeWeapon extends AnimatedWeaponItem {
    public CombatAxeWeapon(Item.Properties properties) {
        super(
                "combat_axe",
                properties,
                AiMod.COMBAT_AXE_SWITCH,
                AiMod.COMBAT_AXE_LIGHT_ATTACK_1,
                AiMod.COMBAT_AXE_LIGHT_ATTACK_1,
                AiMod.COMBAT_AXE_LIGHT_ATTACK_2,
                false,
                true,
                true
        );
    }
}
