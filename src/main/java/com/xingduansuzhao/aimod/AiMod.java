package com.xingduansuzhao.aimod;

import com.mojang.logging.LogUtils;
import com.xingduansuzhao.aimod.bamboocicada.event.BambooCicadaLoopHandler;
import com.xingduansuzhao.aimod.bamboocicada.event.ToolsmithTradeHandler;
import com.xingduansuzhao.aimod.bamboocicada.item.BambooCicadaItem;
import com.xingduansuzhao.aimod.bamboocicada.network.BambooCicadaSoundPayload;
import com.xingduansuzhao.aimod.bamboocicada.network.BambooCicadaStopPayload;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(AiMod.MODID)
public final class AiMod {
    public static final String MODID = "bamboo_cicada";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredItem<BambooCicadaItem> BAMBOO_CICADA = ITEMS.registerItem(
            "bamboo_cicada", BambooCicadaItem::new, properties -> properties.stacksTo(1));
    public static final DeferredItem<Item> SPECIAL_EMERALD = ITEMS.registerSimpleItem(
            "special_emerald", properties -> properties.stacksTo(1));

    public static final DeferredHolder<SoundEvent, SoundEvent> BAMBOO_CICADA_LOOP = SOUND_EVENTS.register(
            "item.bamboo_cicada.loop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    MODID, "item.bamboo_cicada.loop"))
    );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bamboo_cicada"))
                    .withTabsBefore(CreativeModeTabs.INGREDIENTS)
                    .icon(() -> BAMBOO_CICADA.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(BAMBOO_CICADA.get());
                        output.accept(SPECIAL_EMERALD.get());
                    })
                    .build());

    public AiMod(IEventBus modEventBus, ModContainer modContainer) {
        ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(ToolsmithTradeHandler::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(BambooCicadaLoopHandler::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(BambooCicadaLoopHandler::onPlayerClone);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                BambooCicadaSoundPayload.TYPE,
                BambooCicadaSoundPayload.STREAM_CODEC,
                (payload, context) -> payload.handleClient()
        ).playToServer(
                BambooCicadaStopPayload.TYPE,
                BambooCicadaStopPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                        BambooCicadaLoopHandler.stopFromInput(player);
                    }
                }
        );
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BambooCicadaLoopHandler.onPlayerLoggedOut(event);
    }

}
