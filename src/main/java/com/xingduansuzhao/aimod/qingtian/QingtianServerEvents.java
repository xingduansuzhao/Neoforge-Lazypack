package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class QingtianServerEvents {
    private QingtianServerEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        AnimatedWeaponItem.tickServerPlayers(event.getServer().getPlayerList().getPlayers());
    }
}
