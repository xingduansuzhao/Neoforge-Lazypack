package com.xingduansuzhao.aimod.bamboocicada.event;

import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ToolsmithTradeHandler {
    private static final int MAX_USES_BEFORE_RESTOCK = 12;

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide() || !(event.getTarget() instanceof Villager villager)) {
            return;
        }
        if (villager.getVillagerData().profession() != VillagerProfession.TOOLSMITH) {
            return;
        }

        // getOffers() creates the villager's normal offers first. Injecting here,
        // immediately before vanilla opens the menu, makes this trade guaranteed
        // instead of leaving it to the random novice-trade selection.
        MerchantOffers offers = villager.getOffers();
        boolean alreadyPresent = offers.stream()
                .anyMatch(offer -> offer.getResult().is(AiMod.BAMBOO_CICADA.get()));
        if (!alreadyPresent) {
            offers.add(new MerchantOffer(
                    new ItemCost(AiMod.SPECIAL_EMERALD.get()),
                    new ItemStack(AiMod.BAMBOO_CICADA.get()),
                    MAX_USES_BEFORE_RESTOCK,
                    1,
                    0.0F
            ));
        }
    }

    private ToolsmithTradeHandler() {
    }
}
