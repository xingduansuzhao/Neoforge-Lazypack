package com.xingduansuzhao.aimod.weapon;

import java.util.function.Supplier;

import com.xingduansuzhao.aimod.AiMod;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;

/** A configurable animated weapon for models that share an existing sound set. */
public class SharedAnimatedWeapon extends AnimatedWeaponItem {
    public static SharedAnimatedWeapon withMilitaryShovelSounds(String weaponId, Item.Properties properties) {
        return new SharedAnimatedWeapon(
                weaponId,
                properties,
                AiMod.QINGTIAN_SWITCH,
                AiMod.QINGTIAN_HEAVY_ATTACK,
                AiMod.QINGTIAN_LIGHT_ATTACK_1,
                AiMod.QINGTIAN_LIGHT_ATTACK_2
        );
    }

    public SharedAnimatedWeapon(
            String weaponId,
            Item.Properties properties,
            Supplier<? extends SoundEvent> switchSound,
            Supplier<? extends SoundEvent> heavyAttackSound,
            Supplier<? extends SoundEvent> lightAttackSound1,
            Supplier<? extends SoundEvent> lightAttackSound2
    ) {
        super(weaponId, properties, switchSound, heavyAttackSound, lightAttackSound1, lightAttackSound2);
    }
}
