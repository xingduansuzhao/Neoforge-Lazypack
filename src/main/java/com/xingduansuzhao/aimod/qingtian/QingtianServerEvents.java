package com.xingduansuzhao.aimod.qingtian;

import com.xingduansuzhao.aimod.karambit.KarambitWeapon;
import com.xingduansuzhao.aimod.weapon.AnimatedWeaponItem;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class QingtianServerEvents {
    private QingtianServerEvents() {
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList().getPlayers();
        AnimatedWeaponItem.tickServerPlayers(players);
        KarambitWeapon.tickServerPlayers(players);
    }
}
