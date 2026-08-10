package com.xingduansuzhao.aimod.bamboocicada.client;

import com.xingduansuzhao.aimod.AiMod;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public final class BambooCicadaLoopSound extends AbstractTickableSoundInstance {
    private final Player player;
    private final InteractionHand hand;

    BambooCicadaLoopSound(Player player, InteractionHand hand) {
        super(
                AiMod.BAMBOO_CICADA_LOOP.get(),
                SoundSource.PLAYERS,
                SoundInstance.createUnseededRandom()
        );
        this.player = player;
        this.hand = hand;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        updatePosition();
    }

    @Override
    public void tick() {
        if (this.player.isRemoved()
                || this.player.getItemInHand(this.hand).getItem() != AiMod.BAMBOO_CICADA.get()) {
            stopImmediately();
            BambooCicadaSoundManager.forget(this.player.getId(), this);
            return;
        }

        updatePosition();
    }

    void stopImmediately() {
        stop();
    }

    InteractionHand hand() {
        return this.hand;
    }

    private void updatePosition() {
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
    }
}
