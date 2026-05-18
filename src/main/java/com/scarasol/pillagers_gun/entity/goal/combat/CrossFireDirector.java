package com.scarasol.pillagers_gun.entity.goal.combat;

import com.scarasol.pillagers_gun.entity.goal.GunAttackGoal;

public class CrossFireDirector extends NoTacticsDirector {
    public static final CrossFireDirector INSTANCE = new CrossFireDirector();

    private CrossFireDirector() {
    }

    @Override
    public CombatIntent selectIntent(CombatContext context) {
        FireTeamCoordinator.INSTANCE.update(context);
        if (shouldRequestReload(context)) {
            if (FireTeamCoordinator.INSTANCE.requestReload(context) == ReloadPermission.ALLOW) {
                return CombatIntent.RELOAD;
            }
        }
        if (FireTeamCoordinator.INSTANCE.shouldHoldFire(context)) {
            return CombatIntent.HOLD_FIRE;
        }
        return super.selectIntent(context);
    }

    private boolean shouldRequestReload(CombatContext context) {
        if (!context.targetValid()
                || context.stunned()
                || !context.controller().canReload()) {
            return false;
        }

        if (context.gunState() == GunAttackGoal.GunState.UNCHARGED && !context.controller().hasAmmo()) {
            return true;
        }

        if (context.delayReloadForPositioning()) {
            return false;
        }

        return (context.gunState() == GunAttackGoal.GunState.CHARGED
                || context.gunState() == GunAttackGoal.GunState.READY_TO_ATTACK)
                && FireTeamCoordinator.INSTANCE.shouldTacticalReload(context);
    }
}
