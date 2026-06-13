package com.xingduansuzhao.aimod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import java.util.List;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.xingduansuzhao.aimod.canjiaoji.CanjiaojiWeapon;
import com.xingduansuzhao.aimod.fletching.FletchingArrowGenerator;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;
import com.xingduansuzhao.aimod.qingtian.QingtianServerEvents;
import com.xingduansuzhao.aimod.spiritring.SpiritRingItem;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AiMod.MODID)
public class AiMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "aimod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "aimod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "aimod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "aimod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "aimod:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "aimod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Creates a new food item with the id "aimod:example_id", nutrition 1 and saturation 2
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item", p -> p.food(new FoodProperties.Builder()
            .alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    public static final DeferredItem<Item> SPIRIT_RING = ITEMS.registerItem("spirit_ring", SpiritRingItem::new);
    public static final DeferredItem<MyCustomWeapon> QINGTIAN = ITEMS.registerItem("qingtian", MyCustomWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<CanjiaojiWeapon> CANJIAOJI = ITEMS.registerItem("canjiaoji", CanjiaojiWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<Item> TOMATO = ITEMS.registerSimpleItem("tomato", p -> p.food(foodProperties(2, 0.3f)));
    public static final DeferredItem<Item> TOMATO_EGG_STIR_FRY = ITEMS.registerSimpleItem("tomato_egg_stir_fry", p -> p.food(foodProperties(3, 0.45f)));
    public static final DeferredItem<Item> TOMATO_CHICKEN_CASSEROLE = ITEMS.registerSimpleItem("tomato_chicken_casserole", p -> p.food(foodProperties(4, 0.65f)));
    public static final DeferredItem<Item> TOMATO_PORK_CASSEROLE = ITEMS.registerSimpleItem("tomato_pork_casserole", p -> p.food(foodProperties(6, 0.8f)));

    public static final DeferredItem<Item> CHOCOLATE_CAKE = ITEMS.registerSimpleItem("chocolate_cake", p -> p.food(foodProperties(5, 0.6f)));
    public static final DeferredItem<Item> CHOCOLATE_MILK_BUCKET = ITEMS.registerSimpleItem("chocolate_milk_bucket", p -> p.food(foodProperties(3, 0.4f)));
    public static final DeferredItem<Item> CHOCOLATE_DIRTY_BUN = ITEMS.registerSimpleItem("chocolate_dirty_bun", p -> p.food(foodProperties(4, 0.5f)));
    public static final DeferredItem<Item> CHOCOLATE_COOKIE = ITEMS.registerSimpleItem("chocolate_cookie", p -> p.food(foodProperties(2, 0.3f)));
    public static final DeferredItem<Item> BANANA = ITEMS.registerSimpleItem("banana", p -> p.food(foodProperties(2, 0.3f)));
    public static final DeferredItem<Item> STRAWBERRY = ITEMS.registerSimpleItem("strawberry", p -> p.food(foodProperties(2, 0.25f)));
    public static final DeferredItem<Item> GRAPE = ITEMS.registerSimpleItem("grape", p -> p.food(foodProperties(2, 0.2f)));
    public static final DeferredItem<Item> LYCHEE = ITEMS.registerSimpleItem("lychee", p -> p.food(foodProperties(2, 0.25f)));
    public static final DeferredItem<Item> MANGO = ITEMS.registerSimpleItem("mango", p -> p.food(foodProperties(3, 0.35f)));
    public static final DeferredItem<Item> DRAGON_FRUIT = ITEMS.registerSimpleItem("dragon_fruit", p -> p.food(foodProperties(3, 0.3f)));
    public static final DeferredItem<Item> DURIAN = ITEMS.registerSimpleItem("durian", p -> p.food(foodProperties(4, 0.45f)));

    public static final DeferredItem<Item> DISH_4 = ITEMS.registerSimpleItem("dish_4");
    public static final DeferredItem<Item> DISH_5 = ITEMS.registerSimpleItem("dish_5");
    public static final DeferredItem<Item> DISH_6 = ITEMS.registerSimpleItem("dish_6");
    public static final DeferredItem<Item> DISH_7 = ITEMS.registerSimpleItem("dish_7");
    public static final DeferredItem<Item> DISH_8 = ITEMS.registerSimpleItem("dish_8");
    public static final DeferredItem<Item> DISH_9 = ITEMS.registerSimpleItem("dish_9");
    public static final DeferredItem<Item> DISH_10 = ITEMS.registerSimpleItem("dish_10");
    public static final DeferredItem<Item> DISH_11 = ITEMS.registerSimpleItem("dish_11");
    public static final DeferredItem<Item> DISH_12 = ITEMS.registerSimpleItem("dish_12");

    public static final List<DeferredItem<Item>> DISH_ITEMS = List.of(
            DISH_4, DISH_5, DISH_6, DISH_7, DISH_8, DISH_9, DISH_10, DISH_11, DISH_12
    );

    public static final List<DeferredItem<? extends Item>> ALL_SPECIAL_ITEMS = List.of(
            SPIRIT_RING, QINGTIAN, CANJIAOJI, TOMATO, TOMATO_EGG_STIR_FRY, TOMATO_CHICKEN_CASSEROLE, TOMATO_PORK_CASSEROLE,
            CHOCOLATE_CAKE, CHOCOLATE_MILK_BUCKET, CHOCOLATE_DIRTY_BUN, CHOCOLATE_COOKIE,
            BANANA, STRAWBERRY, GRAPE, LYCHEE, MANGO, DRAGON_FRUIT, DURIAN,
            DISH_4, DISH_5, DISH_6, DISH_7, DISH_8, DISH_9, DISH_10, DISH_11, DISH_12
    );
    public static final List<DeferredItem<? extends AnimatedWeaponItem>> ANIMATED_WEAPON_ITEMS = List.of(
            QINGTIAN, CANJIAOJI
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> QINGTIAN_SWITCH = SOUND_EVENTS.register(
            "item.qingtian.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.qingtian.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> QINGTIAN_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.qingtian.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.qingtian.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> QINGTIAN_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.qingtian.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.qingtian.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> QINGTIAN_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.qingtian.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.qingtian.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CANJIAOJI_SWITCH = SOUND_EVENTS.register(
            "item.canjiaoji.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.canjiaoji.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CANJIAOJI_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.canjiaoji.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.canjiaoji.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CANJIAOJI_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.canjiaoji.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.canjiaoji.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> CANJIAOJI_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.canjiaoji.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.canjiaoji.light_attack_2"))
    );

    // Creates a creative tab with the id "aimod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.aimod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> TOMATO.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
                output.accept(SPIRIT_RING.get());
                output.accept(QINGTIAN.get());
                output.accept(CANJIAOJI.get());
                output.accept(TOMATO.get());
                output.accept(TOMATO_EGG_STIR_FRY.get());
                output.accept(TOMATO_CHICKEN_CASSEROLE.get());
                output.accept(TOMATO_PORK_CASSEROLE.get());
                output.accept(CHOCOLATE_CAKE.get());
                output.accept(CHOCOLATE_MILK_BUCKET.get());
                output.accept(CHOCOLATE_DIRTY_BUN.get());
                output.accept(CHOCOLATE_COOKIE.get());
                output.accept(BANANA.get());
                output.accept(STRAWBERRY.get());
                output.accept(GRAPE.get());
                output.accept(LYCHEE.get());
                output.accept(MANGO.get());
                output.accept(DRAGON_FRUIT.get());
                output.accept(DURIAN.get());
                DISH_ITEMS.forEach(dish -> output.accept(dish.get()));
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AiMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (AIMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(QingtianServerEvents::onServerTick);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));

        event.enqueueWork(() -> {
            FireBlock fireBlock = (FireBlock) Blocks.FIRE;
            fireBlock.setFlammable(Blocks.BAMBOO, 0, 0);
            fireBlock.setFlammable(Blocks.BAMBOO_SAPLING, 0, 0);
        });
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
    
    // 监听玩家登出事件，清理相关数据
    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        FletchingArrowGenerator.cleanupPlayerData(event.getEntity());
        LOGGER.info("清理玩家 {} 的制箭台数据", event.getEntity().getName().getString());
    }
    private static FoodProperties foodProperties(int hungerShanks, float saturationModifier) {
        return new FoodProperties.Builder()
                .nutrition(hungerShanks * 2)
                .saturationModifier(saturationModifier)
                .build();
    }
}
