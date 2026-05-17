package com.scarasol.pillagers_gun.entity.goal.controller;

import com.scarasol.pillagers_gun.compat.sbw.SbwCompat;
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
        if (ModList.get().isLoaded("superbwarfare") && CommonConfig.SBW_GUN_USE.get()) {
            GunController controller = SbwCompat.createGunController(mob);
            if (controller.isValid()) {
                return controller;
            }
        }
        if (ModList.get().isLoaded("tacz") && CommonConfig.TACZ_GUN_USE.get()) {
            GunController controller = TaczCompat.createGunController(mob);
            if (controller.isValid()) {
                return controller;
            }
        }
        return EmptyGunController.INSTANCE;
    }
}
