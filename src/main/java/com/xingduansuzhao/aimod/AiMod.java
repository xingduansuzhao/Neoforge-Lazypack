package com.xingduansuzhao.aimod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import java.util.List;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.xingduansuzhao.aimod.weapon.KillStreakPayload;
import com.xingduansuzhao.aimod.qingtian.QingtianTransformHandler;
import com.xingduansuzhao.aimod.qingtian.QingtianTransformPayload;

import com.xingduansuzhao.aimod.baseballbat.BaseballBatWeapon;
import com.xingduansuzhao.aimod.canjiaoji.CanjiaojiWeapon;
import com.xingduansuzhao.aimod.combataxe.CombatAxeWeapon;
import com.xingduansuzhao.aimod.karambit.KarambitWeapon;
import com.xingduansuzhao.aimod.knife.KnifeWeapon;
import com.xingduansuzhao.aimod.kris.KrisWeapon;
import com.xingduansuzhao.aimod.kukri.KukriWeapon;
import com.xingduansuzhao.aimod.laserknife.LaserKnifeWeapon;
import com.xingduansuzhao.aimod.militaryshovel.MilitaryShovelWeapon;
import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;
import com.xingduansuzhao.aimod.qingtian.QingtianServerEvents;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;
import com.xingduansuzhao.aimod.weapon.KillStreakTracker;
import com.xingduansuzhao.aimod.weapon.SharedAnimatedWeapon;
import com.xingduansuzhao.aimod.weapon.WeaponKillCycler;
import com.xingduansuzhao.aimod.wrench.WrenchWeapon;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AiMod.MODID)
public class AiMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "yaoniming3000";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Items which will all be registered under the "aimod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "aimod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<MyCustomWeapon> QINGTIAN = ITEMS.registerItem("qingtian", MyCustomWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL = ITEMS.registerItem("military_shovel", MilitaryShovelWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL_GOLD = ITEMS.registerItem("military_shovel_gold",
            p -> new MilitaryShovelWeapon("military_shovel_gold", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL_CHRISTMAS = ITEMS.registerItem("military_shovel_christmas",
            p -> new MilitaryShovelWeapon("military_shovel_christmas", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL_HALLOWEEN = ITEMS.registerItem("military_shovel_halloween",
            p -> new MilitaryShovelWeapon("military_shovel_halloween", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL_RED = ITEMS.registerItem("military_shovel_red",
            p -> new MilitaryShovelWeapon("military_shovel_red", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<MilitaryShovelWeapon> MILITARY_SHOVEL_H = ITEMS.registerItem("military_shovel_h",
            p -> new MilitaryShovelWeapon("military_shovel_h", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<SharedAnimatedWeapon> BOXING_GLOVE = ITEMS.registerItem("boxing_glove",
            p -> SharedAnimatedWeapon.withMilitaryShovelSounds("boxing_glove", p),
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<LaserKnifeWeapon> LASER_KNIFE = ITEMS.registerItem("laser_knife", LaserKnifeWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<SharedAnimatedWeapon> BATON = ITEMS.registerItem("baton",
            p -> SharedAnimatedWeapon.withMilitaryShovelSounds("baton", p),
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<SharedAnimatedWeapon> REAPER = ITEMS.registerItem("reaper",
            p -> SharedAnimatedWeapon.withMilitaryShovelSounds("reaper", p),
            p -> p.sword(ToolMaterial.DIAMOND, 7.0f, -2.8f));
    public static final DeferredItem<CanjiaojiWeapon> CANJIAOJI = ITEMS.registerItem("canjiaoji", CanjiaojiWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<KukriWeapon> KUKRI = ITEMS.registerItem("kukri", KukriWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<KrisWeapon> KRIS = ITEMS.registerItem("kris", KrisWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<BaseballBatWeapon> BASEBALL_BAT = ITEMS.registerItem("baseball_bat", BaseballBatWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<KnifeWeapon> KNIFE = ITEMS.registerItem("knife", KnifeWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<CombatAxeWeapon> COMBAT_AXE = ITEMS.registerItem("combat_axe", CombatAxeWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 8.0f, -2.4f));
    public static final DeferredItem<WrenchWeapon> WRENCH = ITEMS.registerItem("wrench", WrenchWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final DeferredItem<KarambitWeapon> KARAMBIT = ITEMS.registerItem("karambit", KarambitWeapon::new,
            p -> p.sword(ToolMaterial.DIAMOND, 5.0f, -2.4f));
    public static final List<DeferredItem<? extends AnimatedWeaponItem>> ANIMATED_WEAPON_ITEMS = List.of(
            QINGTIAN, MILITARY_SHOVEL, MILITARY_SHOVEL_GOLD, MILITARY_SHOVEL_CHRISTMAS,
            MILITARY_SHOVEL_HALLOWEEN, MILITARY_SHOVEL_RED, MILITARY_SHOVEL_H, BOXING_GLOVE, LASER_KNIFE,
            BATON, REAPER, CANJIAOJI, KUKRI, KRIS, BASEBALL_BAT, KNIFE, COMBAT_AXE, WRENCH, KARAMBIT
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
    public static final DeferredHolder<SoundEvent, SoundEvent> KUKRI_SWITCH = SOUND_EVENTS.register(
            "item.kukri.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kukri.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KUKRI_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.kukri.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kukri.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KUKRI_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.kukri.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kukri.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KUKRI_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.kukri.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kukri.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KRIS_SWITCH = SOUND_EVENTS.register(
            "item.kris.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kris.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KRIS_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.kris.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kris.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KRIS_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.kris.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kris.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KRIS_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.kris.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.kris.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BASEBALL_BAT_SWITCH = SOUND_EVENTS.register(
            "item.baseball_bat.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.baseball_bat.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BASEBALL_BAT_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.baseball_bat.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.baseball_bat.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BASEBALL_BAT_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.baseball_bat.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.baseball_bat.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> BASEBALL_BAT_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.baseball_bat.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.baseball_bat.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_SWITCH = SOUND_EVENTS.register(
            "item.knife.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.knife.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> LASER_KNIFE_SWITCH = SOUND_EVENTS.register(
            "item.laser_knife.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.laser_knife.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.knife.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.knife.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.knife.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.knife.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KNIFE_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.knife.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.knife.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_AXE_SWITCH = SOUND_EVENTS.register(
            "item.combat_axe.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.combat_axe.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_AXE_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.combat_axe.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.combat_axe.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> COMBAT_AXE_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.combat_axe.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.combat_axe.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH_SWITCH = SOUND_EVENTS.register(
            "item.wrench.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.wrench.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.wrench.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.wrench.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.wrench.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.wrench.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> WRENCH_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.wrench.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.wrench.light_attack_2"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KARAMBIT_SWITCH = SOUND_EVENTS.register(
            "item.karambit.switch",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.karambit.switch"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KARAMBIT_HEAVY_ATTACK = SOUND_EVENTS.register(
            "item.karambit.heavy_attack",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.karambit.heavy_attack"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KARAMBIT_LIGHT_ATTACK_1 = SOUND_EVENTS.register(
            "item.karambit.light_attack_1",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.karambit.light_attack_1"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KARAMBIT_LIGHT_ATTACK_2 = SOUND_EVENTS.register(
            "item.karambit.light_attack_2",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "item.karambit.light_attack_2"))
    );

    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_HEADSHOT = SOUND_EVENTS.register(
            "killstreak.headshot",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.headshot"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_DOUBLE_KILL = SOUND_EVENTS.register(
            "killstreak.double_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.double_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_MULTI_KILL = SOUND_EVENTS.register(
            "killstreak.multi_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.multi_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_MEGA_KILL = SOUND_EVENTS.register(
            "killstreak.mega_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.mega_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_ULTRA_KILL = SOUND_EVENTS.register(
            "killstreak.ultra_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.ultra_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_MONSTER_KILL = SOUND_EVENTS.register(
            "killstreak.monster_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.monster_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_LUDICROUS_KILL = SOUND_EVENTS.register(
            "killstreak.ludicrous_kill",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.ludicrous_kill"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_HOLY_SHIT = SOUND_EVENTS.register(
            "killstreak.holy_shit",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.holy_shit"))
    );
    public static final DeferredHolder<SoundEvent, SoundEvent> KILLSTREAK_ICON = SOUND_EVENTS.register(
            "killstreak.icon",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, "killstreak.icon"))
    );

    // Creates a creative tab with the id "yaoniming3000:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.yaoniming3000")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> QINGTIAN.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(QINGTIAN.get());
                output.accept(MILITARY_SHOVEL.get());
                output.accept(MILITARY_SHOVEL_GOLD.get());
                output.accept(MILITARY_SHOVEL_CHRISTMAS.get());
                output.accept(MILITARY_SHOVEL_HALLOWEEN.get());
                output.accept(MILITARY_SHOVEL_RED.get());
                output.accept(MILITARY_SHOVEL_H.get());
                output.accept(BOXING_GLOVE.get());
                output.accept(LASER_KNIFE.get());
                output.accept(BATON.get());
                output.accept(REAPER.get());
                output.accept(CANJIAOJI.get());
                output.accept(KUKRI.get());
                output.accept(KRIS.get());
                output.accept(BASEBALL_BAT.get());
                output.accept(KNIFE.get());
                output.accept(COMBAT_AXE.get());
                output.accept(WRENCH.get());
                output.accept(KARAMBIT.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AiMod(IEventBus modEventBus, ModContainer modContainer) {
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
        NeoForge.EVENT_BUS.addListener(KarambitWeapon::onSwapHands);

        // Register network payloads
        modEventBus.addListener(this::registerPayloads);

    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(KillStreakPayload.TYPE, KillStreakPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    com.xingduansuzhao.aimod.weapon.KillStreakOverlay.triggerIcon(payload.streakIndex());
                }));
        registrar.playToServer(QingtianTransformPayload.TYPE, QingtianTransformPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        QingtianTransformHandler.handleTransform(serverPlayer, payload.restore());
                    }
                }));
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
        KillStreakTracker.cleanupPlayer(event.getEntity().getUUID());
        KarambitWeapon.cleanupPlayer(event.getEntity().getUUID());
        QingtianTransformHandler.cleanupPlayer(event.getEntity().getUUID());
        WeaponKillCycler.cleanupPlayer(event.getEntity().getUUID());
        LOGGER.info("清理玩家 {} 的数据", event.getEntity().getName().getString());
    }
}
