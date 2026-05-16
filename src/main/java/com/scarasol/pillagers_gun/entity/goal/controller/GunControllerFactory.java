package com.scarasol.pillagers_gun.entity.goal.controller;

import com.scarasol.pillagers_gun.compat.tacz.TaczCompat;
import com.scarasol.pillagers_gun.config.CommonConfig;
import com.scarasol.pillagers_gun.item.gun.GunItem;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.fml.ModList;

public class GunControllerFactory {
    private GunControllerFactory() {
    }

    public static GunController create(Mob mob) {
        if (mob.getMainHandItem().getItem() instanceof GunItem) {
            return new VanillaGunController(mob);
        }
        if (ModList.get().isLoaded("tacz") && CommonConfig.TACZ_GUN_USE.get()) {
            return TaczCompat.createGunController(mob);
        }
        return EmptyGunController.INSTANCE;
    }
}
