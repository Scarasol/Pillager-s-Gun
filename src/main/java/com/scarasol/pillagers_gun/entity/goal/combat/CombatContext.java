package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;
import com.scarasol.pillagers_gun.entity.goal.controller.GunController;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;

public record CombatContext(
        Mob mob,
        @Nullable
        LivingEntity target,
        GunController controller,
        GunAttackGoal.GunState gunState,
        boolean targetValid,
        boolean hasLineOfSight,
        boolean stunned,
        boolean delayReloadForPositioning,
        double distanceToTarget,
        double attackRadius,
        int seeTime,
        int attackDelay,
        int ammoCount,
        long gameTime
) {
}
