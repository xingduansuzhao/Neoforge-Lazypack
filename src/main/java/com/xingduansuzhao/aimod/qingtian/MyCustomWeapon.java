package com.xingduansuzhao.aimod.qingtian;

import java.util.function.Consumer;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

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

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
        tooltipAdder.accept(Component.translatable("item.yaoniming3000.qingtian.tooltip.transform")
                .withStyle(ChatFormatting.GOLD));
        tooltipAdder.accept(Component.translatable("item.yaoniming3000.qingtian.tooltip.restore")
                .withStyle(ChatFormatting.GREEN));
    }
}
