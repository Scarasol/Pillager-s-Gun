package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.controller.GunRole;

public record CombatMovementProfile(
        double preferredMinRatio,
        double preferredMaxRatio,
        float forwardWeight,
        float strafeWeight,
        float moveScale,
        double closeTargetLookAngleDegrees,
        double farTargetLookAngleDegrees
) {
    private static final double EPSILON = 1.0E-6D;

    public static CombatMovementProfile forRole(GunRole role) {
        return switch (role) {
            case RIFLE -> new CombatMovementProfile(0.35D, 0.70D, 0.70F, 0.55F, 1.0F, 30.0D, 5.0D);
            case PISTOL -> new CombatMovementProfile(0.25D, 0.55D, 0.95F, 0.60F, 1.0F, 30.0D, 5.0D);
            case SHOTGUN -> new CombatMovementProfile(0.15D, 0.35D, 0.80F, 0.35F, 1.0F, 30.0D, 5.0D);
            case SNIPER -> new CombatMovementProfile(0.60D, 0.90D, 0.45F, 0.20F, 0.8F, 30.0D, 5.0D);
            case EXPLOSIVE -> new CombatMovementProfile(0.45D, 0.80D, 0.70F, 0.20F, 0.8F, 30.0D, 5.0D);
            case OTHER -> new CombatMovementProfile(0.35D, 0.65D, 0.60F, 0.35F, 0.9F, 30.0D, 5.0D);
        };
    }

    public float forward(double distanceRatio) {
        double farPressure = positivePressure(distanceRatio - this.preferredMaxRatio, 1.0D - this.preferredMaxRatio);
        double closePressure = positivePressure(this.preferredMinRatio - distanceRatio, this.preferredMinRatio);
        return clamp((float) ((farPressure - closePressure) * this.forwardWeight * this.moveScale), -1.0F, 1.0F);
    }

    public float strafe(boolean targetLookingAtMob, boolean clockwise) {
        if (!targetLookingAtMob) {
            return 0.0F;
        }
        float direction = clockwise ? 1.0F : -1.0F;
        return clamp(direction * this.strafeWeight * this.moveScale, -1.0F, 1.0F);
    }

    public double targetLookAngleDegrees(double distanceRatio) {
        double ratio = clamp(distanceRatio, 0.0D, 1.0D);
        return this.closeTargetLookAngleDegrees
                + (this.farTargetLookAngleDegrees - this.closeTargetLookAngleDegrees) * ratio;
    }

    public boolean shouldMove(float forward, float strafe) {
        return Math.abs(forward) > 0.02F || Math.abs(strafe) > 0.02F;
    }

    private static double positivePressure(double value, double range) {
        return clamp(value / Math.max(EPSILON, range), 0.0D, 1.0D);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
