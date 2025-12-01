package com.scarasol.pillagers_gun.util;

import com.scarasol.pillagers_gun.init.PillagersGunItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GunUtil {
    public static boolean isSniperGun(ItemStack itemStack) {

        if (itemStack.is(PillagersGunItems.SNIPERS_RIFLE.get())) {
            return true;
        }
        return false;
    }
}
