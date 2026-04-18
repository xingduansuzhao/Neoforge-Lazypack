package com.xingduansuzhao.aimod;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = AiMod.MODID)
public class ChocolateVillagerTrades {

    @SubscribeEvent
    public static void onVillagerTrades(VillagerTradesEvent event) {
        if (event.getType() != VillagerProfession.FARMER) {
            return;
        }

        List<VillagerTrades.ItemListing> level1 = event.getTrades().get(1);

        level1.add(new BasicItemListing(
                new ItemStack(Items.COCOA_BEANS, 16),
                new ItemStack(Items.EMERALD, 64),
                new ItemStack(AiMod.CHOCOLATE_CAKE.get(), 1),
                12, 5, 0.05f
        ));

        level1.add(new BasicItemListing(
                new ItemStack(Items.COCOA_BEANS, 8),
                new ItemStack(Items.EMERALD, 48),
                new ItemStack(AiMod.CHOCOLATE_MILK_BUCKET.get(), 1),
                12, 5, 0.05f
        ));

        level1.add(new BasicItemListing(
                new ItemStack(Items.COCOA_BEANS, 12),
                new ItemStack(Items.EMERALD, 32),
                new ItemStack(AiMod.CHOCOLATE_DIRTY_BUN.get(), 1),
                12, 5, 0.05f
        ));

        level1.add(new LockedTradeListing(
                new ItemCost(Items.COCOA_BEANS, 2),
                new ItemCost(Items.EMERALD, 16),
                new ItemStack(AiMod.CHOCOLATE_COOKIE.get(), 2)
        ));
    }

    private static class LockedTradeListing implements VillagerTrades.ItemListing {
        private final ItemCost priceA;
        private final ItemCost priceB;
        private final ItemStack result;

        LockedTradeListing(ItemCost priceA, ItemCost priceB, ItemStack result) {
            this.priceA = priceA;
            this.priceB = priceB;
            this.result = result;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource random) {
            MerchantOffer offer = new MerchantOffer(
                    priceA,
                    Optional.of(priceB),
                    result.copy(),
                    0, 0, 0, 0.05f
            );
            offer.setToOutOfStock();
            return offer;
        }
    }
}
