package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.AiMod;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record QingtianTransformPayload(boolean restore) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<QingtianTransformPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "qingtian_transform"));

    public static final StreamCodec<ByteBuf, QingtianTransformPayload> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, QingtianTransformPayload::restore, QingtianTransformPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
