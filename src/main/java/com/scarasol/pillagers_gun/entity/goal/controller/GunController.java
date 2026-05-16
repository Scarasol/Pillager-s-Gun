package com.scarasol.pillagers_gun.entity.goal.controller;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface GunController {
    boolean isValid();

    default void start() {
    }

    default void stop() {
    }

    boolean hasAmmo();

    boolean canReload();

    int getAmmoCount();

    int getReadyDelayAfterAmmoFound();

    int getReadyDelayAfterReload();

    void startReload();

    GunReloadResult tickReloading();

    default boolean isAttackBlocked() {
        return false;
    }

    default boolean canMeleeAttack(LivingEntity target) {
        return false;
    }

    default void meleeAttack(LivingEntity target) {
    }

    default void startAiming(LivingEntity target) {
    }

    default void stopAiming() {
    }

    GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount);
}
