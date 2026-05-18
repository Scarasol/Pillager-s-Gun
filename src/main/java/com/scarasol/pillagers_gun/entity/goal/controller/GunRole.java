package com.scarasol.pillagers_gun.entity.goal.controller;

public enum GunRole {
    RIFLE(3.0D),
    SHOTGUN(1.5D),
    PISTOL(1.0D),
    SNIPER(0.8D),
    EXPLOSIVE(0.3D),
    OTHER(1.0D);

    private final double suppressivePower;

    GunRole(double suppressivePower) {
        this.suppressivePower = suppressivePower;
    }

    public double getSuppressivePower() {
        return suppressivePower;
    }

    public boolean shouldTacticalReload(int ammoCount, int maxAmmoCount) {
        if (ammoCount <= 0 || maxAmmoCount <= 1) {
            return false;
        }
        return switch (this) {
            case RIFLE -> ammoCount <= Math.max(1, (int) Math.ceil(maxAmmoCount * 0.25D));
            case PISTOL -> ammoCount <= 2;
            case SHOTGUN, SNIPER -> ammoCount <= 2;
            case EXPLOSIVE -> false;
            case OTHER -> ammoCount <= Math.max(1, (int) Math.ceil(maxAmmoCount * 0.20D));
        };
    }
}
