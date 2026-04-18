package com.xingduansuzhao.aimod;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = AiMod.MODID)
public class SpecialItemDropHandler {

    private static final int DESPAWN_TICKS = 15;
    private static final Set<Item> SPECIAL_ITEMS = new HashSet<>();
    private static final Set<Item> PERSISTENT_ITEMS = new HashSet<>();

    private static Set<Item> getSpecialItems() {
        if (SPECIAL_ITEMS.isEmpty()) {
            for (DeferredItem<? extends Item> item : AiMod.ALL_SPECIAL_ITEMS) {
                SPECIAL_ITEMS.add(item.get());
            }
        }
        return SPECIAL_ITEMS;
    }

    private static Set<Item> getPersistentItems() {
        if (PERSISTENT_ITEMS.isEmpty()) {
            PERSISTENT_ITEMS.add(AiMod.DISH_12.get());
        }
        return PERSISTENT_ITEMS;
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) {
            return;
        }
        Item item = itemEntity.getItem().getItem();
        if (!getSpecialItems().contains(item)) {
            return;
        }
        if (getPersistentItems().contains(item)) {
            return;
        }
        itemEntity.lifespan = DESPAWN_TICKS;
    }
}
