package com.xingduansuzhao.aimod;

import com.xingduansuzhao.aimod.bamboocicada.client.SpecialEmeraldDecorator;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;

@Mod(value = AiMod.MODID, dist = Dist.CLIENT)
public class AiModClient {
    public AiModClient(IEventBus modEventBus) {
        modEventBus.addListener(AiModClient::onClientSetup);
        modEventBus.addListener(AiModClient::registerItemDecorations);
    }

    static void onClientSetup(FMLClientSetupEvent event) {
        AiMod.LOGGER.info("HELLO FROM CLIENT SETUP");
        AiMod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        event.register(AiMod.SPECIAL_EMERALD.get(), SpecialEmeraldDecorator.INSTANCE);
    }
}
