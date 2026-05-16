package com.scarasol.pillagers_gun.entity.goal.controller;

public class DynamicFireRate {
    private static final double TICKS_PER_MINUTE = 1200D;
    private static final double SEMI_AUTO_MIN_DIVISOR = 2400D;
    private static final double SEMI_AUTO_DISTANCE_DIVISOR = 1200D;
    private static final double SEMI_AUTO_DISTANCE_REFERENCE = 8D;

    private DynamicFireRate() {
    }

    public static double cooldownTicksToRpm(int cooldownTicks) {
        return TICKS_PER_MINUTE / Math.max(cooldownTicks, 1);
    }

    public static double getStep(boolean automatic, double rpm, double distance, boolean stunned) {
        return automatic ? getAutoStep(rpm) : getSemiAutoStep(rpm, distance, stunned);
    }

    public static double getAutoStep(double rpm) {
        return rpm / TICKS_PER_MINUTE;
    }

    public static double getSemiAutoStep(double rpm, double distance, boolean stunned) {
        return rpm / getSemiAutoDivisor(distance, stunned);
    }

    private static double getSemiAutoDivisor(double distance, boolean stunned) {
        return stunned ? SEMI_AUTO_MIN_DIVISOR : Math.max(SEMI_AUTO_DISTANCE_DIVISOR * (distance / SEMI_AUTO_DISTANCE_REFERENCE), SEMI_AUTO_MIN_DIVISOR);
    }
}
