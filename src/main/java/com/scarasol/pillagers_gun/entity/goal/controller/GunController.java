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

    default int getMaxAmmoCount() {
        return getAmmoCount();
    }

    default GunRole getRole() {
        return GunRole.OTHER;
    }

    default double getSuppressivePower() {
        return getRole().getSuppressivePower();
    }

    default double getAmmoUsePerTick(double distanceToTarget) {
        return 1.0D / Math.max(1, getReadyDelayAfterReload());
    }

    default int getReloadDurationTicks() {
        return 60;
    }

    default boolean shouldTacticalReload() {
        int ammoCount = getAmmoCount();
        int maxAmmoCount = getMaxAmmoCount();
        return hasAmmo()
                && canReload()
                && ammoCount >= 0
                && maxAmmoCount > 1
                && ammoCount < maxAmmoCount
                && getRole().shouldTacticalReload(ammoCount, maxAmmoCount);
    }

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

    default void startAiming(Vec3 targetPosition) {
    }

    default void stopAiming() {
    }

    GunShotResult shoot(LivingEntity target, boolean isStunned, Vec3 lastTargetPosition, int ammoCount);

    default GunShotResult shootAt(Vec3 targetPosition, boolean isStunned, Vec3 lastTargetPosition, int ammoCount) {
        return GunShotResult.noShot(ammoCount);
    }
}
