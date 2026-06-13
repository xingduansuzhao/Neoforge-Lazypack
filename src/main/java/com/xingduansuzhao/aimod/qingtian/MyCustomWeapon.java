package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class MyCustomWeapon extends AnimatedWeaponItem {
    public MyCustomWeapon(Item.Properties properties) {
        super(
                "qingtian",
                properties,
                AiMod.QINGTIAN_SWITCH,
                AiMod.QINGTIAN_HEAVY_ATTACK,
                AiMod.QINGTIAN_LIGHT_ATTACK_1,
                AiMod.QINGTIAN_LIGHT_ATTACK_2
        );
    }

    @Override
    protected void onClientHeavyAttack(Player player) {
        QingtianClientAnimations.playHeavyAttack(player);
    }
}
