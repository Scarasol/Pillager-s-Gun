package com.scarasol.pillagers_gun.item.ammo;

import net.minecraft.world.item.ArrowItem;

public abstract class AmmoItem extends ArrowItem {
    public AmmoItem(Properties properties) {
        super(properties);
    }

    public abstract float getSpeed();
}
