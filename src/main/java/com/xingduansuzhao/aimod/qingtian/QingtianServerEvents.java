package com.xingduansuzhao.aimod.qingtian;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class QingtianServerEvents {
    private QingtianServerEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        MyCustomWeapon.tickServerPlayers(event.getServer().getPlayerList().getPlayers());
    }
}
