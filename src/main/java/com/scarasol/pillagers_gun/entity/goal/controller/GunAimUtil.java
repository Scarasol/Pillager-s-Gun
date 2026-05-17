package com.scarasol.pillagers_gun.entity.goal.controller;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public final class GunAimUtil {
    private GunAimUtil() {
    }

    public static void lookAt(Mob mob, Vec3 targetPosition) {
        mob.getLookControl().setLookAt(targetPosition.x, targetPosition.y, targetPosition.z, 30.0F, 30.0F);

        Vec3 direction = targetPosition.subtract(mob.getEyePosition());
        if (direction.lengthSqr() < 1.0E-6D) {
            return;
        }

        double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        float yRot = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
        float xRot = (float) (-(Mth.atan2(direction.y, horizontalDistance) * Mth.RAD_TO_DEG));
        mob.setYRot(yRot);
        mob.setYHeadRot(yRot);
        mob.yBodyRot = yRot;
        mob.setXRot(xRot);
    }

    public static boolean isLookingAt(Mob mob, Vec3 targetPosition, double toleranceDegrees) {
        Vec3 targetDirection = targetPosition.subtract(mob.getEyePosition());
        if (targetDirection.lengthSqr() < 1.0E-6D) {
            return true;
        }
        return vectorDegreeCalculate(mob.getViewVector(1), targetDirection) < toleranceDegrees;
    }

    public static double vectorDegreeCalculate(Vec3 vec1, Vec3 vec2) {
        double denominator = vec1.length() * vec2.length();
        if (denominator < 1.0E-6D) {
            return 0.0D;
        }
        double cos = Mth.clamp(vec1.dot(vec2) / denominator, -1.0D, 1.0D);
        return Math.toDegrees(Math.acos(cos));
    }
}
