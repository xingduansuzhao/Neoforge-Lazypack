package com.xingduansuzhao.aimod.bamboocicada.network;

import com.xingduansuzhao.aimod.AiMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record BambooCicadaStopPayload() implements CustomPacketPayload {
    public static final BambooCicadaStopPayload INSTANCE = new BambooCicadaStopPayload();
    public static final Type<BambooCicadaStopPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "bamboo_cicada_stop")
    );
    public static final StreamCodec<ByteBuf, BambooCicadaStopPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
