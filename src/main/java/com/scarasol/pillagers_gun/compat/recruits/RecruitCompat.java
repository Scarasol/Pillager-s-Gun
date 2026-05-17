package com.scarasol.pillagers_gun.compat.recruits;

import com.scarasol.pillagers_gun.compat.recruits.goal.RecruitGunStrategicFireGoal;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.BowmanEntity;
import com.talhanation.recruits.entities.CrossBowmanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;

public final class RecruitCompat {
    private RecruitCompat() {
    }

    public static void addGunGoals(Mob mob) {
        if (mob instanceof AbstractRecruitEntity recruit && supportsStrategicFire(recruit)) {
            mob.goalSelector.addGoal(-1, new RecruitGunStrategicFireGoal(recruit));
        }
    }

    public static boolean canUseRegularGunAttack(Mob mob) {
        return !(mob instanceof AbstractRecruitEntity recruit) || recruit.getShouldRanged();
    }

    public static boolean supportsStrategicFire(AbstractRecruitEntity recruit) {
        return recruit instanceof BowmanEntity || recruit instanceof CrossBowmanEntity;
    }

    public static boolean shouldStrategicFire(AbstractRecruitEntity recruit) {
        if (recruit instanceof BowmanEntity bowman) {
            return bowman.getShouldStrategicFire();
        }
        if (recruit instanceof CrossBowmanEntity crossBowman) {
            return crossBowman.getShouldStrategicFire();
        }
        return false;
    }

    public static BlockPos getStrategicFirePos(AbstractRecruitEntity recruit) {
        if (recruit instanceof BowmanEntity bowman) {
            return bowman.StrategicFirePos();
        }
        if (recruit instanceof CrossBowmanEntity crossBowman) {
            return crossBowman.getStrategicFirePos();
        }
        return null;
    }
}
