package com.scarasol.pillagers_gun.entity.goal.controller;

public class GunShotResult {
    private final int ammoCount;
    private final boolean depleted;
    private final boolean shouldCooldown;
    private final int cooldownTicks;

    private GunShotResult(int ammoCount, boolean depleted, boolean shouldCooldown, int cooldownTicks) {
        this.ammoCount = ammoCount;
        this.depleted = depleted;
        this.shouldCooldown = shouldCooldown;
        this.cooldownTicks = cooldownTicks;
    }

    public static GunShotResult noShot(int ammoCount) {
        return new GunShotResult(ammoCount, false, false, 0);
    }

    public static GunShotResult ready(int ammoCount) {
        return new GunShotResult(ammoCount, false, false, 0);
    }

    public static GunShotResult cooldown(int ammoCount, int cooldownTicks) {
        return new GunShotResult(ammoCount, false, true, cooldownTicks);
    }

    public static GunShotResult depleted() {
        return new GunShotResult(0, true, false, 0);
    }

    public int getAmmoCount() {
        return ammoCount;
    }

    public boolean isDepleted() {
        return depleted;
    }

    public boolean shouldCooldown() {
        return shouldCooldown;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }
}
