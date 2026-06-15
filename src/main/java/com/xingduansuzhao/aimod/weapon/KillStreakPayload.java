package com.xingduansuzhao.aimod.weapon;

import com.xingduansuzhao.aimod.AiMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record KillStreakPayload(int streakIndex) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<KillStreakPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "killstreak"));

    public static final StreamCodec<ByteBuf, KillStreakPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, KillStreakPayload::streakIndex, KillStreakPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
