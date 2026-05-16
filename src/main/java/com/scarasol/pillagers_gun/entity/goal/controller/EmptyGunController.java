package com.scarasol.pillagers_gun.entity.goal.controller;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public enum EmptyGunController implements GunController {
    INSTANCE;

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean hasAmmo() {
        return false;
    }

    @Override
    public boolean canReload() {
        return false;
    }

    @Override
    public int getAmmoCount() {
        return 0;
    }

    @Override
    public int getReadyDelayAfterAmmoFound() {
        return 0;
    }

    @Override
    public int getReadyDelayAfterReload() {
        return 0;
    }

    @Override
    public void startReload() {
    }

    @Override
    public GunReloadResult tickReloading() {
        return GunReloadResult.FAILED;
    }

    @Override
    public GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        return GunShotResult.noShot(ammoCount);
    }
}
