package com.scarasol.pillagers_gun.util;

import com.scarasol.pillagers_gun.init.PillagersGunItems;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * @author Scarasol
 */
public class GunUtil {

    private static final double ANG_DEAD_ZONE_DEG_PER_TICK = 0.05D;
    private static final double ANG_REF_DEG_PER_TICK = 0.70D;
    private static final double ANG_MAX_BOOST = 0.90D;

    private static final double DIST_REF_BLOCKS = 16.0D;
    private static final double DIST_CLAMP_MIN = 0.25D;
    private static final double DIST_CLAMP_MAX = 4.00D;
    private static final double DIST_EXP = 0.50D;

    public static boolean isSniperGun(ItemStack itemStack) {
        return itemStack.is(PillagersGunItems.SNIPERS_RIFLE.get());
    }

    /**
     * 线速度（矢量）：entity2 相对 entity1 的“垂直于两者 EyePosition 连线”的速度分量（3D，包含Y）
     * 单位：方块 / tick
     *
     * vRel = v2 - v1
     * r = eye2 - eye1
     * vPerp = vRel - r * (vRel·r)/|r|^2   （不做归一化，更省资源）
     */
    public static Vec3 getPerpendicularVelocityToEyeLine(Entity entity1, Entity entity2, float partialTicks) {
        Vec3 r = entity2.getEyePosition(partialTicks).subtract(entity1.getEyePosition(partialTicks));
        double r2 = r.lengthSqr();
        if (r2 < 1.0E-12) {
            return Vec3.ZERO;
        }

        Vec3 vRel = entity2.getDeltaMovement().subtract(entity1.getDeltaMovement());
        double projScale = vRel.dot(r) / r2;
        return vRel.subtract(r.scale(projScale));
    }

    /**
     * 角速度（标量）：entity2 相对 entity1 围绕“眼睛连线”的角速度大小
     * 返回单位：度 / tick
     *
     * ω(rad/tick) = |r × vRel| / |r|^2
     */
    public static double getAngularSpeedToEyeLineDegPerTick(Entity entity1, Entity entity2, float partialTicks) {
        Vec3 r = entity2.getEyePosition(partialTicks).subtract(entity1.getEyePosition(partialTicks));
        double r2 = r.lengthSqr();
        if (r2 < 1.0E-12) {
            return 0.0D;
        }

        Vec3 vRel = entity2.getDeltaMovement().subtract(entity1.getDeltaMovement());
        double omegaRadPerTick = r.cross(vRel).length() / r2;
        return omegaRadPerTick * (180.0D / Math.PI);
    }

    /**
     * 基于“相对角速度 + 距离”的不精准度放大（以 oldInaccuracy 为基准百分比）。
     * 距离以 16 格为 1.0；越远越不准、越近越准；狙击枪忽略距离因子。
     */
    public static float getModifiedInaccuracy(Entity attacker, Entity target, float oldInaccuracy) {
        if (oldInaccuracy <= 0.0F) {
            return oldInaccuracy;
        }

        boolean sniper = false;
        if (attacker instanceof LivingEntity living) {
            sniper = isSniperGun(living.getMainHandItem());
        }

        double distMul = 1.0D;
        if (!sniper) {
            Vec3 r = target.getEyePosition(1.0F).subtract(attacker.getEyePosition(1.0F));
            double dist = r.length();
            double d = dist / DIST_REF_BLOCKS;
            d = Mth.clamp(d, DIST_CLAMP_MIN, DIST_CLAMP_MAX);
            distMul = Math.pow(d, DIST_EXP);
        }

        // 角速度倍率（所有枪都启用）
        double omega = Math.abs(getAngularSpeedToEyeLineDegPerTick(attacker, target, 1.0F)); // deg/tick
        omega = Math.max(0.0D, omega - ANG_DEAD_ZONE_DEG_PER_TICK);

        double w2 = omega * omega;
        double angMul = 1.0D + ANG_MAX_BOOST * (w2 / (w2 + ANG_REF_DEG_PER_TICK * ANG_REF_DEG_PER_TICK));

        double mul = distMul * angMul;
        return (float) (oldInaccuracy * mul);
    }
}
