package com.xingduansuzhao.aimod.qingtian.client;

import com.xingduansuzhao.aimod.qingtian.MyCustomWeapon;
import com.xingduansuzhao.aimod.weapon.client.AnimatedWeaponRenderer;

/** Uses GeckoLib's full-bright emissive layer for the marked Qingtian pixels. */
public final class QingtianRenderer extends AnimatedWeaponRenderer<MyCustomWeapon> {
    public QingtianRenderer(MyCustomWeapon weapon) {
        super(weapon);
        withRenderLayer(new QingtianEmissiveLayer(this));
    }
}
