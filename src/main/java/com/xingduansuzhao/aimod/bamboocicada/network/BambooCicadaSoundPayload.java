package com.xingduansuzhao.aimod.bamboocicada.network;

import com.xingduansuzhao.aimod.AiMod;
import com.xingduansuzhao.aimod.bamboocicada.client.BambooCicadaSoundManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record BambooCicadaSoundPayload(int entityId, boolean playing, int handOrdinal)
        implements CustomPacketPayload {
    public static final Type<BambooCicadaSoundPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AiMod.MODID, "bamboo_cicada_sound")
    );

    public static final StreamCodec<ByteBuf, BambooCicadaSoundPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BambooCicadaSoundPayload::entityId,
            ByteBufCodecs.BOOL,
            BambooCicadaSoundPayload::playing,
            ByteBufCodecs.VAR_INT,
            BambooCicadaSoundPayload::handOrdinal,
            BambooCicadaSoundPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient() {
        InteractionHand hand = this.handOrdinal == InteractionHand.OFF_HAND.ordinal()
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        BambooCicadaSoundManager.setPlaying(this.entityId, this.playing, hand);
    }
}
